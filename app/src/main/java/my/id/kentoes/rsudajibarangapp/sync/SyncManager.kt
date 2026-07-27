package my.id.kentoes.rsudajibarangapp.sync

import kotlinx.coroutines.flow.first
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.inspection.InspectionRepository
import my.id.kentoes.rsudajibarangapp.sync.api.DetailSubmit
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

@Singleton
class SyncManager @Inject constructor(
    private val inspectionRepository: InspectionRepository,
    private val drafDao: DrafDao,
    private val syncApi: SyncApi,
    private val imageCompressor: ImageCompressor
) {

    /**
     * Sinkronisasi semua draf dengan status PENDING_SYNC.
     * Dipanggil oleh SyncWorker.
     */
    suspend fun syncAllPending(): List<SyncResult> {
        val results = mutableListOf<SyncResult>()
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
            val fotoFileNames = mutableListOf<Pair<String, String>>() // (localPath → serverFileName)

            for (item in payload.items) {
                for (fotoPath in item.fotoPaths) {
                    // Kompres
                    val compressedPath = imageCompressor.compress(fotoPath)
                    val file = File(compressedPath)

                    // Upload via Multipart
                    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val multipart = MultipartBody.Part.createFormData("photo", file.name, requestBody)
                    val uploadResponse = syncApi.uploadPhoto(multipart)
                    fotoFileNames.add(fotoPath to uploadResponse.fileName)
                }
            }

            // 3. Buat serverFileName map dari hasil upload
            val uploadedNames = fotoFileNames.associate { (local, server) -> local to server }

            // 4. Kirim inspection JSON (BE langsung return 201 tanpa wrapper)
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

            syncApi.submitInspection(submitRequest)

            // 5. Sukses — atomic: update status + hapus draf (cegah ghost SYNCED entry)
            drafDao.markSyncedAndDelete(draftId)
            SyncResult(draftId, true, "Inspeksi berhasil dikirim")
        } catch (e: Exception) {
            SyncResult(draftId, false, e.message ?: "Error sinkronisasi")
        }
}
