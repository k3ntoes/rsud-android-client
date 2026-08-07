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
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RoomItemEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.master.MasterDataRepository
import my.id.kentoes.rsudajibarangapp.master.SyncState
import my.id.kentoes.rsudajibarangapp.master.SyncStateStore
import my.id.kentoes.rsudajibarangapp.sync.MasterDataSyncResult
import my.id.kentoes.rsudajibarangapp.sync.SyncManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModelTest {

    private lateinit var drafDao: DrafDao
    private lateinit var masterDataDao: MasterDataDao
    private lateinit var masterDataRepository: MasterDataRepository
    private lateinit var syncManager: SyncManager
    private lateinit var syncStateStore: SyncStateStore
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        drafDao = mockk()
        masterDataDao = mockk()
        masterDataRepository = mockk()
        syncManager = mockk()
        syncStateStore = mockk()
        // Default mocks for computeInspectionStatus() called in init
        coEvery { masterDataDao.getAllRoomsOnce() } returns emptyList()
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns emptySet()
        // UX-01: query detail per-room (status list) — default kosong
        coEvery { masterDataDao.getAllRoomItems() } returns emptyList()
        coEvery { drafDao.getItemsForDraft(any()) } returns emptyList()
        coEvery { masterDataDao.getInspectionsByDate(any()) } returns MutableStateFlow(emptyList())
        // Default: cache terisi → auto-sync TIDAK jalan; lastSyncAt dibaca dari store
        coEvery { masterDataRepository.isCacheAvailable() } returns true
        every { syncStateStore.load() } returns SyncState()
        // combine() mengamati 2 flow — InspectionEntity default kosong
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DashboardViewModel =
        DashboardViewModel(
            drafDao, masterDataDao, masterDataRepository,
            syncManager, syncStateStore
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
        // Terkirim dari InspectionEntity (bukan draf SYNCED)
        assertEquals(1, state.draftCount)
        assertEquals(1, state.pendingSyncCount)
        assertEquals(3, state.syncedCount)
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
        // Terkirim dari InspectionEntity
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
        assertEquals(0, state.draftCount)
        assertEquals(0, state.pendingSyncCount)
        assertEquals(0, state.syncedCount)
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

        assertEquals(1, viewModel.uiState.value.draftCount)
        assertEquals(1, viewModel.uiState.value.syncedCount)

        draftsFlow.value = listOf(
            DrafInspeksi(id = 1, roomId = 1, localTimestamp = "", status = "DRAFT"),
            DrafInspeksi(id = 2, roomId = 2, localTimestamp = "", status = "DRAFT"),
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.draftCount)

        // Inspections flow update → Terkirim ikut berubah
        inspectionsFlow.value = listOf(
            InspectionEntity(id = 101, roomId = 1, inspectorId = 1, status = "PENDING"),
            InspectionEntity(id = 102, roomId = 2, inspectorId = 1, status = "APPROVED"),
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.syncedCount)
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

    // ── Phase 6 (2026-08): UX-01 per-room status list ──

    @Test
    fun `roomStatuses derives status with precedence inspection over draft`() = runTest {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val rooms = listOf(
            RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true),
            RuangEntity(id = 2, nama = "Ruang B", isMyRoom = true),
            RuangEntity(id = 3, nama = "Ruang C", isMyRoom = true),
        )
        every { drafDao.getAllDrafts() } returns MutableStateFlow(
            listOf(DrafInspeksi(id = 11, roomId = 1, localTimestamp = "T", status = "DRAFT", businessDate = today))
        )
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(rooms)
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns rooms
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns setOf(1L, 2L)
        // Room 2 sudah APPROVED hari ini — precedence inspeksi menang atas draf
        coEvery { masterDataDao.getInspectionsByDate(today) } returns MutableStateFlow(
            listOf(InspectionEntity(id = 21, roomId = 2, inspectorId = 1, status = "APPROVED", businessDate = today))
        )
        // Room 1 punya 2 item dari pivot
        coEvery { masterDataDao.getAllRoomItems() } returns listOf(
            RoomItemEntity(id = 1, roomId = 1, itemId = 1, sortOrder = 0),
            RoomItemEntity(id = 2, roomId = 1, itemId = 2, sortOrder = 1),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val statuses = viewModel.uiState.value.roomStatuses
        assertEquals(3, statuses.size)
        val room1 = statuses.first { it.roomId == 1L }
        assertEquals(RoomStatus.DRAF, room1.status)
        assertEquals(11L, room1.draftId)
        assertEquals(2, room1.itemCount)
        val room2 = statuses.first { it.roomId == 2L }
        assertEquals(RoomStatus.DISETUJUI, room2.status)
        assertEquals(21L, room2.inspectionId)
        val room3 = statuses.first { it.roomId == 3L }
        assertEquals(RoomStatus.BELUM, room3.status)
        assertNull(room3.draftId)
        assertNull(room3.inspectionId)
    }

    @Test
    fun `roomStatuses maps PENDING_SYNC draft to MENUNGGU_KIRIM and REJECTED to DITOLAK`() = runTest {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val rooms = listOf(
            RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true),
            RuangEntity(id = 2, nama = "Ruang B", isMyRoom = true),
        )
        every { drafDao.getAllDrafts() } returns MutableStateFlow(
            listOf(DrafInspeksi(id = 11, roomId = 1, localTimestamp = "T", status = "PENDING_SYNC", businessDate = today))
        )
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(rooms)
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns rooms
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns setOf(1L, 2L)
        coEvery { masterDataDao.getInspectionsByDate(today) } returns MutableStateFlow(
            listOf(InspectionEntity(id = 21, roomId = 2, inspectorId = 1, status = "REJECTED", businessDate = today))
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val statuses = viewModel.uiState.value.roomStatuses
        assertEquals(RoomStatus.MENUNGGU_KIRIM, statuses.first { it.roomId == 1L }.status)
        assertEquals(RoomStatus.DITOLAK, statuses.first { it.roomId == 2L }.status)
    }

    @Test
    fun `roomStatuses sorts BELUM first then by name`() = runTest {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val rooms = listOf(
            RuangEntity(id = 1, nama = "Zulu", isMyRoom = true),
            RuangEntity(id = 2, nama = "Alpha", isMyRoom = true),
        )
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(rooms)
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns rooms
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns setOf(2L)
        coEvery { masterDataDao.getInspectionsByDate(today) } returns MutableStateFlow(
            listOf(InspectionEntity(id = 21, roomId = 2, inspectorId = 1, status = "PENDING", businessDate = today))
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // BELUM (Zulu) harus di depan DISETUJUI/MENUNGGU_REVIEW (Alpha)
        assertEquals(listOf(1L, 2L), viewModel.uiState.value.roomStatuses.map { it.roomId })
    }

    @Test
    fun `roomStatuses refresh when drafts flow re-emits after save`() = runTest {
        // Regresi reviewer: status per-room harus segar saat user kembali dari form
        // setelah menyimpan draf — bukan hanya saat init/refresh.
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val rooms = listOf(RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true))
        val draftsFlow = MutableStateFlow(emptyList<DrafInspeksi>())
        every { drafDao.getAllDrafts() } returns draftsFlow
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(rooms)
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataDao.getAllRoomsOnce() } returns rooms
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns emptySet()

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(RoomStatus.BELUM, viewModel.uiState.value.roomStatuses.first().status)

        // User menyimpan draf → flow draf re-emit → status harus jadi DRAF tanpa refresh manual
        draftsFlow.value = listOf(
            DrafInspeksi(id = 11, roomId = 1, localTimestamp = "T", status = "DRAFT", businessDate = today)
        )
        advanceUntilIdle()

        assertEquals(RoomStatus.DRAF, viewModel.uiState.value.roomStatuses.first().status)
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

    // ── Phase 5 (2026-08): Sync UX ──

    @Test
    fun `refresh recomputes inspection status after sync populates rooms`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllInspections() } returns MutableStateFlow(emptyList())
        coEvery { masterDataRepository.isCacheAvailable() } returns false
        coEvery { syncManager.syncMasterData() } returns MasterDataSyncResult(
            succeeded = listOf("Items"), failed = emptyList()
        )
        every { syncStateStore.load() } returns SyncState(itemsSyncedAt = "2026-07-29T08:30:00Z")
        // init: cache kosong → rooms belum ada → kedua count 0
        coEvery { masterDataDao.getAllRoomsOnce() } returns emptyList()
        coEvery { masterDataRepository.getInspectedRoomIdsForDate(any()) } returns emptySet()

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.uninspectedRoomCount)
        assertEquals(0, viewModel.uiState.value.inspectedRoomCount)

        // Sync selesai → DB terisi rooms. refresh() berikutnya harus menghitung ulang
        // (regresi: computeInspectionStatus hanya jalan sekali di init, count macet di 0).
        coEvery { masterDataDao.getAllRoomsOnce() } returns listOf(
            RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true),
            RuangEntity(id = 2, nama = "Ruang B", isMyRoom = true),
            RuangEntity(id = 3, nama = "Ruang C", isMyRoom = false),
        )
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.uninspectedRoomCount)
        assertEquals(0, viewModel.uiState.value.inspectedRoomCount)
    }

    @Test
    fun `empty cache triggers auto sync and sets lastSyncAt`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { masterDataRepository.isCacheAvailable() } returns false
        coEvery { syncManager.syncMasterData() } returns MasterDataSyncResult(
            succeeded = listOf("Items"), failed = emptyList()
        )
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
        coEvery { syncManager.syncMasterData() } returns MasterDataSyncResult(
            succeeded = listOf("Items"), failed = emptyList()
        )

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
        coEvery { syncManager.syncMasterData() } returns MasterDataSyncResult(
            succeeded = emptyList(), failed = listOf("Items"), firstError = "Network down"
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals("Sync gagal", viewModel.uiState.value.syncError)
        assertNull(viewModel.uiState.value.lastSyncAt)
    }

    @Test
    fun `refresh partial success shows partial message instead of blanket failure`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())
        coEvery { masterDataRepository.isCacheAvailable() } returns true
        // H1: 5 langkah sukses, 1 gagal — pesan harus "Sebagian", bukan "Sync gagal",
        // karena DB memang sudah ter-update sebagian (nilai dashboard berubah).
        coEvery { syncManager.syncMasterData() } returns MasterDataSyncResult(
            succeeded = listOf("Items", "Ruangan", "Pivot Room-Item", "User-Room", "Users"),
            failed = listOf("Ruangan Saya"),
            firstError = "Network down"
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals(
            "Sebagian data diperbarui (5/6 berhasil) — ketuk retry",
            viewModel.uiState.value.syncError
        )
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
            MasterDataSyncResult(succeeded = emptyList(), failed = emptyList())
        }

        viewModelForGuard.refresh()
        advanceUntilIdle()

        assertTrue(refreshReentered)
        // Hanya 1 panggilan sync — refresh kedua selama isSyncing diabaikan
        coVerify(exactly = 1) { syncManager.syncMasterData() }
        assertFalse(viewModelForGuard.uiState.value.isSyncing)
    }
}
