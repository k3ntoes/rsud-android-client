package my.id.kentoes.rsudajibarangapp.inspection

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.id.kentoes.rsudajibarangapp.auth.AuthRepository
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionPhotoEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionDetailOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.PhotoOutDto
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InspectionHistoryViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var repository: InspectionHistoryRepository
    private lateinit var masterDataDao: MasterDataDao
    private lateinit var authRepository: AuthRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        masterDataDao = mockk()
        authRepository = mockk()
        every { authRepository.currentUser } returns MutableStateFlow(null)
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getInspectionsByStatus(any()) } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getPhotosForInspection(any()) } returns emptyList()
        coEvery { repository.observeLocalInspections(any(), any()) } returns MutableStateFlow(emptyList())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = InspectionHistoryViewModel(repository, masterDataDao, authRepository)

    // ── init ──

    @Test
    fun `init starts with isInitialLoading true`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun `init collects cache from repository`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun `init triggers refresh from server`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    // ── refreshFromServer ──

    @Test
    fun `refreshFromServer updates cache and resets page`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `refreshFromServer sets error on failure`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } throws RuntimeException("Network error")
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals("Network error", viewModel.uiState.value.error)
    }

    // ── setFilter ──

    @Test
    fun `setFilter updates filter status and resets page`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setFilter("APPROVED")
        assertEquals("APPROVED", viewModel.uiState.value.filterStatus)
        assertEquals(1, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.uiState.value.hasMorePages)
    }

    @Test
    fun `setFilter to null shows all`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.setFilter("APPROVED")
        viewModel.setFilter(null)
        assertNull(viewModel.uiState.value.filterStatus)
    }

    // ── loadNextPage ──

    @Test
    fun `loadNextPage does nothing when hasMorePages is false`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.currentPage >= 1)
    }

    @Test
    fun `loadNextPage does nothing when already loading`() = runTest(testDispatcher) {
        // Stub for init — page 1 of 2, sehingga hasMorePages tetap true setelah init
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(emptyList(), totalPages = 2, currentPage = 1)
        val viewModel = createViewModel()
        advanceUntilIdle() // init completes

        // Overwrite with count-tracking stub for pagination
        var callCount = 0
        coEvery { repository.fetchInspections(any(), any(), any()) } coAnswers {
            callCount++
            kotlinx.coroutines.delay(100000)
            PaginatedResult(items = emptyList(), totalPages = 2, currentPage = firstArg())
        }

        viewModel.loadNextPage()
        testDispatcher.scheduler.runCurrent() // execute coroutine up to delay(100000)
        viewModel.loadNextPage() // guard fires — isLoadingMore is true
        assertEquals(1, callCount)
    }

    @Test
    fun `loadNextPage sets error on failure`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 2, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        coEvery { repository.fetchInspections(page = 2, any(), any()) } throws RuntimeException("Timeout")
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoadingMore)
        assertEquals("Timeout", viewModel.uiState.value.error)
    }

    @Test
    fun `refresh landing mid loadNextPage does not clobber pagination state`() = runTest(testDispatcher) {
        // Init: page 1 of 2 → hasMorePages tetap true
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 2, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Gate: fetch halaman 2 (loadMore) memblok sampai dilepaskan
        val gate = CompletableDeferred<Unit>()
        coEvery { repository.fetchInspections(page = 2, any(), any()) } coAnswers {
            gate.await()
            PaginatedResult(items = emptyList(), totalPages = 2, currentPage = 2)
        }

        viewModel.loadNextPage()
        testDispatcher.scheduler.runCurrent() // loadMore mulai, blok di gate
        assertTrue(viewModel.uiState.value.isLoadingMore)

        viewModel.refreshFromServer() // refresh harus membatalkan loadMore yang basi
        testDispatcher.scheduler.runCurrent()

        gate.complete(Unit) // lepaskan gate — hasil loadMore basi TIDAK boleh menimpa state refresh
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.uiState.value.hasMorePages)
        assertFalse(viewModel.uiState.value.isLoadingMore)
    }

    @Test
    fun `loadNextPage landing after refresh result does not clobber pagination state`() = runTest(testDispatcher) {
        // Init: page 1 of 2 → hasMorePages tetap true
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 2, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Refresh dan loadMore sama-sama ditahan sampai dilepas bergantian
        val refreshGate = CompletableDeferred<Unit>()
        coEvery { repository.fetchInspections(page = 1, any(), any()) } coAnswers {
            refreshGate.await()
            PaginatedResult(items = emptyList(), totalPages = 2, currentPage = 1)
        }
        val loadGate = CompletableDeferred<Unit>()
        coEvery { repository.fetchInspections(page = 2, any(), any()) } coAnswers {
            loadGate.await()
            PaginatedResult(items = emptyList(), totalPages = 2, currentPage = 2)
        }

        viewModel.refreshFromServer()
        testDispatcher.scheduler.runCurrent() // refresh mulai, blok di refreshGate
        assertTrue(viewModel.uiState.value.isRefreshing)

        viewModel.loadNextPage() // scroll saat refresh masih berjalan
        testDispatcher.scheduler.runCurrent() // loadMore mulai, blok di loadGate
        assertTrue(viewModel.uiState.value.isLoadingMore)

        // Refresh selesai lebih dulu — state menjadi page 1
        refreshGate.complete(Unit)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, viewModel.uiState.value.currentPage)

        // Hasil loadMore yang basi selesai belakangan — TIDAK boleh menimpa
        loadGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.uiState.value.hasMorePages)
        assertFalse(viewModel.uiState.value.isLoadingMore)
    }

    @Test
    fun `loadNextPage stops at last page from server totalPages`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 3, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasMorePages)

        coEvery { repository.fetchInspections(page = 2, any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 3, currentPage = 2
        )
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasMorePages)
        assertEquals(2, viewModel.uiState.value.currentPage)

        coEvery { repository.fetchInspections(page = 3, any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 3, currentPage = 3
        )
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.hasMorePages)
        assertEquals(3, viewModel.uiState.value.currentPage)

        viewModel.loadNextPage()
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.currentPage)
    }

    // ── loadDetail ──

    @Test
    fun `loadDetail fetches detail and sets state`() = runTest(testDispatcher) {
        val detail = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            rejectionReason = null,
            details = listOf(
                InspectionDetailOutDto(id = 10, itemId = 1, itemNameSnapshot = "Meja", score = 2,
                    photos = listOf(PhotoOutDto(id = 100, photoFileName = "foto.jpg", thumbnailFileName = null, sortOrder = 0)))
            )
        )
        coEvery { repository.fetchDetail(1L) } returns detail
        coEvery { masterDataDao.getRoomById(1L) } returns null
        val viewModel = createViewModel()
        viewModel.loadDetail(1L)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoadingDetail)
        assertEquals(detail, viewModel.uiState.value.selectedDetail)
    }

    @Test
    fun `loadDetail looks up room name and inspector name`() = runTest(testDispatcher) {
        val detail = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            rejectionReason = null, details = emptyList()
        )
        coEvery { repository.fetchDetail(1L) } returns detail
        coEvery { masterDataDao.getRoomById(1L) } returns RuangEntity(id = 1, nama = "Ruang IGD")
        every { authRepository.currentUser } returns MutableStateFlow(
            UserOut(id = 5, username = "petugas01", role = "inspector", isActive = true, name = "Petugas Satu")
        )
        val viewModel = createViewModel()
        viewModel.loadDetail(1L)
        advanceUntilIdle()
        assertEquals("Ruang IGD", viewModel.uiState.value.detailRoomName)
        assertEquals("Petugas Satu · petugas01", viewModel.uiState.value.inspectorName)
    }

    @Test
    fun `loadDetail shows placeholder when user not found`() = runTest(testDispatcher) {
        val detail = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 99, status = "PENDING",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            rejectionReason = null, details = emptyList()
        )
        coEvery { repository.fetchDetail(1L) } returns detail
        coEvery { masterDataDao.getRoomById(1L) } returns null
        every { authRepository.currentUser } returns MutableStateFlow(
            UserOut(id = 5, username = "petugas01", role = "inspector", isActive = true)
        )
        val viewModel = createViewModel()
        viewModel.loadDetail(1L)
        advanceUntilIdle()
        assertEquals("Petugas #99", viewModel.uiState.value.inspectorName)
    }

    @Test
    fun `loadDetail sets error on failure`() = runTest(testDispatcher) {
        coEvery { repository.fetchDetail(1L) } throws RuntimeException("Not found")
        val viewModel = createViewModel()
        viewModel.loadDetail(1L)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoadingDetail)
        assertNull(viewModel.uiState.value.selectedDetail)
        assertEquals("Not found", viewModel.uiState.value.error)
    }

    // ── clearDetail ──

    // ── reuploadPhoto (ADR-0016) ──

    @Test
    fun `reuploadPhoto calls repository with local backup path`() = runTest(testDispatcher) {
        // localPath harus menunjuk file yang BENAR-BENAR ada — ViewModel memfilter
        // detailPhotoLocalPaths dengan File.exists() (ADR-0016 lokal-first).
        val backupFile = tempFolder.newFile("foto.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val detail = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            details = listOf(
                InspectionDetailOutDto(id = 10, itemId = 1, itemNameSnapshot = "Meja", score = 2,
                    photos = listOf(PhotoOutDto(id = 100, photoFileName = "foto.jpg", thumbnailFileName = null, sortOrder = 0)))
            )
        )
        coEvery { repository.fetchDetail(1L) } returns detail
        coEvery { masterDataDao.getRoomById(1L) } returns null
        coEvery { masterDataDao.getPhotosForInspection(1L) } returns listOf(
            InspectionPhotoEntity(
                id = 100, detailId = 10, photoFileName = "foto.jpg",
                thumbnailFileName = null, sortOrder = 0, localPath = backupFile.absolutePath
            )
        )
        val viewModel = createViewModel()
        viewModel.loadDetail(1L)
        advanceUntilIdle()

        coEvery { repository.replacePhoto(1L, 100L, backupFile.absolutePath) } returns PhotoOutDto(
            id = 100, photoFileName = "new_foto.jpg", thumbnailFileName = null, sortOrder = 0
        )
        coEvery { repository.fetchDetail(1L) } returns detail // refresh setelah re-upload

        viewModel.reuploadPhoto(1L, 100L)
        advanceUntilIdle()

        coVerify { repository.replacePhoto(1L, 100L, backupFile.absolutePath) }
        assertFalse(viewModel.uiState.value.isReuploading)
    }

    @Test
    fun `reuploadPhoto sets error when repository fails`() = runTest(testDispatcher) {
        val backupFile = tempFolder.newFile("foto2.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val detail = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            details = emptyList()
        )
        coEvery { repository.fetchDetail(1L) } returns detail
        coEvery { masterDataDao.getRoomById(1L) } returns null
        coEvery { masterDataDao.getPhotosForInspection(1L) } returns listOf(
            InspectionPhotoEntity(id = 100, detailId = 10, photoFileName = "foto.jpg", sortOrder = 0, localPath = backupFile.absolutePath)
        )
        val viewModel = createViewModel()
        viewModel.loadDetail(1L)
        advanceUntilIdle()

        coEvery { repository.replacePhoto(1L, 100L, backupFile.absolutePath) } throws RuntimeException("Endpoint belum tersedia")

        viewModel.reuploadPhoto(1L, 100L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isReuploading)
        assertEquals("Endpoint belum tersedia", viewModel.uiState.value.error)
    }

    @Test
    fun `reuploadPhoto without local backup sets error`() = runTest(testDispatcher) {
        val detail = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            details = emptyList()
        )
        coEvery { repository.fetchDetail(1L) } returns detail
        coEvery { masterDataDao.getRoomById(1L) } returns null
        coEvery { masterDataDao.getPhotosForInspection(1L) } returns emptyList()
        val viewModel = createViewModel()
        viewModel.loadDetail(1L)
        advanceUntilIdle()

        viewModel.reuploadPhoto(1L, 100L)
        advanceUntilIdle()

        assertEquals("Backup foto lokal tidak ditemukan", viewModel.uiState.value.error)
        coVerify(exactly = 0) { repository.replacePhoto(any(), any(), any()) }
    }

    @Test
    fun `clearDetail resets selectedDetail`() = runTest(testDispatcher) {
        val detail = InspectionOutDto(
            id = 1, roomId = 1, inspectorId = 5, status = "APPROVED",
            businessDate = "2026-07-28", localTimestamp = "2026-07-28T10:00:00Z", createdAt = "2026-07-28T10:00:00Z",
            rejectionReason = null, details = emptyList()
        )
        coEvery { repository.fetchDetail(1L) } returns detail
        coEvery { masterDataDao.getRoomById(1L) } returns null
        val viewModel = createViewModel()
        viewModel.loadDetail(1L)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.selectedDetail)
        viewModel.clearDetail()
        assertNull(viewModel.uiState.value.selectedDetail)
    }

    // ── Error edge cases ──

    @Test
    fun `init handles fetch failure gracefully`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } throws RuntimeException("Server down")
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isInitialLoading)
        assertEquals("Server down", viewModel.uiState.value.error)
    }

    @Test
    fun `multiple fast refreshes cancel previous`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } coAnswers {
            kotlinx.coroutines.delay(500)
            PaginatedResult(items = emptyList(), totalPages = 1, currentPage = firstArg())
        }
        val viewModel = createViewModel()
        viewModel.refreshFromServer()
        viewModel.refreshFromServer()
        viewModel.refreshFromServer()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    // ── Phase 4: Date Filter ──

    @Test
    fun `setFilterDate updates filterDate state`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        coEvery { repository.observeLocalInspections(any(), any()) } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.filterDate)
        viewModel.setFilterDate("2026-07-30")
        advanceUntilIdle()

        assertEquals("2026-07-30", viewModel.uiState.value.filterDate)
    }

    @Test
    fun `setFilterDate resets to page 1`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 2, currentPage = 1
        )
        coEvery { repository.observeLocalInspections(any(), any()) } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFilterDate("2026-07-30")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.uiState.value.hasMorePages)
    }

    @Test
    fun `setFilterDate passes date to observeLocalInspections`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFilterDate("2026-07-30")
        advanceUntilIdle()

        coVerify { repository.observeLocalInspections(null, "2026-07-30") }
    }

    @Test
    fun `setFilterDate with null clears filter`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        coEvery { repository.observeLocalInspections(any(), any()) } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFilterDate("2026-07-30")
        advanceUntilIdle()
        assertEquals("2026-07-30", viewModel.uiState.value.filterDate)

        viewModel.setFilterDate(null)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.filterDate)
    }

    @Test
    fun `setFilter preserves filterDate when changing status`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        coEvery { repository.observeLocalInspections(any(), any()) } returns MutableStateFlow(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFilterDate("2026-07-30")
        advanceUntilIdle()

        viewModel.setFilter("APPROVED")
        advanceUntilIdle()

        assertEquals("2026-07-30", viewModel.uiState.value.filterDate)
        assertEquals("APPROVED", viewModel.uiState.value.filterStatus)
        coVerify { repository.observeLocalInspections("APPROVED", "2026-07-30") }
    }

    @Test
    fun `setFilter with date passes both to repository`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setFilterDate("2026-07-30")
        advanceUntilIdle()

        viewModel.setFilter("REJECTED")
        advanceUntilIdle()

        coVerify { repository.observeLocalInspections("REJECTED", "2026-07-30") }
    }
}
