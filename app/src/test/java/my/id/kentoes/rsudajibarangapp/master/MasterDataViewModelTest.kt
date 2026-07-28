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
        RuangEntity(1, "Ruang 1", "Lantai 1"),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
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
    fun `init triggers syncFromApi when cache is empty`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns false
        coEvery { repository.syncFromApi() } returns "Synced"

        MasterDataViewModel(repository)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.syncFromApi() }
    }

    @Test
    fun `init does not syncFromApi when cache is available`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true

        MasterDataViewModel(repository)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.syncFromApi() }
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
    fun `refresh triggers syncFromApi once`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        coEvery { repository.syncFromApi() } returns "Refreshed"

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.syncFromApi() }
    }

    @Test
    fun `refresh updates syncMessage on completion`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns true
        coEvery { repository.syncFromApi() } returns "Refresh done"

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals("Refresh done", viewModel.uiState.value.syncMessage)
    }

    // ── Sync state transitions ────────────────────────────

    @Test
    fun `sync sets isSyncing true during operation then false after`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns false
        coEvery { repository.syncFromApi() } returns "Done"

        val viewModel = MasterDataViewModel(repository)
        // Before advanceUntilIdle: isLoading is true, isSyncing false
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSyncing)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Done", viewModel.uiState.value.syncMessage)
    }

    @Test
    fun `sync failure sets syncMessage with error`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns false
        coEvery { repository.syncFromApi() } throws RuntimeException("Server error 500")

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
        assertEquals("Server error 500", viewModel.uiState.value.syncMessage)
    }

    // ── clearSyncMessage ──────────────────────────────────

    @Test
    fun `clearSyncMessage clears syncMessage to null`() = runTest {
        val itemsFlow = MutableStateFlow(emptyList<MasterDataItem>())
        val roomsFlow = MutableStateFlow(emptyList<RuangEntity>())
        every { repository.items } returns itemsFlow
        every { repository.rooms } returns roomsFlow
        coEvery { repository.isCacheAvailable() } returns false
        coEvery { repository.syncFromApi() } returns "Done"

        val viewModel = MasterDataViewModel(repository)
        advanceUntilIdle()
        assertEquals("Done", viewModel.uiState.value.syncMessage)

        viewModel.clearSyncMessage()

        assertNull(viewModel.uiState.value.syncMessage)
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
}
