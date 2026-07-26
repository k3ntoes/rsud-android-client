package my.id.kentoes.rsudajibarangapp.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.model.ApiResponse
import my.id.kentoes.rsudajibarangapp.inspection.InspectionPayload
import my.id.kentoes.rsudajibarangapp.inspection.InspectionRepository
import my.id.kentoes.rsudajibarangapp.inspection.PayloadItem
import my.id.kentoes.rsudajibarangapp.sync.api.SubmitInspectionRequest
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi
import my.id.kentoes.rsudajibarangapp.sync.api.UploadPhotoResponse
import okhttp3.MultipartBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class SyncManagerTest {

    private lateinit var inspectionRepository: InspectionRepository
    private lateinit var drafDao: DrafDao
    private lateinit var syncApi: SyncApi
    private lateinit var imageCompressor: ImageCompressor
    private lateinit var syncManager: SyncManager

    private val sampleDraft = DrafInspeksi(
        id = 1,
        roomId = 10,
        localTimestamp = "2026-01-01T00:00:00Z",
        status = "PENDING_SYNC"
    )

    private val samplePayload = InspectionPayload(
        roomId = 10,
        localTimestamp = "2026-01-01T00:00:00Z",
        items = listOf(
            PayloadItem(
                itemId = 1,
                skor = 2,
                catatan = "OK",
                fotoPaths = listOf("/photo/a.jpg")
            ),
            PayloadItem(
                itemId = 2,
                skor = 0,
                catatan = null,
                fotoPaths = listOf("/photo/b.jpg", "/photo/c.jpg")
            )
        )
    )

    @Before
    fun setup() {
        inspectionRepository = mockk()
        drafDao = mockk()
        syncApi = mockk()
        imageCompressor = mockk()
        syncManager = SyncManager(inspectionRepository, drafDao, syncApi, imageCompressor)
    }

    // ── syncAllPending ──

    @Test
    fun `syncAllPending returns empty list when no pending drafts`() = runTest {
        coEvery { drafDao.getDraftsByStatus("PENDING_SYNC") } returns flowOf(emptyList())

        val results = syncManager.syncAllPending()

        assertTrue(results.isEmpty())
    }

    @Test
    fun `syncAllPending syncs each pending draft`() = runTest {
        coEvery { drafDao.getDraftsByStatus("PENDING_SYNC") } returns flowOf(
            listOf(sampleDraft)
        )
        // syncSingleDraft will be called — mock its dependencies
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns ApiResponse(
            success = true,
            data = UploadPhotoResponse("server_a.jpg")
        )
        coEvery { syncApi.submitInspection(any()) } returns ApiResponse(success = true)
        coEvery { inspectionRepository.updateStatus(any(), any()) } returns Unit
        coEvery { inspectionRepository.deleteDraft(any()) } returns Unit

        val results = syncManager.syncAllPending()

        assertEquals(1, results.size)
        assertTrue(results[0].success)
        coVerify(exactly = 1) { drafDao.getDraftsByStatus("PENDING_SYNC") }
    }

    // ── syncSingleDraft — success ──

    @Test
    fun `syncSingleDraft returns full success flow`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress("/photo/a.jpg") } returns "/compressed/a.jpg"
        coEvery { imageCompressor.compress("/photo/b.jpg") } returns "/compressed/b.jpg"
        coEvery { imageCompressor.compress("/photo/c.jpg") } returns "/compressed/c.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns ApiResponse(
            success = true,
            data = UploadPhotoResponse("server_file.jpg")
        )
        coEvery { syncApi.submitInspection(any()) } returns ApiResponse(success = true)
        coEvery { inspectionRepository.updateStatus(1L, "SYNCED") } returns Unit
        coEvery { inspectionRepository.deleteDraft(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
        assertEquals(1L, result.draftId)
        assertEquals("Inspeksi berhasil dikirim", result.message)

        // Verifikasi foto dikompres & diupload
        coVerify(exactly = 3) { imageCompressor.compress(any()) }
        coVerify(exactly = 3) { syncApi.uploadPhoto(any()) }

        // Verifikasi submit request memiliki fotoFiles
        coVerify {
            syncApi.submitInspection(match { request ->
                request.items.size == 2 &&
                    request.items[0].fotoFiles.size == 1 && // 1 foto diupload
                    request.items[0].itemId == 1L
            })
        }

        // Verifikasi status update & delete
        coVerify(exactly = 1) { inspectionRepository.updateStatus(1L, "SYNCED") }
        coVerify(exactly = 1) { inspectionRepository.deleteDraft(1L) }
    }

    @Test
    fun `syncSingleDraft returns error when preparePayload returns null`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns null

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Draf tidak ditemukan", result.message)
        coVerify(exactly = 0) { imageCompressor.compress(any()) }
        coVerify(exactly = 0) { syncApi.uploadPhoto(any()) }
    }

    @Test
    fun `syncSingleDraft handles upload failure gracefully`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"

        // Upload gagal — return success:false, data:null
        coEvery { syncApi.uploadPhoto(any()) } returns ApiResponse(
            success = false,
            data = null
        )
        coEvery { syncApi.submitInspection(any()) } returns ApiResponse(success = true)
        coEvery { inspectionRepository.updateStatus(1L, "SYNCED") } returns Unit
        coEvery { inspectionRepository.deleteDraft(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)

        // Submit tetap dipanggil dengan fotoFiles kosong (karena upload gagal)
        coVerify {
            syncApi.submitInspection(match { request ->
                request.items.all { item -> item.fotoFiles.isEmpty() }
            })
        }
    }

    @Test
    fun `syncSingleDraft returns error when submit fails`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns ApiResponse(
            success = true,
            data = UploadPhotoResponse("server.jpg")
        )
        coEvery { syncApi.submitInspection(any()) } returns ApiResponse(
            success = false,
            message = "Validation error"
        )

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Validation error", result.message)

        // Status update & delete TIDAK dipanggil
        coVerify(exactly = 0) { inspectionRepository.updateStatus(1L, "SYNCED") }
        coVerify(exactly = 0) { inspectionRepository.deleteDraft(1L) }
    }

    @Test
    fun `syncSingleDraft returns error with message when submit has no message`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns ApiResponse(
            success = true,
            data = UploadPhotoResponse("server.jpg")
        )
        coEvery { syncApi.submitInspection(any()) } returns ApiResponse(
            success = false,
            message = null
        )

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Gagal mengirim inspeksi", result.message)
    }

    // ── syncSingleDraft — exception handling ──

    @Test
    fun `syncSingleDraft catches exception and returns failure`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } throws RuntimeException("Network down")

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Network down", result.message)
    }

    @Test
    fun `syncSingleDraft catches exception from upload`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } throws RuntimeException("Upload timeout")

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Upload timeout", result.message)
    }

    @Test
    fun `syncSingleDraft catches exception from submit`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns ApiResponse(
            success = true,
            data = UploadPhotoResponse("server.jpg")
        )
        coEvery { syncApi.submitInspection(any()) } throws RuntimeException("Submit timeout")

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Submit timeout", result.message)
    }

    @Test
    fun `syncSingleDraft handles draft with no items`() = runTest {
        val emptyPayload = samplePayload.copy(items = emptyList())
        coEvery { inspectionRepository.preparePayload(1L) } returns emptyPayload
        coEvery { syncApi.submitInspection(any()) } returns ApiResponse(success = true)
        coEvery { inspectionRepository.updateStatus(1L, "SYNCED") } returns Unit
        coEvery { inspectionRepository.deleteDraft(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
        coVerify(exactly = 0) { imageCompressor.compress(any()) }
        coVerify(exactly = 0) { syncApi.uploadPhoto(any()) }
        coVerify(exactly = 1) { syncApi.submitInspection(any()) }
    }

    @Test
    fun `syncSingleDraft item without photos skips upload`() = runTest {
        val payloadWithoutPhotos = InspectionPayload(
            roomId = 10,
            localTimestamp = "2026-01-01T00:00:00Z",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = emptyList())
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payloadWithoutPhotos
        coEvery { syncApi.submitInspection(any()) } returns ApiResponse(success = true)
        coEvery { inspectionRepository.updateStatus(1L, "SYNCED") } returns Unit
        coEvery { inspectionRepository.deleteDraft(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
        coVerify(exactly = 0) { imageCompressor.compress(any()) }
        coVerify(exactly = 0) { syncApi.uploadPhoto(any()) }
    }

    @Test
    fun `syncSingleDraft maps multiple items and photos correctly in submit request`() = runTest {
        val multiPayload = InspectionPayload(
            roomId = 5,
            localTimestamp = "2026-06-01T10:00:00Z",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "Baik", fotoPaths = listOf("/foto1.jpg", "/foto2.jpg")),
                PayloadItem(itemId = 2, skor = 1, catatan = "Kurang", fotoPaths = listOf("/foto3.jpg")),
                PayloadItem(itemId = 3, skor = 0, catatan = null, fotoPaths = emptyList())
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns multiPayload
        coEvery { imageCompressor.compress(any()) } returnsMany listOf(
            "/compressed/foto1.jpg", "/compressed/foto2.jpg", "/compressed/foto3.jpg"
        )
        coEvery { syncApi.uploadPhoto(any()) } returns ApiResponse(
            success = true,
            data = UploadPhotoResponse("server_foto.jpg")
        )
        coEvery { syncApi.submitInspection(any()) } returns ApiResponse(success = true)
        coEvery { inspectionRepository.updateStatus(1L, "SYNCED") } returns Unit
        coEvery { inspectionRepository.deleteDraft(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)

        coVerify {
            syncApi.submitInspection(match { request ->
                request.roomId == 5L &&
                    request.localTimestamp == "2026-06-01T10:00:00Z" &&
                    request.items.size == 3 &&
                    request.items[0].itemId == 1L &&
                    request.items[0].skor == 2 &&
                    request.items[0].catatan == "Baik" &&
                    request.items[0].fotoFiles.size == 2 &&
                    request.items[2].itemId == 3L &&
                    request.items[2].fotoFiles.isEmpty()
            })
        }
    }
}
