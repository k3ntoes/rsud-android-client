package my.id.kentoes.rsudajibarangapp.master

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
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MasterDataViewModelTest {

    private lateinit var repository: MasterDataRepository
    private val testDispatcher = StandardTestDispatcher()

    private val sampleItems = listOf(
        MasterDataItem(1, "Meja", "Furnitur", "Meja kayu"),
        MasterDataItem(2, "Kursi", "Furnitur", "Kursi plastik"),
        MasterDataItem(3, "Stetoskop", "Medis", "Alat medis"),
    )
    private val sampleRooms = listOf(
        RuangEntity(1, "Ruang 1", "Lantai 1", isMyRoom = true),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        // loadRoomItemCounts() dipanggil saat init & setelah sync — default pivot kosong
        coEvery { repository.getRoomItemMap() } returns emptyMap()
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    // ── Init — flow collection ────────────────────────────

    @Test
    fun `init loads items and rooms from repository flows`() = runTest {
        val itemsFlow = MutableStateFlow(sampleItems)
        val roomsFlow = MutableStateFlow(sampleRooms)
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.items.size)
        assertEquals(1, state.rooms.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `init loads item counts from pivot room_items`() = runTest {
        val itemsFlow = MutableStateFlow(sampleItems)
        val roomsFlow = MutableStateFlow(sampleRooms)
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        // Pivot: room 1 punya 2 item
        coEvery { repository.getRoomItemMap() } returns mapOf(1L to listOf(1L, 2L))

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        // Item count RoomCard dari pivot — bukan heuristik nama
        assertEquals(2, viewModel.uiState.value.roomItemCounts[1L])
    }

    @Test
    fun `refresh reloads item counts after sync`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        coEvery { repository.syncItems() } returns Unit
        coEvery { repository.syncRooms() } returns Unit
        coEvery { repository.syncRoomItems() } returns Unit
        coEvery { repository.syncMyRooms() } returns Unit
        coEvery { repository.getRoomItemMap() } returns mapOf(7L to listOf(1L))

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        // Setelah refresh (sync selesai), pivot dimuat ulang → count segar
        assertEquals(1, viewModel.uiState.value.roomItemCounts[7L])
    }

    @Test
    fun `init groups items by kategori`() = runTest {
        val itemsFlow = MutableStateFlow(sampleItems)
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        val grouped = viewModel.uiState.value.groupedItems
        assertEquals(2, grouped.size)
        assertEquals(2, grouped["Furnitur"]?.size)
        assertEquals(1, grouped["Medis"]?.size)
    }

    @Test
    fun `init handles empty items gracefully`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.items.isEmpty())
        assertTrue(state.groupedItems.isEmpty())
        assertFalse(state.isLoading)
    }

    // ── Init — auto-sync behavior ─────────────────────────

    @Test
    fun `init triggers full master data sync when cache is empty`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns false
        coEvery { repository.syncItems() } returns Unit
        coEvery { repository.syncRooms() } returns Unit
        coEvery { repository.syncRoomItems() } returns Unit
        coEvery { repository.syncMyRooms() } returns Unit

        MasterDataViewModel(repository)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.syncItems() }
        coVerify(exactly = 1) { repository.syncRooms() }
        coVerify(exactly = 1) { repository.syncRoomItems() }
        coVerify(exactly = 1) { repository.syncMyRooms() }
    }

    @Test
    fun `init does not sync when cache is available`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true

        MasterDataViewModel(repository)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.syncItems() }
        coVerify(exactly = 0) { repository.syncRooms() }
        coVerify(exactly = 0) { repository.syncRoomItems() }
        coVerify(exactly = 0) { repository.syncMyRooms() }
    }

    @Test
    fun `init sets isLoading false when cache is available and no sync needed`() = runTest {
        val itemsFlow = MutableStateFlow(sampleItems)
        val roomsFlow = MutableStateFlow(sampleRooms)
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSyncing)
        assertNull(viewModel.uiState.value.syncMessage)
    }

    // ── Refresh ───────────────────────────────────────────

    @Test
    fun `refresh triggers full master data sync once`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        coEvery { repository.syncItems() } returns Unit
        coEvery { repository.syncRooms() } returns Unit
        coEvery { repository.syncRoomItems() } returns Unit
        coEvery { repository.syncMyRooms() } returns Unit

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.syncItems() }
        coVerify(exactly = 1) { repository.syncRooms() }
        coVerify(exactly = 1) { repository.syncRoomItems() }
        coVerify(exactly = 1) { repository.syncMyRooms() }
    }

    @Test
    fun `refresh updates syncMessage on completion`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        coEvery { repository.syncItems() } returns Unit
        coEvery { repository.syncRooms() } returns Unit
        coEvery { repository.syncRoomItems() } returns Unit
        coEvery { repository.syncMyRooms() } returns Unit

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals("Data berhasil diperbarui", viewModel.uiState.value.syncMessage)
    }

    // ── Sync state transitions ────────────────────────────

    @Test
    fun `sync sets isSyncing true during operation then false after`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns false
        coEvery { repository.syncItems() } returns Unit
        coEvery { repository.syncRooms() } returns Unit
        coEvery { repository.syncRoomItems() } returns Unit
        coEvery { repository.syncMyRooms() } returns Unit

        val viewModel = MasterDataViewModel(repository)
        // Before advanceUntilIdle: isLoading is true, isSyncing false
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSyncing)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Data berhasil diperbarui", viewModel.uiState.value.syncMessage)
    }

    @Test
    fun `sync failure sets syncMessage with error`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns false
        coEvery { repository.syncItems() } throws RuntimeException("Server error 500")

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals("Server error 500", viewModel.uiState.value.syncMessage)
    }

    @Test
    fun `refresh partial failure shows partial message instead of blanket failure`() = runTest {
        // H1: 3 langkah sukses, syncMyRooms gagal — pesan harus "Sebagian", bukan error
        // mentah, karena sebagian data memang sudah ter-update.
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        coEvery { repository.syncItems() } returns Unit
        coEvery { repository.syncRooms() } returns Unit
        coEvery { repository.syncRoomItems() } returns Unit
        coEvery { repository.syncMyRooms() } throws RuntimeException("Network down")

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals(
            "Sebagian data diperbarui (3/4 berhasil) — ketuk retry",
            viewModel.uiState.value.syncMessage
        )
    }

    // ── clearSyncMessage ──────────────────────────────────

    @Test
    fun `clearSyncMessage clears syncMessage to null`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns false
        coEvery { repository.syncItems() } returns Unit
        coEvery { repository.syncRooms() } returns Unit
        coEvery { repository.syncRoomItems() } returns Unit
        coEvery { repository.syncMyRooms() } returns Unit

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()
        assertEquals("Data berhasil diperbarui", viewModel.uiState.value.syncMessage)

        viewModel.clearSyncMessage()

        assertNull(viewModel.uiState.value.syncMessage)
    }

    // ── Phase 4: Uninspected Filter ──

    @Test
    fun `setUninspectedFilter sets excludeRoomIds from repository`() = runTest {
        val itemsFlow = MutableStateFlow(sampleItems)
        val roomsFlow = MutableStateFlow(sampleRooms)
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        coEvery { repository.getInspectedRoomIdsForDate(any()) } returns setOf(1L)

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.excludeRoomIds.size)

        viewModel.setUninspectedFilter("2026-07-30")
        advanceUntilIdle()

        assertEquals(setOf(1L), viewModel.uiState.value.excludeRoomIds)
        coVerify { repository.getInspectedRoomIdsForDate("2026-07-30") }
    }

    @Test
    fun `setUninspectedFilter excludes inspected rooms from list`() = runTest {
        val roomsWithExtra = sampleRooms + RuangEntity(id = 2, nama = "Ruang 2", lantai = "Lantai 2", isMyRoom = true)
        val itemsFlow = MutableStateFlow(sampleItems)
        val roomsFlow = MutableStateFlow(roomsWithExtra)
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        coEvery { repository.getInspectedRoomIdsForDate(any()) } returns setOf(1L)

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        // Initially all rooms visible
        assertEquals(2, viewModel.uiState.value.rooms.size)

        viewModel.setUninspectedFilter("2026-07-30")
        advanceUntilIdle()

        // After filter: only room 2 remains (room 1 excluded)
        assertEquals(1, viewModel.uiState.value.rooms.size)
        assertEquals(2, viewModel.uiState.value.rooms[0].id)
    }

    @Test
    fun `setUninspectedFilter with empty inspected set shows all rooms`() = runTest {
        val roomsWithExtra = sampleRooms + RuangEntity(id = 2, nama = "Ruang 2", lantai = "Lantai 2", isMyRoom = true)
        val itemsFlow = MutableStateFlow(sampleItems)
        val roomsFlow = MutableStateFlow(roomsWithExtra)
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        coEvery { repository.getInspectedRoomIdsForDate(any()) } returns emptySet()

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        viewModel.setUninspectedFilter("2026-07-30")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.rooms.size)
    }

    @Test
    fun `clearSyncMessage does not affect other state fields`() = runTest {
        val itemsFlow = MutableStateFlow(sampleItems)
        val roomsFlow = MutableStateFlow(sampleRooms)
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        viewModel.clearSyncMessage()

        val state = viewModel.uiState.value
        assertEquals(3, state.items.size)
        assertEquals(1, state.rooms.size)
        assertFalse(state.isLoading)
        assertNull(state.syncMessage)
    }

    // ── Role-based room filtering (isMyRoom) ──

    @Test
    fun `inspector sees only assigned rooms`() = runTest {
        val itemsFlow = MutableStateFlow(sampleItems)
        val roomsFlow = MutableStateFlow(listOf(
            RuangEntity(id = 1, nama = "Ruang A", isMyRoom = true),
            RuangEntity(id = 2, nama = "Ruang B", isMyRoom = false),
        ))
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        // Hanya room yang di-assign yang tampil — room B (dicabut) tidak muncul
        val rooms = viewModel.uiState.value.rooms
        assertEquals(1, rooms.size)
        assertEquals(1L, rooms[0].id)
    }

}
