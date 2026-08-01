package my.id.kentoes.rsudajibarangapp.sync

import kotlinx.coroutines.flow.first
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.inspection.InspectionHistoryRepository
import my.id.kentoes.rsudajibarangapp.inspection.InspectionRepository
import my.id.kentoes.rsudajibarangapp.master.MasterDataRepository
import my.id.kentoes.rsudajibarangapp.sync.api.DetailSubmit
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionSubmit
import my.id.kentoes.rsudajibarangapp.sync.api.PhotoSubmit
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hasil sinkronisasi untuk satu draf.
 */
data class SyncResult(
    val draftId: Long,
    val success: Boolean,
    val message: String
)

/**
 * Hasil sinkronisasi master data per langkah. Setiap langkah menulis ke DB begitu
 * sukses, jadi kegagalan di tengah = data ter-update sebagian — pesan error harus
 * mencerminkan itu, bukan "gagal total" (H1, fix partial-sync).
 */
data class MasterDataSyncResult(
    val succeeded: List<String>,
    val failed: List<String>,
    val firstError: String? = null
) {
    val total: Int get() = succeeded.size + failed.size
    val isPartial: Boolean get() = failed.isNotEmpty() && succeeded.isNotEmpty()
    val isAllFailed: Boolean get() = failed.isNotEmpty() && succeeded.isEmpty()
}

