package my.id.kentoes.rsudajibarangapp.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.inspection.InspectionPayload
import my.id.kentoes.rsudajibarangapp.inspection.InspectionRepository
import my.id.kentoes.rsudajibarangapp.inspection.PayloadItem
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi
import my.id.kentoes.rsudajibarangapp.sync.api.UploadPhotoResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
        businessDate = "2026-01-01",
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
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse("server_a.jpg")
        coEvery { syncApi.submitInspection(any()) } returns Unit
        coEvery { drafDao.markSyncedAndDelete(any()) } returns Unit

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
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse("server_file.jpg")
        coEvery { syncApi.submitInspection(any()) } returns Unit
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
        assertEquals(1L, result.draftId)
        assertEquals("Inspeksi berhasil dikirim", result.message)

        // Verifikasi foto dikompres & diupload
        coVerify(exactly = 3) { imageCompressor.compress(any()) }
        coVerify(exactly = 3) { syncApi.uploadPhoto(any()) }

        // Verifikasi submit request memiliki photos
        coVerify {
            syncApi.submitInspection(match { request ->
                request.details.size == 2 &&
                    request.details[0].photos.size == 1 && // 1 foto diupload
                    request.details[0].itemId == 1L
            })
        }

        // Verifikasi atomic markSyncedAndDelete dipanggil
        coVerify(exactly = 1) { drafDao.markSyncedAndDelete(1L) }
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

        // Upload throws exception — skip foto & tetap lanjut submit
        coEvery { syncApi.uploadPhoto(any()) } throws RuntimeException("Upload gagal")

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Upload gagal", result.message)
    }

    @Test
    fun `syncSingleDraft returns error when submit fails`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse("server.jpg")
        // Submit throws exception (422, 500, etc.)
        coEvery { syncApi.submitInspection(any()) } throws RuntimeException("HTTP 422 Unprocessable Entity")

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("HTTP 422 Unprocessable Entity", result.message)

        // markSyncedAndDelete TIDAK dipanggil
        coVerify(exactly = 0) { drafDao.markSyncedAndDelete(any()) }
    }

    @Test
    fun `syncSingleDraft returns error when submit throws`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse("server.jpg")
        coEvery { syncApi.submitInspection(any()) } throws RuntimeException("Gagal mengirim inspeksi")

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
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse("server.jpg")
        coEvery { syncApi.submitInspection(any()) } throws RuntimeException("Submit timeout")

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Submit timeout", result.message)
    }

    @Test
    fun `syncSingleDraft handles draft with no items`() = runTest {
        val emptyPayload = samplePayload.copy(items = emptyList())
        coEvery { inspectionRepository.preparePayload(1L) } returns emptyPayload
        coEvery { syncApi.submitInspection(any()) } returns Unit
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

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
            businessDate = "2026-01-01",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = emptyList())
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payloadWithoutPhotos
        coEvery { syncApi.submitInspection(any()) } returns Unit
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

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
            businessDate = "2026-06-01",
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
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse("server_foto.jpg")
        coEvery { syncApi.submitInspection(any()) } returns Unit
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)

        coVerify {
            syncApi.submitInspection(match { request ->
                request.roomId == 5L &&
                    request.localTimestamp == "2026-06-01T10:00:00Z" &&
                    request.businessDate == "2026-06-01" &&
                    request.details.size == 3 &&
                    request.details[0].itemId == 1L &&
                    request.details[0].score == 2 &&
                    request.details[0].photos.size == 2 &&
                    request.details[2].itemId == 3L &&
                    request.details[2].photos.isEmpty()
            })
        }
    }
}
