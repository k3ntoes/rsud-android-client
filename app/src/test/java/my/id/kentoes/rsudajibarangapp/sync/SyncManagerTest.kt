package my.id.kentoes.rsudajibarangapp.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.inspection.InspectionHistoryRepository
import my.id.kentoes.rsudajibarangapp.inspection.InspectionPayload
import my.id.kentoes.rsudajibarangapp.inspection.InspectionRepository
import my.id.kentoes.rsudajibarangapp.inspection.PayloadItem
import my.id.kentoes.rsudajibarangapp.master.MasterDataRepository
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionDetailOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi
import my.id.kentoes.rsudajibarangapp.sync.api.UploadPhotoResponse
import okhttp3.MultipartBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncManagerTest {

    private lateinit var inspectionRepository: InspectionRepository
    private lateinit var inspectionHistoryRepository: InspectionHistoryRepository
    private lateinit var masterDataRepository: MasterDataRepository
    private lateinit var drafDao: DrafDao
    private lateinit var syncApi: SyncApi
    private lateinit var imageCompressor: ImageCompressor
    private lateinit var syncManager: SyncManager

    private val sampleInspectionOut = InspectionOutDto(
        id = 1,
        roomId = 10,
        inspectorId = 5,
        status = "PENDING",
        businessDate = "2026-01-01",
        localTimestamp = "2026-01-01T00:00:00Z",
        createdAt = "2026-01-01T00:00:00Z",
        details = emptyList()
    )

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
        inspectionHistoryRepository = mockk()
        masterDataRepository = mockk()
        coEvery { inspectionHistoryRepository.cacheInspection(any()) } returns Unit
        drafDao = mockk()
        syncApi = mockk()
        imageCompressor = mockk()
        syncManager = SyncManager(
            inspectionRepository,
            inspectionHistoryRepository,
            masterDataRepository,
            drafDao,
            syncApi,
            imageCompressor
        )
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
        coEvery { drafDao.getDraftsByStatus("PENDING_SYNC") } returns flowOf(listOf(sampleDraft))
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse("server_a.jpg")
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
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
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
        assertEquals(1L, result.draftId)
        assertEquals("Inspeksi berhasil dikirim (ID: 1)", result.message)

        coVerify(exactly = 3) { imageCompressor.compress(any()) }
        coVerify(exactly = 3) { syncApi.uploadPhoto(any()) }
        coVerify {
            syncApi.submitInspection(match { request ->
                request.details.size == 2 &&
                    request.details[0].photos.size == 1 &&
                    request.details[0].itemId == 1L
            })
        }
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
        coEvery { syncApi.submitInspection(any()) } throws RuntimeException("HTTP 422 Unprocessable Entity")

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("HTTP 422 Unprocessable Entity", result.message)
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
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
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
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
        coVerify(exactly = 0) { imageCompressor.compress(any()) }
        coVerify(exactly = 0) { syncApi.uploadPhoto(any()) }
    }

    @Test
    fun `syncSingleDraft sends multipart with correct field name 'file'`() = runTest {
        coEvery { inspectionRepository.preparePayload(1L) } returns samplePayload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"

        val capturedParts = mutableListOf<MultipartBody.Part>()
        coEvery { syncApi.uploadPhoto(any()) } coAnswers {
            capturedParts.add(firstArg())
            UploadPhotoResponse("server_file.jpg")
        }
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        syncManager.syncSingleDraft(1L)

        val capturedPart = capturedParts.first()
        val disposition = capturedPart.headers?.get("Content-Disposition")
        assertNotNull("Multipart part must have Content-Disposition header", disposition)
        assertTrue(
            "Multipart form field must be named 'file' but got: $disposition",
            disposition!!.contains("name=\"file\"")
        )
    }

    // ── Edge cases — partial upload ──

    @Test
    fun `syncSingleDraft partial upload failure fails the entire draft`() = runTest {
        val payload = InspectionPayload(
            roomId = 10,
            localTimestamp = "2026-01-01T00:00:00Z",
            businessDate = "2026-01-01",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = listOf("/photo/a.jpg", "/photo/b.jpg"))
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payload
        coEvery { imageCompressor.compress("/photo/a.jpg") } returns "/compressed/a.jpg"
        coEvery { imageCompressor.compress("/photo/b.jpg") } returns "/compressed/b.jpg"

        var uploadCall = 0
        coEvery { syncApi.uploadPhoto(any()) } coAnswers {
            uploadCall++
            if (uploadCall == 1) UploadPhotoResponse("server_a.jpg")
            else throw RuntimeException("Upload gagal pada foto kedua")
        }

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Upload gagal pada foto kedua", result.message)
        coVerify(exactly = 0) { drafDao.markSyncedAndDelete(any()) }
        coVerify(exactly = 0) { syncApi.submitInspection(any()) }
    }

    @Test
    fun `syncSingleDraft partial upload failure on second item fails the entire draft`() = runTest {
        val payload = InspectionPayload(
            roomId = 10,
            localTimestamp = "2026-01-01T00:00:00Z",
            businessDate = "2026-01-01",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = listOf("/photo/a.jpg")),
                PayloadItem(itemId = 2, skor = 1, catatan = null, fotoPaths = listOf("/photo/b.jpg"))
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payload
        coEvery { imageCompressor.compress("/photo/a.jpg") } returns "/compressed/a.jpg"
        coEvery { imageCompressor.compress("/photo/b.jpg") } returns "/compressed/b.jpg"

        var uploadCall = 0
        coEvery { syncApi.uploadPhoto(any()) } coAnswers {
            uploadCall++
            if (uploadCall == 1) UploadPhotoResponse("server_a.jpg")
            else throw RuntimeException("Gagal upload foto item 2")
        }

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Gagal upload foto item 2", result.message)
        coVerify(exactly = 0) { drafDao.markSyncedAndDelete(any()) }
        coVerify(exactly = 0) { syncApi.submitInspection(any()) }
    }

    // ── Edge cases — ImageCompressor ──

    @Test
    fun `syncSingleDraft compressor returns original path still uploads`() = runTest {
        val payload = InspectionPayload(
            roomId = 10,
            localTimestamp = "2026-01-01T00:00:00Z",
            businessDate = "2026-01-01",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = listOf("/photo/small.jpg"))
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payload
        coEvery { imageCompressor.compress("/photo/small.jpg") } returns "/photo/small.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse("server_small.jpg")
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
        coVerify(exactly = 1) { syncApi.uploadPhoto(any()) }
        coVerify(exactly = 1) { syncApi.submitInspection(any()) }
    }

    @Test
    fun `syncSingleDraft compressor exception fails the draft`() = runTest {
        val payload = InspectionPayload(
            roomId = 10,
            localTimestamp = "2026-01-01T00:00:00Z",
            businessDate = "2026-01-01",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = listOf("/photo/corrupted.jpg"))
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payload
        coEvery { imageCompressor.compress(any()) } throws RuntimeException("Gagal kompres: bitmap null")

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("Gagal kompres: bitmap null", result.message)
        coVerify(exactly = 0) { syncApi.uploadPhoto(any()) }
        coVerify(exactly = 0) { syncApi.submitInspection(any()) }
        coVerify(exactly = 0) { drafDao.markSyncedAndDelete(any()) }
    }

    @Test
    fun `syncSingleDraft compressor exception on second photo does not submit`() = runTest {
        val payload = InspectionPayload(
            roomId = 10,
            localTimestamp = "2026-01-01T00:00:00Z",
            businessDate = "2026-01-01",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = listOf("/photo/good.jpg", "/photo/bad.jpg"))
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payload
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse("server_good.jpg")

        var compressCall = 0
        coEvery { imageCompressor.compress(any()) } coAnswers {
            compressCall++
            if (compressCall == 1) "/compressed/good.jpg"
            else throw RuntimeException("File tidak ditemukan")
        }

        val result = syncManager.syncSingleDraft(1L)

        assertFalse(result.success)
        assertEquals("File tidak ditemukan", result.message)
        coVerify(exactly = 1) { syncApi.uploadPhoto(any()) }
        coVerify(exactly = 0) { syncApi.submitInspection(any()) }
    }

    // ── Edge cases — upload response ──

    @Test
    fun `syncSingleDraft upload returns empty fileName is passed to submit`() = runTest {
        val payload = InspectionPayload(
            roomId = 10,
            localTimestamp = "2026-01-01T00:00:00Z",
            businessDate = "2026-01-01",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = listOf("/photo/a.jpg"))
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse(fileName = "")
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
        coVerify {
            syncApi.submitInspection(match { request ->
                request.details[0].photos.size == 1 &&
                    request.details[0].photos[0].fileName == ""
            })
        }
    }

    @Test
    fun `syncSingleDraft upload returns null thumbnail does not affect flow`() = runTest {
        val payload = InspectionPayload(
            roomId = 10,
            localTimestamp = "2026-01-01T00:00:00Z",
            businessDate = "2026-01-01",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = listOf("/photo/a.jpg"))
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payload
        coEvery { imageCompressor.compress(any()) } returns "/compressed/a.jpg"
        coEvery { syncApi.uploadPhoto(any()) } returns UploadPhotoResponse(fileName = "server.jpg", thumbnailName = null)
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
    }

    // ── Edge cases — syncAllPending mixed results ──

    @Test
    fun `syncAllPending multiple drafts mixed success and failure`() = runTest {
        val draft1 = DrafInspeksi(id = 1, roomId = 10, localTimestamp = "2026-01-01T00:00:00Z", status = "PENDING_SYNC")
        val draft2 = DrafInspeksi(id = 2, roomId = 20, localTimestamp = "2026-01-02T00:00:00Z", status = "PENDING_SYNC")

        coEvery { drafDao.getDraftsByStatus("PENDING_SYNC") } returns flowOf(listOf(draft1, draft2))

        coEvery { inspectionRepository.preparePayload(1L) } returns InspectionPayload(
            roomId = 10, localTimestamp = "2026-01-01T00:00:00Z", businessDate = "2026-01-01",
            items = listOf(PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = emptyList()))
        )
        coEvery { inspectionRepository.preparePayload(2L) } returns null

        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val results = syncManager.syncAllPending()

        assertEquals(2, results.size)
        assertTrue(results[0].success)
        assertEquals(1L, results[0].draftId)
        assertFalse(results[1].success)
        assertEquals(2L, results[1].draftId)
        assertEquals("Draf tidak ditemukan", results[1].message)
    }

    @Test
    fun `syncAllPending mixed upload failures`() = runTest {
        val draft1 = DrafInspeksi(id = 1, roomId = 10, localTimestamp = "2026-01-01T00:00:00Z", status = "PENDING_SYNC")
        val draft2 = DrafInspeksi(id = 2, roomId = 20, localTimestamp = "2026-01-02T00:00:00Z", status = "PENDING_SYNC")

        coEvery { drafDao.getDraftsByStatus("PENDING_SYNC") } returns flowOf(listOf(draft1, draft2))

        coEvery { inspectionRepository.preparePayload(1L) } returns InspectionPayload(
            roomId = 10, localTimestamp = "2026-01-01T00:00:00Z", businessDate = "2026-01-01",
            items = listOf(PayloadItem(itemId = 1, skor = 2, catatan = "OK", fotoPaths = emptyList()))
        )
        coEvery { inspectionRepository.preparePayload(2L) } returns InspectionPayload(
            roomId = 20, localTimestamp = "2026-01-02T00:00:00Z", businessDate = "2026-01-02",
            items = listOf(PayloadItem(itemId = 2, skor = 0, catatan = null, fotoPaths = listOf("/photo/fail.jpg")))
        )
        coEvery { imageCompressor.compress(any()) } returns "/compressed/fail.jpg"
        coEvery { syncApi.uploadPhoto(any()) } throws RuntimeException("Upload error")

        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val results = syncManager.syncAllPending()

        assertEquals(2, results.size)
        assertTrue(results[0].success)
        assertFalse(results[1].success)
        assertEquals("Upload error", results[1].message)
    }

    // ── Edge cases — multi-item with mixed foto paths ──

    @Test
    fun `syncSingleDraft mixed fotoPaths across items includes empty server names`() = runTest {
        val payload = InspectionPayload(
            roomId = 5,
            localTimestamp = "2026-06-01T10:00:00Z",
            businessDate = "2026-06-01",
            items = listOf(
                PayloadItem(itemId = 1, skor = 2, catatan = "Baik", fotoPaths = listOf("/foto1.jpg", "/foto2.jpg")),
                PayloadItem(itemId = 2, skor = 1, catatan = null, fotoPaths = listOf("/foto3.jpg")),
                PayloadItem(itemId = 3, skor = 0, catatan = null, fotoPaths = emptyList())
            )
        )
        coEvery { inspectionRepository.preparePayload(1L) } returns payload
        coEvery { imageCompressor.compress(any()) } returnsMany listOf(
            "/compressed/foto1.jpg", "/compressed/foto2.jpg", "/compressed/foto3.jpg"
        )
        coEvery { syncApi.uploadPhoto(any()) } returnsMany listOf(
            UploadPhotoResponse("server_foto1.jpg"),
            UploadPhotoResponse(fileName = ""),
            UploadPhotoResponse("server_foto3.jpg")
        )
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
        coEvery { drafDao.markSyncedAndDelete(1L) } returns Unit

        val result = syncManager.syncSingleDraft(1L)

        assertTrue(result.success)
        coVerify {
            syncApi.submitInspection(match { request ->
                request.details[0].photos.size == 2 &&
                    request.details[0].photos[0].fileName == "server_foto1.jpg" &&
                    request.details[0].photos[1].fileName == "" &&
                    request.details[1].photos.size == 1 &&
                    request.details[1].photos[0].fileName == "server_foto3.jpg" &&
                    request.details[2].photos.isEmpty()
            })
        }
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
        coEvery { syncApi.submitInspection(any()) } returns sampleInspectionOut
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
