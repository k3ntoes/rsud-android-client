package my.id.kentoes.rsudajibarangapp.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.inspection.InspectionRepository
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
    suspend fun syncSingleDraft(draftId: Long): SyncResult = withContext(Dispatchers.IO) {
        try {
            // 1. Ambil payload draf
            val payload = inspectionRepository.preparePayload(draftId)
                ?: return@withContext SyncResult(draftId, false, "Draf tidak ditemukan")

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

                    if (uploadResponse.success && uploadResponse.data != null) {
                        fotoFileNames.add(fotoPath to uploadResponse.data.fileName)
                    } else {
                        // Partial failure — foto ini gagal upload, skip
                        // Tapi item tetap dikirim tanpa foto ini
                    }
                }
            }

            // 3. Buat serverFileName map dari hasil upload
            val uploadedNames = fotoFileNames.associate { (local, server) -> local to server }

            // 4. Kirim inspection JSON
            val submitItems = payload.items.map { item ->
                val serverFileNames = item.fotoPaths.mapNotNull { uploadedNames[it] }
                my.id.kentoes.rsudajibarangapp.sync.api.SubmitItem(
                    itemId = item.itemId,
                    skor = item.skor,
                    catatan = item.catatan,
                    fotoFiles = serverFileNames
                )
            }

            val submitRequest = my.id.kentoes.rsudajibarangapp.sync.api.SubmitInspectionRequest(
                roomId = payload.roomId,
                localTimestamp = payload.localTimestamp,
                items = submitItems
            )

            val submitResponse = syncApi.submitInspection(submitRequest)

            if (submitResponse.success) {
                // 5. Sukses — update status & hapus draf lokal
                inspectionRepository.updateStatus(draftId, "SYNCED")
                inspectionRepository.deleteDraft(draftId)
                SyncResult(draftId, true, "Inspeksi berhasil dikirim")
            } else {
                SyncResult(draftId, false, submitResponse.message ?: "Gagal mengirim inspeksi")
            }
        } catch (e: Exception) {
            SyncResult(draftId, false, e.message ?: "Error sinkronisasi")
        }
    }
}
