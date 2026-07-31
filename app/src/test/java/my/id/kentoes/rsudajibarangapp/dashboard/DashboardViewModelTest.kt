package my.id.kentoes.rsudajibarangapp.dashboard

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
import my.id.kentoes.rsudajibarangapp.auth.AuthRepository
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.dashboard.api.AnalyticsApi
import my.id.kentoes.rsudajibarangapp.dashboard.api.IssueFrequencyOut
import my.id.kentoes.rsudajibarangapp.dashboard.api.RoomScoreOut
import my.id.kentoes.rsudajibarangapp.master.MasterDataRepository
import my.id.kentoes.rsudajibarangapp.master.SyncState
import my.id.kentoes.rsudajibarangapp.master.SyncStateStore
import my.id.kentoes.rsudajibarangapp.sync.SyncManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DashboardViewModelTest {

    private lateinit var drafDao: DrafDao
    private lateinit var masterDataDao: MasterDataDao
    private lateinit var analyticsApi: AnalyticsApi
    private lateinit var masterDataRepository: MasterDataRepository
    private lateinit var syncManager: SyncManager
    private lateinit var syncStateStore: SyncStateStore
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        drafDao = mockk()
        masterDataDao = mockk()
        analyticsApi = mockk(relaxed = true)
        masterDataRepository = mockk()
        syncManager = mockk()
        syncStateStore = mockk()
        authRepository = mockk()
        every { authRepository.currentUser } returns MutableStateFlow(
            UserOut(id = 1, username = "petugas01", role = "inspector", isActive = true)
        )
        // Default mocks for computeInspectionStatus() called in init
        coEvery { masterDataDao.getAllRoomsOnce() } returns emptyList()
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns emptySet()
        // Default: cache terisi → auto-sync TIDAK jalan; lastSyncAt dibaca dari store
        coEvery { masterDataRepository.isCacheAvailable() } returns true
        every { syncStateStore.load() } returns SyncState()
        // combine() mengamati 4 flow — InspectionEntity default kosong
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DashboardViewModel =
        DashboardViewModel(
            drafDao, masterDataDao, analyticsApi, masterDataRepository,
            syncManager, syncStateStore, authRepository
        )

    @Test
    fun `init loads all stats from dao flows`() = runTest {
        val draftsFlow = MutableStateFlow(
            listOf(
                DrafInspeksi(id = 1, roomId = 1, localTimestamp = "2026-01-01T00:00:00Z", status = "DRAFT"),
                DrafInspeksi(id = 2, roomId = 2, localTimestamp = "2026-01-02T00:00:00Z", status = "PENDING_SYNC"),
                DrafInspeksi(id = 3, roomId = 3, localTimestamp = "2026-01-03T00:00:00Z", status = "SYNCED"),
            )
        )
        val roomsFlow = MutableStateFlow(
            listOf(
                RuangEntity(id = 1, nama = "Ruang A"),
                RuangEntity(id = 2, nama = "Ruang B"),
            )
        )
        val itemsFlow = MutableStateFlow(
            listOf(
                MasterDataItem(id = 1, nama = "Meja", kategori = "Furnitur"),
                MasterDataItem(id = 2, nama = "Kursi", kategori = "Furnitur"),
                MasterDataItem(id = 3, nama = "Lantai", kategori = "Struktur"),
                MasterDataItem(id = 4, nama = "AC", kategori = "Elektronik"),
            )
        )

        every { drafDao.getAllDrafts() } returns draftsFlow
        every { masterDataDao.getAllRooms() } returns roomsFlow
        every { masterDataDao.getAllItems() } returns itemsFlow
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(
            listOf(
                InspectionEntity(id = 101, roomId = 1, inspectorId = 1, status = "APPROVED"),
                InspectionEntity(id = 102, roomId = 2, inspectorId = 1, status = "PENDING"),
                InspectionEntity(id = 103, roomId = 1, inspectorId = 1, status = "REJECTED"),
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        // Terkirim & Total dari InspectionEntity (bukan draf SYNCED)
        assertEquals(3, state.totalDrafts)
        assertEquals(1, state.draftCount)
        assertEquals(1, state.pendingSyncCount)
        assertEquals(3, state.syncedCount)
        assertEquals(2, state.totalRooms)
        assertEquals(4, state.totalItems)
    }

    @Test
    fun `init counts drafts by status correctly`() = runTest {
        val draftsFlow = MutableStateFlow(
            listOf(
                DrafInspeksi(id = 1, roomId = 1, localTimestamp = "", status = "DRAFT"),
                DrafInspeksi(id = 2, roomId = 2, localTimestamp = "", status = "DRAFT"),
                DrafInspeksi(id = 3, roomId = 3, localTimestamp = "", status = "PENDING_SYNC"),
                DrafInspeksi(id = 4, roomId = 4, localTimestamp = "", status = "PENDING_SYNC"),
                DrafInspeksi(id = 5, roomId = 5, localTimestamp = "", status = "PENDING_SYNC"),
                DrafInspeksi(id = 6, roomId = 6, localTimestamp = "", status = "SYNCED"),
            )
        )

        every { drafDao.getAllDrafts() } returns draftsFlow
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(
            (1..6).map { InspectionEntity(id = it.toLong(), roomId = it.toLong(), inspectorId = 1, status = "PENDING") }
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Total & Terkirim dari InspectionEntity
        assertEquals(6, state.totalDrafts)
        assertEquals(2, state.draftCount)
        assertEquals(3, state.pendingSyncCount)
        assertEquals(6, state.syncedCount)
    }

    @Test
    fun `init with empty drafts returns zeros`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.totalDrafts)
        assertEquals(0, state.draftCount)
        assertEquals(0, state.pendingSyncCount)
        assertEquals(0, state.syncedCount)
        assertEquals(0, state.totalRooms)
        assertEquals(0, state.totalItems)
        assertTrue(state.recentDrafts.isEmpty())
    }

    @Test
    fun `recentDrafts only contains first 5 drafts`() = runTest {
        val drafts = (1..10).map { id ->
            DrafInspeksi(id = id.toLong(), roomId = id.toLong(), localTimestamp = "2026-01-${id.toString().padStart(2, '0')}T00:00:00Z", status = "DRAFT")
        }
        val draftsFlow = MutableStateFlow(drafts)

        every { drafDao.getAllDrafts() } returns draftsFlow
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.recentDrafts.size)
        assertEquals(1L, viewModel.uiState.value.recentDrafts[0].id)
        assertEquals(5L, viewModel.uiState.value.recentDrafts[4].id)
    }

    @Test
    fun `init state has isLoading true before advance`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(0, viewModel.uiState.value.totalDrafts)
    }

    @Test
    fun `init handles unknown draft status gracefully`() = runTest {
        val draftsFlow = MutableStateFlow(
            listOf(
                DrafInspeksi(id = 1, roomId = 1, localTimestamp = "", status = "UNKNOWN_STATUS"),
            )
        )

        every { drafDao.getAllDrafts() } returns draftsFlow
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Tidak ada InspectionEntity & draft berstatus unknown tidak dihitung di draf/pending
        assertEquals(0, state.totalDrafts)
        assertEquals(0, state.draftCount)
        assertEquals(0, state.pendingSyncCount)
        assertEquals(0, state.syncedCount)
    }

    @Test
    fun `init reacts to updated flow emissions`() = runTest {
        val draftsFlow = MutableStateFlow(
            listOf(DrafInspeksi(id = 1, roomId = 1, localTimestamp = "", status = "DRAFT"))
        )
        val inspectionsFlow = MutableStateFlow(
            listOf(InspectionEntity(id = 101, roomId = 1, inspectorId = 1, status = "PENDING"))
        )

        every { drafDao.getAllDrafts() } returns draftsFlow
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns inspectionsFlow

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.totalDrafts)
        assertEquals(1, viewModel.uiState.value.draftCount)
        assertEquals(1, viewModel.uiState.value.syncedCount)

        draftsFlow.value = listOf(
            DrafInspeksi(id = 1, roomId = 1, localTimestamp = "", status = "DRAFT"),
            DrafInspeksi(id = 2, roomId = 2, localTimestamp = "", status = "DRAFT"),
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.draftCount)

        // Inspections flow update → Terkirim/Total ikut berubah
        inspectionsFlow.value = listOf(
            InspectionEntity(id = 101, roomId = 1, inspectorId = 1, status = "PENDING"),
            InspectionEntity(id = 102, roomId = 2, inspectorId = 1, status = "APPROVED"),
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.syncedCount)
        assertEquals(2, viewModel.uiState.value.totalDrafts)
    }

    // ── Analytics (hanya supervisor/admin_ppi — keputusan review 2026-08) ──

    @Test
    fun `supervisor fetches analytics from BE`() = runTest {
        every { authRepository.currentUser } returns MutableStateFlow(
            UserOut(id = 9, username = "super01", role = "supervisor", isActive = true)
        )
        val sampleRooms = listOf(
            RoomScoreOut(roomId = 1, scorePct = 0.45, inspectionCount = 5),
            RoomScoreOut(roomId = 2, scorePct = 0.60, inspectionCount = 3),
        )
        val sampleIssues = listOf(
            IssueFrequencyOut(itemId = 1, itemNameSnapshot = "Meja", scoreZeroCount = 12),
        )

        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { analyticsApi.getLowestRooms(any(), any()) } returns sampleRooms
        coEvery { analyticsApi.getTopIssues(any(), any()) } returns sampleIssues

        val viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { analyticsApi.getLowestRooms(any(), any()) }
        coVerify(exactly = 1) { analyticsApi.getTopIssues(any(), any()) }
        assertEquals(2, viewModel.uiState.value.lowestRooms.size)
        assertEquals(1, viewModel.uiState.value.topIssues.size)
        assertEquals(1, viewModel.uiState.value.lowestRooms[0].roomId)
        assertEquals("Meja", viewModel.uiState.value.topIssues[0].itemNameSnapshot)
    }

    @Test
    fun `admin fetches analytics from BE`() = runTest {
        every { authRepository.currentUser } returns MutableStateFlow(
            UserOut(id = 9, username = "admin01", role = "admin_ppi", isActive = true)
        )
        val sampleRooms = listOf(
            RoomScoreOut(roomId = 1, scorePct = 0.45, inspectionCount = 5),
        )

        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { analyticsApi.getLowestRooms(any(), any()) } returns sampleRooms
        coEvery { analyticsApi.getTopIssues(any(), any()) } returns emptyList()

        val viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { analyticsApi.getLowestRooms(any(), any()) }
        assertEquals(1, viewModel.uiState.value.lowestRooms.size)
    }

    @Test
    fun `inspector does not fetch analytics`() = runTest {
        // Role default setup = inspector → analytics TIDAK boleh di-fetch
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { analyticsApi.getLowestRooms(any(), any()) }
        coVerify(exactly = 0) { analyticsApi.getTopIssues(any(), any()) }
        assertTrue(viewModel.uiState.value.lowestRooms.isEmpty())
        assertTrue(viewModel.uiState.value.topIssues.isEmpty())
    }

    // ── Phase 4: Inspection Status ──

    @Test
    fun `init computes inspectedRoomCount from repository for assigned rooms`() = runTest {
        // Inspector default → hanya room isMyRoom yang masuk scope
        val sampleRoomList = listOf(
            RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true),
            RuangEntity(id = 2, nama = "Ruang B", isMyRoom = true),
            RuangEntity(id = 3, nama = "Ruang C", isMyRoom = true),
        )
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(sampleRoomList)
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns sampleRoomList
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns setOf(1L, 2L)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.inspectedRoomCount)
        assertEquals(1, viewModel.uiState.value.uninspectedRoomCount)
    }

    @Test
    fun `inspector scope only counts isMyRoom rooms`() = runTest {
        // Room id 3 & 4 TIDAK di-assign → tidak masuk scope inspector
        val sampleRoomList = listOf(
            RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true),
            RuangEntity(id = 2, nama = "Ruang B", isMyRoom = true),
            RuangEntity(id = 3, nama = "Ruang C", isMyRoom = false),
            RuangEntity(id = 4, nama = "Ruang D", isMyRoom = false),
        )
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(sampleRoomList)
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns sampleRoomList
        // Semua room diinspeksi, tapi hanya 2 yang di-assign → scope = 2
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns setOf(1L, 2L, 3L, 4L)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.inspectedRoomCount)
        assertEquals(0, viewModel.uiState.value.uninspectedRoomCount)
    }

    @Test
    fun `admin scope counts all rooms regardless of isMyRoom`() = runTest {
        every { authRepository.currentUser } returns MutableStateFlow(
            UserOut(id = 9, username = "admin01", role = "admin_ppi", isActive = true)
        )
        val sampleRoomList = listOf(
            RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true),
            RuangEntity(id = 2, nama = "Ruang B", isMyRoom = false),
            RuangEntity(id = 3, nama = "Ruang C", isMyRoom = false),
        )
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(sampleRoomList)
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns sampleRoomList
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns setOf(1L)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Admin melihat semua room — 3 total, 1 sudah diinspeksi
        assertEquals(1, viewModel.uiState.value.inspectedRoomCount)
        assertEquals(2, viewModel.uiState.value.uninspectedRoomCount)
    }

    @Test
    fun `init sets inspectedRoomCount zero when no inspections today`() = runTest {
        val sampleRoomList = listOf(
            RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true),
            RuangEntity(id = 2, nama = "Ruang B", isMyRoom = true),
        )
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(sampleRoomList)
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns sampleRoomList
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns emptySet()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.inspectedRoomCount)
        assertEquals(2, viewModel.uiState.value.uninspectedRoomCount)
    }

    @Test
    fun `init sets all rooms inspected when all are done`() = runTest {
        val sampleRoomList = listOf(
            RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true),
            RuangEntity(id = 2, nama = "Ruang B", isMyRoom = true),
        )
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(sampleRoomList)
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns sampleRoomList
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns setOf(1L, 2L)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.inspectedRoomCount)
        assertEquals(0, viewModel.uiState.value.uninspectedRoomCount)
    }

    @Test
    fun `init handles empty rooms list for inspection status`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns emptyList()
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns emptySet()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.inspectedRoomCount)
        assertEquals(0, viewModel.uiState.value.uninspectedRoomCount)
    }

    @Test
    fun `init uninspected count never goes negative`() = runTest {
        // Edge case: more inspected rooms than total (should be guarded by coerceAtLeast)
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(listOf(RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true)))
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns listOf(RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true))
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns setOf(1L, 99L) // 99 doesn't exist

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.uninspectedRoomCount)
    }

    @Test
    fun `init calls computeInspectionStatus with today date`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns emptyList()
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns emptySet()

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Verify the repository was called with a yyyy-MM-dd formatted date
        coVerify { masterDataRepository.getInspectedRoomIdsForDate(match { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }) }
    }

    @Test
    fun `analytics fetch failure returns empty lists`() = runTest {
        every { authRepository.currentUser } returns MutableStateFlow(
            UserOut(id = 9, username = "super01", role = "supervisor", isActive = true)
        )
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { analyticsApi.getLowestRooms(any(), any()) } throws RuntimeException("Network error")
        coEvery { analyticsApi.getTopIssues(any(), any()) } throws RuntimeException("Network error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.lowestRooms.isEmpty())
        assertTrue(viewModel.uiState.value.topIssues.isEmpty())
    }

    // ── Phase 5 (2026-08): Sync UX ──

    @Test
    fun `empty cache triggers auto sync and sets lastSyncAt`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { masterDataRepository.isCacheAvailable() } returns false
        coEvery { syncManager.syncMasterData() } returns Unit
        every { syncStateStore.load() } returns SyncState(itemsSyncedAt = "2026-07-29T08:30:00Z")

        val viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { syncManager.syncMasterData() }
        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals("2026-07-29T08:30:00Z", viewModel.uiState.value.lastSyncAt)
        assertNull(viewModel.uiState.value.syncError)
    }

    @Test
    fun `existing cache does not trigger sync but shows last sync time`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { masterDataRepository.isCacheAvailable() } returns true
        every { syncStateStore.load() } returns SyncState(roomsSyncedAt = "2026-07-28T10:00:00Z")

        val viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { syncManager.syncMasterData() }
        assertEquals("2026-07-28T10:00:00Z", viewModel.uiState.value.lastSyncAt)
    }

    @Test
    fun `refresh pulls master data and updates lastSyncAt`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { masterDataRepository.isCacheAvailable() } returns true
        every { syncStateStore.load() } returns SyncState(itemsSyncedAt = "2026-07-29T08:30:00Z")
        coEvery { syncManager.syncMasterData() } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        coVerify(exactly = 1) { syncManager.syncMasterData() }
        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals("2026-07-29T08:30:00Z", viewModel.uiState.value.lastSyncAt)
    }

    @Test
    fun `refresh failure sets syncError for retry`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { masterDataRepository.isCacheAvailable() } returns true
        coEvery { syncManager.syncMasterData() } throws RuntimeException("Network down")

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals("Network down", viewModel.uiState.value.syncError)
        assertNull(viewModel.uiState.value.lastSyncAt)
    }

    @Test
    fun `refresh is guarded against concurrent sync`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { masterDataRepository.isCacheAvailable() } returns true

        val viewModelForGuard = createViewModel()
        advanceUntilIdle()

        // Saat sync pertama sedang berjalan, refresh kedua harus diabaikan (guard isSyncing)
        var refreshReentered = false
        coEvery { syncManager.syncMasterData() } coAnswers {
            viewModelForGuard.refresh()
            refreshReentered = true
            Unit
        }

        viewModelForGuard.refresh()
        advanceUntilIdle()

        assertTrue(refreshReentered)
        // Hanya 1 panggilan sync — refresh kedua selama isSyncing diabaikan
        coVerify(exactly = 1) { syncManager.syncMasterData() }
        assertFalse(viewModelForGuard.uiState.value.isSyncing)
    }
}
