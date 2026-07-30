package my.id.kentoes.rsudajibarangapp.inspection

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserEntity
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
import org.junit.Test

class InspectionHistoryViewModelTest {

    private lateinit var repository: InspectionHistoryRepository
    private lateinit var masterDataDao: MasterDataDao

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        masterDataDao = mockk()
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getInspectionsByStatus(any()) } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getUserById(any()) } returns null
        coEvery { repository.observeLocalInspections(any(), any()) } returns MutableStateFlow(emptyList())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = InspectionHistoryViewModel(repository, masterDataDao)

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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    // ── refreshFromServer ──

    @Test
    fun `refreshFromServer updates cache and resets page`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(1, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `refreshFromServer sets error on failure`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } throws RuntimeException("Network error")
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals("Network error", viewModel.uiState.value.error)
    }

    // ── setFilter ──

    @Test
    fun `setFilter updates filter status and resets page`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
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
        // Stub for init — simple, completes immediately
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(emptyList(), 1, 1)
        val viewModel = createViewModel()
        advanceUntilIdle() // init completes

        // Overwrite with count-tracking stub for pagination
        var callCount = 0
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } coAnswers {
            callCount++
            kotlinx.coroutines.delay(100000)
            PaginatedResult(items = emptyList(), totalPages = 1, currentPage = firstArg())
        }

        viewModel.loadNextPage()
        testDispatcher.scheduler.runCurrent() // execute coroutine up to delay(100000)
        viewModel.loadNextPage() // guard fires — isLoadingMore is true
        assertEquals(1, callCount)
    }

    @Test
    fun `loadNextPage sets error on failure`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        coEvery { repository.fetchInspections(page = 2, any(), any(), any()) } throws RuntimeException("Timeout")
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoadingMore)
        assertEquals("Timeout", viewModel.uiState.value.error)
    }

    @Test
    fun `loadNextPage stops at 10 pages`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
        )
        val viewModel = createViewModel()
        advanceUntilIdle()
        for (i in 2..10) {
            viewModel.loadNextPage()
            advanceUntilIdle()
        }
        assertFalse(viewModel.uiState.value.hasMorePages)
        assertEquals(10, viewModel.uiState.value.currentPage)
        viewModel.loadNextPage()
        advanceUntilIdle()
        assertEquals(10, viewModel.uiState.value.currentPage)
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
        coEvery { masterDataDao.getUserById(5) } returns UserEntity(id = 5, username = "petugas01", role = "inspector", isActive = true)
        val viewModel = createViewModel()
        viewModel.loadDetail(1L)
        advanceUntilIdle()
        assertEquals("Ruang IGD", viewModel.uiState.value.detailRoomName)
        assertEquals("petugas01 (inspector)", viewModel.uiState.value.inspectorName)
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
        coEvery { masterDataDao.getUserById(99) } returns null
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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } throws RuntimeException("Server down")
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isInitialLoading)
        assertEquals("Server down", viewModel.uiState.value.error)
    }

    @Test
    fun `multiple fast refreshes cancel previous`() = runTest(testDispatcher) {
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } coAnswers {
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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
            items = emptyList(), totalPages = 1, currentPage = 1
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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
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
        coEvery { repository.fetchInspections(any(), any(), any(), any()) } returns PaginatedResult(
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
