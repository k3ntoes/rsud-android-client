package my.id.kentoes.rsudajibarangapp.inspection

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionDetailEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionPhotoEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.core.model.PaginatedResponse
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionDetailOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionListItemDto
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionOutDto
import my.id.kentoes.rsudajibarangapp.sync.SentPhotoStorage
import my.id.kentoes.rsudajibarangapp.sync.api.PhotoOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class InspectionHistoryRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var syncApi: SyncApi
    private lateinit var masterDataDao: MasterDataDao
    private lateinit var sentPhotoStorage: SentPhotoStorage
    private lateinit var repository: InspectionHistoryRepository

    private val sampleRoom = RuangEntity(id = 1, nama = "Ruang A", lantai = "Lantai 1")
    private val sampleRooms = listOf(sampleRoom)

    private val sampleInspectionEntity = InspectionEntity(
        id = 100, roomId = 1, inspectorId = 5, status = "APPROVED",
        businessDate = "2026-07-28", createdAt = "2026-07-28T10:00:00Z"
    )

    @Before
    fun setup() {
        syncApi = mockk()
        masterDataDao = mockk()
        sentPhotoStorage = mockk()
        every { masterDataDao.getAllRooms() } returns flowOf(sampleRooms)
        coEvery { masterDataDao.getAllRoomsOnce() } returns sampleRooms
        repository = InspectionHistoryRepository(syncApi, masterDataDao, sentPhotoStorage)
    }

    // ── observeLocalInspections ──

    @Test
    fun `observeLocalInspections returns Flow that maps entities to items`() = runTest {
        every { masterDataDao.getAllInspections() } returns flowOf(listOf(sampleInspectionEntity))

        val items = repository.observeLocalInspections().first()

        assertEquals(1, items.size)
        assertEquals(100L, items[0].id)
        assertEquals("Ruang A", items[0].roomName)
        assertEquals("APPROVED", items[0].status)
        assertEquals("2026-07-28", items[0].businessDate)
    }

    @Test
    fun `observeLocalInspections filters by status when provided`() = runTest {
        every { masterDataDao.getInspectionsByStatus("APPROVED") } returns flowOf(listOf(sampleInspectionEntity))

        val items = repository.observeLocalInspections(status = "APPROVED").first()

        assertEquals(1, items.size)
        assertEquals("APPROVED", items[0].status)
    }

    @Test
    fun `observeLocalInspections uses room name from cache`() = runTest {
        val entity = sampleInspectionEntity.copy(roomId = 2) // roomId 2, not in sampleRooms
        every { masterDataDao.getAllInspections() } returns flowOf(listOf(entity))

        val items = repository.observeLocalInspections().first()

        assertEquals(1, items.size)
        assertEquals("Room #2", items[0].roomName) // fallback when room not found
    }

    @Test
    fun `observeLocalInspections returns empty flow when no inspections`() = runTest {
        every { masterDataDao.getAllInspections() } returns flowOf(emptyList())

        val items = repository.observeLocalInspections().first()

        assertTrue(items.isEmpty())
    }

    @Test
    fun `observeLocalInspections maps room name correctly from local cache`() = runTest {
        every { masterDataDao.getInspectionsByStatus("REJECTED") } returns flowOf(listOf(sampleInspectionEntity))

        val items = repository.observeLocalInspections(status = "REJECTED").first()

        assertEquals(1, items.size)
        assertEquals("Ruang A", items[0].roomName)
        assertEquals("APPROVED", items[0].status) // entity status, not filter status
    }

    // ── fetchInspections ──

    @Test
    fun `fetchInspections fetches from API and maps to items`() = runTest {
        val apiResponse = PaginatedResponse(
            items = listOf(
                InspectionListItemDto(id = 1, roomId = 1, inspectorId = 5, status = "PENDING", businessDate = "2026-07-28", createdAt = "2026-07-28T10:00:00Z", detailCount = 3)
            ),
            total = 1,
            page = 1,
            perPage = 20,
            totalPages = 1
        )
        coEvery { syncApi.getInspections(any(), any(), any()) } returns apiResponse
        every { masterDataDao.getAllRooms() } returns flowOf(sampleRooms)
        coEvery { masterDataDao.insertInspection(any()) } returns Unit

        val result = repository.fetchInspections(page = 1, perPage = 20)

        assertEquals(1, result.items.size)
        assertEquals(1L, result.items[0].id)
        assertEquals("Ruang A", result.items[0].roomName)
        assertEquals(3, result.items[0].detailCount)
        assertEquals(1, result.currentPage)
        coVerify(exactly = 1) { syncApi.getInspections(1, 20, null) }
        coVerify(exactly = 1) { masterDataDao.insertInspection(any()) }
    }

    @Test
    fun `fetchInspections passes status filter to API`() = runTest {
        coEvery { syncApi.getInspections(any(), any(), any()) } returns PaginatedResponse(items = emptyList())
        every { masterDataDao.getAllRooms() } returns flowOf(sampleRooms)

        repository.fetchInspections(page = 1, perPage = 10, status = "APPROVED")

        coVerify { syncApi.getInspections(1, 10, "APPROVED") }
    }

    @Test
    fun `fetchInspections returns empty result when API returns empty`() = runTest {
        coEvery { syncApi.getInspections(any(), any(), any()) } returns PaginatedResponse(items = emptyList())
        every { masterDataDao.getAllRooms() } returns flowOf(sampleRooms)

        val result = repository.fetchInspections()

        assertTrue(result.items.isEmpty())
        assertEquals(1, result.currentPage)
    }

    @Test
    fun `fetchInspections uses empty string fallback for null room name`() = runTest {
        val apiItems = PaginatedResponse(
            items = listOf(
                InspectionListItemDto(id = 2, roomId = 99, inspectorId = 1, status = "PENDING", businessDate = "2026-07-28", createdAt = null, detailCount = 0)
            ),
            total = 1,
            page = 1,
            perPage = 20,
            totalPages = 1
        )
        coEvery { syncApi.getInspections(any(), any(), any()) } returns apiItems
        every { masterDataDao.getAllRooms() } returns flowOf(emptyList())
        coEvery { masterDataDao.insertInspection(any()) } returns Unit

        val result = repository.fetchInspections()

        assertEquals("Room #99", result.items[0].roomName) // fallback
    }

    // ── fetchDetail ──

    @Test
    fun `fetchDetail returns detail from API`() = runTest {
        val detail = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            details = listOf(
                InspectionDetailOutDto(id = 10, itemId = 1, itemNameSnapshot = "Meja", score = 2,
                    photos = listOf(PhotoOutDto(id = 100, photoFileName = "photo.jpg", thumbnailFileName = null, sortOrder = 0)))
            )
        )
        coEvery { syncApi.getInspectionDetail(1L) } returns detail

        val result = repository.fetchDetail(1L)

        assertNotNull(result)
        assertEquals(1L, result!!.id)
        assertEquals(1, result.details.size)
        assertEquals("Meja", result.details[0].itemNameSnapshot)
    }

    @Test
    fun `fetchDetail returns null when API throws`() = runTest {
        coEvery { syncApi.getInspectionDetail(any()) } throws RuntimeException("Network error")

        val result = repository.fetchDetail(1L)

        assertNull(result)
    }

    @Test
    fun `fetchDetail returns null when detail not found`() = runTest {
        coEvery { syncApi.getInspectionDetail(999L) } throws RuntimeException("Not found")

        val result = repository.fetchDetail(999L)

        assertNull(result)
    }

    // ── cacheInspection ──

    @Test
    fun `cacheInspection saves inspection entity`() = runTest {
        val dto = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            rejectionReason = null, details = emptyList()
        )
        coEvery { masterDataDao.insertInspection(any()) } returns Unit

        repository.cacheInspection(dto)

        coVerify {
            masterDataDao.insertInspection(match { it.id == 1L && it.status == "APPROVED" })
        }
    }

    @Test
    fun `cacheInspection saves details and photos`() = runTest {
        val dto = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            rejectionReason = null,
            details = listOf(
                InspectionDetailOutDto(id = 10, itemId = 1, itemNameSnapshot = "Meja", score = 2,
                    photos = listOf(
                        PhotoOutDto(id = 100, photoFileName = "foto1.jpg", thumbnailFileName = "thumb1.jpg", sortOrder = 0),
                        PhotoOutDto(id = 101, photoFileName = "foto2.jpg", thumbnailFileName = null, sortOrder = 1)
                    )
                )
            )
        )
        coEvery { masterDataDao.insertInspection(any()) } returns Unit
        coEvery { masterDataDao.insertDetails(any()) } returns Unit
        coEvery { masterDataDao.insertPhotos(any()) } returns Unit

        repository.cacheInspection(dto)

        coVerify { masterDataDao.insertDetails(match { it.size == 1 && it[0].itemNameSnapshot == "Meja" }) }
        coVerify { masterDataDao.insertPhotos(match { it.size == 2 }) }
    }

    @Test
    fun `cacheInspection handles rejection reason`() = runTest {
        val dto = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "REJECTED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            rejectionReason = "Foto tidak jelas", details = emptyList()
        )
        coEvery { masterDataDao.insertInspection(any()) } returns Unit

        repository.cacheInspection(dto)

        coVerify {
            masterDataDao.insertInspection(match {
                it.rejectionReason == "Foto tidak jelas"
            })
        }
    }

    @Test
    fun `cacheInspection does not save details or photos when empty`() = runTest {
        val dto = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            details = emptyList()
        )
        coEvery { masterDataDao.insertInspection(any()) } returns Unit

        repository.cacheInspection(dto)

        coVerify(exactly = 0) { masterDataDao.insertDetails(any()) }
        coVerify(exactly = 0) { masterDataDao.insertPhotos(any()) }
    }

    @Test
    fun `cacheInspection saves photos per detail`() = runTest {
        val dto = InspectionOutDto(
            id = 2, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            details = listOf(
                InspectionDetailOutDto(id = 10, itemId = 1, itemNameSnapshot = "Meja", score = 2, photos = emptyList()),
                InspectionDetailOutDto(id = 20, itemId = 2, itemNameSnapshot = "Kursi", score = 0,
                    photos = listOf(PhotoOutDto(id = 200, photoFileName = "rusak.jpg", thumbnailFileName = null, sortOrder = 0)))
            )
        )
        coEvery { masterDataDao.insertInspection(any()) } returns Unit
        coEvery { masterDataDao.insertDetails(any()) } returns Unit
        coEvery { masterDataDao.insertPhotos(any()) } returns Unit

        repository.cacheInspection(dto)

        // Only detail with photos triggers insertPhotos
        coVerify(exactly = 1) { masterDataDao.insertPhotos(match { it.size == 1 }) }
    }

    @Test
    fun `cacheInspection saves photoLocalPaths into entities`() = runTest {
        val dto = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            details = listOf(
                InspectionDetailOutDto(id = 10, itemId = 1, itemNameSnapshot = "Meja", score = 2,
                    photos = listOf(PhotoOutDto(id = 100, photoFileName = "foto1.jpg", thumbnailFileName = null, sortOrder = 0))
                )
            )
        )
        coEvery { masterDataDao.insertInspection(any()) } returns Unit
        coEvery { masterDataDao.insertDetails(any()) } returns Unit
        coEvery { masterDataDao.insertPhotos(any()) } returns Unit

        repository.cacheInspection(dto, photoLocalPaths = mapOf(100L to "/sent/foto1.jpg"))

        coVerify {
            masterDataDao.insertPhotos(match {
                it.size == 1 && it[0].localPath == "/sent/foto1.jpg"
            })
        }
    }

    @Test
    fun `cacheInspection leaves localPath null when map missing`() = runTest {
        val dto = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            details = listOf(
                InspectionDetailOutDto(id = 10, itemId = 1, itemNameSnapshot = "Meja", score = 2,
                    photos = listOf(PhotoOutDto(id = 100, photoFileName = "foto1.jpg", thumbnailFileName = null, sortOrder = 0))
                )
            )
        )
        coEvery { masterDataDao.insertInspection(any()) } returns Unit
        coEvery { masterDataDao.insertDetails(any()) } returns Unit
        coEvery { masterDataDao.insertPhotos(any()) } returns Unit

        repository.cacheInspection(dto)

        coVerify {
            masterDataDao.insertPhotos(match { it.size == 1 && it[0].localPath == null })
        }
    }

    // ── cacheDuplicateInspection (ADR-0018 Q1: 409 juga menulis cache riwayat) ──

    @Test
    fun `cacheDuplicateInspection finds by roomId and businessDate then caches detail`() = runTest {
        // List endpoint TIDAK memuat local_timestamp — cocokkan roomId + businessDate
        coEvery { syncApi.getInspections(any(), any(), any()) } returns PaginatedResponse(
            items = listOf(
                InspectionListItemDto(id = 7, roomId = 1, inspectorId = 5, status = "PENDING", businessDate = "2026-07-28", createdAt = "2026-07-28T10:00:00Z"),
                InspectionListItemDto(id = 9, roomId = 1, inspectorId = 5, status = "PENDING", businessDate = "2026-07-28", createdAt = "2026-07-28T11:00:00Z"),
                InspectionListItemDto(id = 8, roomId = 2, inspectorId = 5, status = "PENDING", businessDate = "2026-07-28", createdAt = "2026-07-28T10:30:00Z") // room beda — bukan kandidat
            ),
            total = 3, page = 1, perPage = 20, totalPages = 1
        )
        val detail = InspectionOutDto(
            id = 9, roomId = 1, inspectorId = 5, status = "PENDING",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T11:00:00Z", createdAt = "2026-07-28T11:00:00Z",
            details = emptyList()
        )
        coEvery { syncApi.getInspectionDetail(9L) } returns detail
        coEvery { masterDataDao.insertInspection(any()) } returns Unit

        repository.cacheDuplicateInspection(roomId = 1, businessDate = "2026-07-28")

        // Kandidat TERBARU (id 9) dipilih, bukan id 7
        coVerify(exactly = 1) { syncApi.getInspectionDetail(9L) }
        coVerify(exactly = 1) { masterDataDao.insertInspection(match { it.id == 9L }) }
    }

    @Test
    fun `cacheDuplicateInspection picks newest candidate when room inspected multiple times`() = runTest {
        coEvery { syncApi.getInspections(any(), any(), any()) } returns PaginatedResponse(
            items = listOf(
                InspectionListItemDto(id = 3, roomId = 1, inspectorId = 5, status = "PENDING", businessDate = "2026-07-28", createdAt = null),
                InspectionListItemDto(id = 5, roomId = 1, inspectorId = 5, status = "PENDING", businessDate = "2026-07-28", createdAt = null),
                InspectionListItemDto(id = 4, roomId = 1, inspectorId = 5, status = "PENDING", businessDate = "2026-07-29", createdAt = null) // tanggal beda — bukan kandidat
            ),
            total = 3, page = 1, perPage = 20, totalPages = 1
        )
        val detail = InspectionOutDto(
            id = 5, roomId = 1, inspectorId = 5, status = "PENDING",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T12:00:00Z", createdAt = null,
            details = emptyList()
        )
        coEvery { syncApi.getInspectionDetail(5L) } returns detail
        coEvery { masterDataDao.insertInspection(any()) } returns Unit

        repository.cacheDuplicateInspection(roomId = 1, businessDate = "2026-07-28")

        coVerify(exactly = 1) { syncApi.getInspectionDetail(5L) }
        coVerify(exactly = 0) { syncApi.getInspectionDetail(3L) }
    }

    @Test
    fun `cacheDuplicateInspection accumulates candidates across pages and picks newest`() = runTest {
        // Page 1: kandidat lama (id 3) — Page 2: duplikat terbaru (id 9). Pemilihan
        // id terbesar harus terjadi SETELAH semua halaman dicari (bukan per-halaman).
        coEvery { syncApi.getInspections(1, 20, null) } returns PaginatedResponse(
            items = listOf(
                InspectionListItemDto(id = 3, roomId = 1, inspectorId = 5, status = "PENDING", businessDate = "2026-07-28", createdAt = null)
            ),
            total = 2, page = 1, perPage = 20, totalPages = 2
        )
        coEvery { syncApi.getInspections(2, 20, null) } returns PaginatedResponse(
            items = listOf(
                InspectionListItemDto(id = 9, roomId = 1, inspectorId = 5, status = "PENDING", businessDate = "2026-07-28", createdAt = null)
            ),
            total = 2, page = 2, perPage = 20, totalPages = 2
        )
        coEvery { syncApi.getInspections(3, 20, null) } returns PaginatedResponse(items = emptyList())
        val detail = InspectionOutDto(
            id = 9, roomId = 1, inspectorId = 5, status = "PENDING",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T11:00:00Z", createdAt = null,
            details = emptyList()
        )
        coEvery { syncApi.getInspectionDetail(9L) } returns detail
        coEvery { masterDataDao.insertInspection(any()) } returns Unit

        repository.cacheDuplicateInspection(roomId = 1, businessDate = "2026-07-28")

        // Kandidat terbaru (id 9) dari halaman 2 yang terpilih, bukan id 3 dari halaman 1
        coVerify(exactly = 1) { syncApi.getInspectionDetail(9L) }
        coVerify(exactly = 0) { syncApi.getInspectionDetail(3L) }
    }

    @Test
    fun `cacheDuplicateInspection does nothing when no match`() = runTest {
        coEvery { syncApi.getInspections(any(), any(), any()) } returns PaginatedResponse(
            items = listOf(
                InspectionListItemDto(id = 1, roomId = 99, inspectorId = 5, status = "PENDING", businessDate = "2026-07-28", createdAt = null)
            ),
            total = 1, page = 1, perPage = 20, totalPages = 1
        )

        repository.cacheDuplicateInspection(roomId = 1, businessDate = "2026-07-28")

        coVerify(exactly = 0) { syncApi.getInspectionDetail(any()) }
        coVerify(exactly = 0) { masterDataDao.insertInspection(any()) }
    }

    @Test
    fun `cacheDuplicateInspection returns silently when list endpoint fails`() = runTest {
        coEvery { syncApi.getInspections(any(), any(), any()) } throws RuntimeException("Network down")

        repository.cacheDuplicateInspection(roomId = 1, businessDate = "2026-07-28")

        coVerify(exactly = 0) { syncApi.getInspectionDetail(any()) }
        coVerify(exactly = 0) { masterDataDao.insertInspection(any()) }
    }

    // ── replacePhoto (ADR-0016 re-upload) ──

    @Test
    fun `replacePhoto uploads local file and updates photo row`() = runTest {
        val updated = PhotoOutDto(id = 100, photoFileName = "new_foto.jpg", thumbnailFileName = "new_thumb.jpg", sortOrder = 0)
        coEvery { syncApi.replacePhoto(1L, 100L, any()) } returns updated
        coEvery { sentPhotoStorage.moveToSent(any()) } returns mapOf("new_foto.jpg" to "/sent/new_foto.jpg")
        coEvery { masterDataDao.updatePhotoAfterReplace(any(), any(), any(), any()) } returns Unit
        val photo = tempFolder.newFile("backup.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val result = repository.replacePhoto(1L, 100L, photo.absolutePath)

        assertEquals("new_foto.jpg", result.photoFileName)
        coVerify { syncApi.replacePhoto(1L, 100L, any()) }
        coVerify { sentPhotoStorage.moveToSent(any()) }
        coVerify { masterDataDao.updatePhotoAfterReplace(100L, "new_foto.jpg", "new_thumb.jpg", any()) }
    }

    @Test
    fun `replacePhoto throws when local backup missing`() = runTest {
        val missing = File(tempFolder.root, "tidak_ada.jpg")

        try {
            repository.replacePhoto(1L, 100L, missing.absolutePath)
            throw AssertionError("Harusnya throw IllegalStateException")
        } catch (e: IllegalStateException) {
            assertEquals("File backup lokal tidak ditemukan", e.message)
        }
        coVerify(exactly = 0) { syncApi.replacePhoto(any(), any(), any()) }
    }

    // ── PaginatedResult ──

    @Test
    fun `fetchInspections returns PaginatedResult with correct page`() = runTest {
        coEvery { syncApi.getInspections(any(), any(), any()) } returns PaginatedResponse(items = emptyList(), page = 3, totalPages = 5)
        every { masterDataDao.getAllRooms() } returns flowOf(sampleRooms)

        val result = repository.fetchInspections(page = 3, perPage = 10)

        assertEquals(3, result.currentPage)
        assertEquals(5, result.totalPages)
        assertTrue(result.items.isEmpty())
    }
}