@Singleton
class SyncManager @Inject constructor(
    private val inspectionRepository: InspectionRepository,
    private val inspectionHistoryRepository: InspectionHistoryRepository,
    private val masterDataRepository: MasterDataRepository,
    private val drafDao: DrafDao,
    private val syncApi: SyncApi,
    private val imageCompressor: ImageCompressor,
    private val sentPhotoStorage: SentPhotoStorage
) {

    /** Sync master data — per-langkah; hasil parsial dilaporkan, bukan dilempar. */
    suspend fun syncMasterData(): MasterDataSyncResult {
        val steps: List<Pair<String, suspend () -> Unit>> = listOf(
            "Items" to { masterDataRepository.syncItems() },
            "Ruangan" to { masterDataRepository.syncRooms() },
            "Pivot Room-Item" to { masterDataRepository.syncRoomItems() },
            "Ruangan Saya" to { masterDataRepository.syncMyRooms() },
            "User-Room" to { masterDataRepository.syncUserRooms() },
            "Users" to { masterDataRepository.syncUsers() }
        )
        val succeeded = mutableListOf<String>()
        val failed = mutableListOf<String>()
        var firstError: String? = null
        for ((name, step) in steps) {
            runCatching { step() }
                .onSuccess { succeeded += name }
                .onFailure {
                    failed += name
                    if (firstError == null) firstError = it.message
                }
        }
        return MasterDataSyncResult(succeeded, failed, firstError)
    }

    /**
     * Sinkronisasi semua draf dengan status PENDING_SYNC.
     * Dipanggil oleh SyncWorker.
     */
    suspend fun syncAllPending(): List<SyncResult> {
        val results = mutableListOf<SyncResult>()
        // Sync master data dulu — hasil parsial tidak menggagalkan sync draf
        syncMasterData()

        // Load draf dengan status PENDING_SYNC langsung via DAO
        val pendingDrafts = drafDao.getDraftsByStatus("PENDING_SYNC").first()

        for (draft in pendingDrafts) {
            val result = syncSingleDraft(draft.id)
            results.add(result)
        }
        return results
    }

    /**
     * Sinkronisasi satu draf: kompres foto → upload foto → submit inspeksi.
     */
    suspend fun syncSingleDraft(draftId: Long): SyncResult = try {
            // 1. Ambil payload draf
            val payload = inspectionRepository.preparePayload(draftId)
                ?: return SyncResult(draftId, false, "Draf tidak ditemukan")

            // 2. Kumpulkan semua foto dari semua item → kompres → upload
            // REVIEW-FIX: dua mapping terpisah:
            //  - uploadedNames: original fotoPath → serverFileName — untuk membangun DetailSubmit
            //  - compressedByServer: serverFileName → compressResultPath — untuk dipindah ke
            //    photos_sent (ADR-0016). compress() mengembalikan path di cacheDir/compressed_photos
            //    ATAU original path bila file sudah ≤300KB (tidak dikompres). JANGAN panggil
            //    compress() ulang — setiap panggilan membuat file temp baru.
            val uploadedNames = mutableMapOf<String, String>()
            val compressedByServer = mutableMapOf<String, String>()

            for (item in payload.items) {
                for (fotoPath in item.fotoPaths) {
                    val compressedPath = imageCompressor.compress(fotoPath)
                    val file = File(compressedPath)
                    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val multipart = MultipartBody.Part.createFormData("file", file.name, requestBody)
                    val uploadResponse = syncApi.uploadPhoto(multipart)
                    uploadedNames[fotoPath] = uploadResponse.fileName
                    compressedByServer[uploadResponse.fileName] = compressedPath
                }
            }

            // 3. Kirim inspection JSON
            val details = payload.items.map { item ->
                val serverFileNames = item.fotoPaths.mapNotNull { uploadedNames[it] }
                DetailSubmit(
                    itemId = item.itemId,
                    score = item.skor,
                    photos = serverFileNames.mapIndexed { i, name ->
                        PhotoSubmit(fileName = name, sortOrder = i)
                    }
                )
            }

            val submitRequest = InspectionSubmit(
                roomId = payload.roomId,
                localTimestamp = payload.localTimestamp,
                businessDate = payload.businessDate,
                details = details
            )

            // Submit returns InspectionOutDto now
            val response = syncApi.submitInspection(submitRequest)

            // ADR-0016: pindahkan file terkompresi (byte-identik server) ke photos_sent,
            // nama = nama file server. Linking key: server photo id ↔ localPath dikirim ke
            // cacheInspection sebagai map — cacheInspection dipanggil SEBELUM deleteSyncedDraft.
            val photoLocalPaths = buildPhotoLocalPaths(compressedByServer, response)

            // Simpan hasil submit ke history cache (dengan localPath backup)
            runCatching { inspectionHistoryRepository.cacheInspection(response, photoLocalPaths) }

            // 5. Sukses — atomic: update status + hapus draf + file foto lokal (asli 3-5MB)
            inspectionRepository.deleteSyncedDraft(draftId)
            SyncResult(draftId, true, "Inspeksi berhasil dikirim (ID: ${response.id})")
        } catch (e: Exception) {
            val msg = e.message ?: "Error sinkronisasi"
            // ponytail: simple error handling; expand if 409/413 handling needed
            if (msg.contains("DUPLICATE_INSPECTION") || msg.contains("409")) {
                // Already synced — skip (hapus baris + file foto lokal)
                inspectionRepository.deleteSyncedDraft(draftId)
                SyncResult(draftId, true, "Inspeksi sudah terkirim (duplicate)")
            } else {
                SyncResult(draftId, false, msg)
            }
        }

    /**
     * Pindahkan file terkompresi ke `photos_sent` dan bangun peta server photo id → localPath.
     *
     * REVIEW-FIX (linking key): cacheInspection hanya menerima InspectionOutDto (server photo
     * ids, tanpa localPath). Koneksi server photo id ↔ file lokal dibuat DI SINI dengan
     * mencocokkan photoFileName pada response (nama file server yang sama dengan hasil upload)
     * terhadap file yang dipindah — lalu map serverPhotoId → localPath dikirim ke cacheInspection.
     */
    private fun buildPhotoLocalPaths(
        compressedByServer: Map<String, String>, // serverFileName → compressResultPath
        response: InspectionOutDto
    ): Map<Long, String> {
        if (compressedByServer.isEmpty()) return emptyMap()

        // serverFileName → localPath setelah dipindah ke photos_sent
        val serverToLocal = sentPhotoStorage.moveToSent(compressedByServer)

        // server photo id → localPath, via photoFileName yang ada di response
        return buildMap {
            response.details.forEach { detail ->
                detail.photos.forEach { photo ->
                    serverToLocal[photo.photoFileName]?.let { put(photo.id, it) }
                }
            }
        }
    }
}
