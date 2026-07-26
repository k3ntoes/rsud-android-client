package my.id.kentoes.rsudajibarangapp.dashboard

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
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DashboardViewModelTest {

    private lateinit var drafDao: DrafDao
    private lateinit var masterDataDao: MasterDataDao
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        drafDao = mockk()
        masterDataDao = mockk()
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

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

        val viewModel = DashboardViewModel(drafDao, masterDataDao)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.totalDrafts)
        assertEquals(1, state.draftCount)
        assertEquals(1, state.pendingSyncCount)
        assertEquals(1, state.syncedCount)
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

        val viewModel = DashboardViewModel(drafDao, masterDataDao)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(6, state.totalDrafts)
        assertEquals(2, state.draftCount)
        assertEquals(3, state.pendingSyncCount)
        assertEquals(1, state.syncedCount)
    }

    @Test
    fun `init with empty drafts returns zeros`() = runTest {
        every { drafDao.getAllDrafts() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())

        val viewModel = DashboardViewModel(drafDao, masterDataDao)
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

        val viewModel = DashboardViewModel(drafDao, masterDataDao)
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

        val viewModel = DashboardViewModel(drafDao, masterDataDao)

        // Before advanceUntilIdle, coroutine hasn't run yet
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

        val viewModel = DashboardViewModel(drafDao, masterDataDao)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.totalDrafts)
        assertEquals(0, state.draftCount)  // not DRAFT
        assertEquals(0, state.pendingSyncCount)  // not PENDING_SYNC
        assertEquals(0, state.syncedCount)  // not SYNCED
    }

    @Test
    fun `init reacts to updated flow emissions`() = runTest {
        val draftsFlow = MutableStateFlow(
            listOf(DrafInspeksi(id = 1, roomId = 1, localTimestamp = "", status = "DRAFT"))
        )

        every { drafDao.getAllDrafts() } returns draftsFlow
        every { masterDataDao.getAllRooms() } returns MutableStateFlow(emptyList())
        every { masterDataDao.getAllItems() } returns MutableStateFlow(emptyList())

        val viewModel = DashboardViewModel(drafDao, masterDataDao)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.totalDrafts)
        assertEquals(1, viewModel.uiState.value.draftCount)

        // Emit updated data — add a new synced draft
        draftsFlow.value = listOf(
            DrafInspeksi(id = 1, roomId = 1, localTimestamp = "", status = "DRAFT"),
            DrafInspeksi(id = 2, roomId = 2, localTimestamp = "", status = "SYNCED"),
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.totalDrafts)
        assertEquals(1, viewModel.uiState.value.draftCount)
        assertEquals(1, viewModel.uiState.value.syncedCount)
    }
}
