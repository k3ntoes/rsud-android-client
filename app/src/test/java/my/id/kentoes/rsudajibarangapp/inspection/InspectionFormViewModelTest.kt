package my.id.kentoes.rsudajibarangapp.inspection

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.sync.SyncWorker
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InspectionFormViewModelTest {

    private val context = mockk<Context>()
    private val masterDataDao = mockk<MasterDataDao>()
    private val drafDao = mockk<DrafDao>()
    private val inspectionRepository = mockk<InspectionRepository>()
    private lateinit var viewModel: InspectionFormViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val sampleItems = listOf(
        MasterDataItem(id = 1, nama = "Meja", kategori = "Furnitur"),
        MasterDataItem(id = 2, nama = "Kursi", kategori = "Furnitur"),
        MasterDataItem(id = 3, nama = "Lantai", kategori = "Struktur"),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock SyncWorker.enqueue agar tidak beneran panggil WorkManager
        mockkObject(SyncWorker.Companion)
        every { SyncWorker.enqueue(any()) } returns Unit

        viewModel = InspectionFormViewModel(context, masterDataDao, drafDao, inspectionRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ── init() ──

    @Test
    fun `init loads items and sets correct initial state`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)

        viewModel.init(roomId = 10, roomName = "Ruang A")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(10L, state.roomId)
        assertEquals("Ruang A", state.roomName)
        assertEquals(3, state.items.size)
        assertEquals(3, state.totalItems)
        assertEquals(0, state.scoredItems)
        assertEquals(0, state.validItems)
        assertFalse(state.submitEnabled)
        assertFalse(state.isLoading)

        // All items start unscored
        state.items.forEach { assertEquals(-1, it.skor) }
    }

    @Test
    fun `init groups items by kategori`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)

        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        val grouped = viewModel.uiState.value.groupedItems
        assertEquals(2, grouped.size)
        assertEquals(2, grouped["Furnitur"]?.size) // Meja, Kursi
        assertEquals(1, grouped["Struktur"]?.size)  // Lantai
    }

    @Test
    fun `init with draftId resumes from draft`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        val draftStates = listOf(
            ItemState(itemId = 1, nama = "Meja", kategori = "Furnitur", skor = 2),
            ItemState(itemId = 2, nama = "Kursi", kategori = "Furnitur", skor = 0, fotoPaths = listOf("/photo1.jpg")),
        )
        coEvery { inspectionRepository.draftToItemStates(5L, sampleItems) } returns (10L to draftStates)

        viewModel.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(10L, state.roomId) // roomId from draft, not init param
        assertEquals(2, state.items.size)
        assertEquals(2, state.items.find { it.itemId == 1L }?.skor)
        assertEquals(0, state.items.find { it.itemId == 2L }?.skor)
        assertEquals(listOf("/photo1.jpg"), state.items.find { it.itemId == 2L }?.fotoPaths)
    }

    // ── updateScore() ──

    @Test
    fun `updateScore sets skor and recalculates counts`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 2)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.find { it.itemId == 1L }?.skor)
        assertEquals(1, state.scoredItems)
        assertEquals(1, state.validItems) // skor 2 = valid
    }

    @Test
    fun `updateScore accepts -1 to reset to unscored`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 1) // set to 1
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.items.find { it.itemId == 1L }?.skor)

        // ScoreIndicator mengirim -1 saat toggle (bukan ViewModel yang toggle)
        viewModel.updateScore(1, -1)
        advanceUntilIdle()
        assertEquals(-1, viewModel.uiState.value.items.find { it.itemId == 1L }?.skor)
    }

    @Test
    fun `updateScore with 0 makes item invalid without photo`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 0) // Berisiko, belum ada foto
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.find { it.itemId == 1L }
        assertEquals(0, item?.skor)
        assertFalse(item?.isValid ?: true)
        assertFalse(viewModel.uiState.value.submitEnabled)
    }

    // ── addPhoto / deletePhoto ──

    @Test
    fun `addPhoto appends photo path and makes skor-0 item valid`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 0) // Berisiko
        viewModel.addPhoto(1, "/photo/a.jpg")
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.find { it.itemId == 1L }
        assertEquals(listOf("/photo/a.jpg"), item?.fotoPaths)
        assertTrue(item?.isValid ?: false)
    }

    @Test
    fun `addPhoto supports multiple photos per item`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.addPhoto(1, "/photo/a.jpg")
        viewModel.addPhoto(1, "/photo/b.jpg")
        viewModel.addPhoto(1, "/photo/c.jpg")

        assertEquals(3, viewModel.uiState.value.items.find { it.itemId == 1L }?.fotoPaths?.size)
    }

    @Test
    fun `deletePhoto removes photo path from item`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.addPhoto(1, "/photo/a.jpg")
        viewModel.addPhoto(1, "/photo/b.jpg")
        viewModel.deletePhoto(1, "/photo/a.jpg")

        assertEquals(listOf("/photo/b.jpg"), viewModel.uiState.value.items.find { it.itemId == 1L }?.fotoPaths)
    }

    @Test
    fun `deleting last photo from skor-0 item makes it invalid again`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 0)
        viewModel.addPhoto(1, "/photo/a.jpg")
        viewModel.deletePhoto(1, "/photo/a.jpg")

        val item = viewModel.uiState.value.items.find { it.itemId == 1L }
        assertFalse(item?.isValid ?: true) // skor 0, no photos = invalid
    }

    // ── updateCatatan ──

    @Test
    fun `updateCatatan sets catatan on item`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateCatatan(1, "Perlu diperbaiki")

        assertEquals("Perlu diperbaiki", viewModel.uiState.value.items.find { it.itemId == 1L }?.catatan)
    }

    @Test
    fun `updateCatatan with blank sets catatan to null`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateCatatan(1, "Catatan")
        viewModel.updateCatatan(1, "   ")

        assertNull(viewModel.uiState.value.items.find { it.itemId == 1L }?.catatan)
    }

    // ── saveDraft ──

    @Test
    fun `saveDraft inserts draft with DRAFT status`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.saveDraft()
        advanceUntilIdle()

        coVerify { drafDao.insertDraft(withArg { assertEquals("DRAFT", it.status) }) }
        assertTrue(viewModel.uiState.value.draftSaved)
    }

    @Test
    fun `saveDraft inserts items and photos for all itemStates`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.addPhoto(1, "/photo.jpg")
        viewModel.saveDraft()
        advanceUntilIdle()

        // 3 items inserted
        coVerify(exactly = 3) { drafDao.insertItem(any()) }
        // 1 photo inserted
        coVerify(exactly = 1) { drafDao.insertPhoto(any()) }
    }

    // ── submit ──

    @Test
    fun `submit does nothing when submitEnabled is false`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returns 1L
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.submit() // submitEnabled is false — no items scored
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.draftSaved)
        coVerify(exactly = 0) { drafDao.insertDraft(any()) } // No DB write
    }

    @Test
    fun `submit saves with PENDING_SYNC status when all items valid`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        // Score all items valid (skor 2 = no photo needed)
        viewModel.updateScore(1, 2)
        viewModel.updateScore(2, 2)
        viewModel.updateScore(3, 2)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.submitEnabled)

        viewModel.submit()
        advanceUntilIdle()

        // Verify PENDING_SYNC status — match() tersedia implicit di coVerify scope
        coVerify {
            drafDao.insertDraft(match { it.status == "PENDING_SYNC" })
        }
        assertTrue(viewModel.uiState.value.draftSaved)
    }

    // ── submitEnabled validation ──

    @Test
    fun `submitEnabled is false when any item unscored`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 2)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.submitEnabled) // item 2 & 3 unscored
    }

    @Test
    fun `submitEnabled is true when all items valid`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 2)
        viewModel.updateScore(2, 1)
        viewModel.updateScore(3, 2)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.submitEnabled)
        assertEquals(3, viewModel.uiState.value.validItems)
        assertEquals(3, viewModel.uiState.value.totalItems)
    }

    @Test
    fun `skor 0 with no photo prevents submit`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 2) // Sesuai
        viewModel.updateScore(2, 0) // Berisiko — butuh foto
        viewModel.updateScore(3, 2) // Sesuai
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.submitEnabled)
    }

    @Test
    fun `skor 0 becomes valid after adding photo`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 2)
        viewModel.updateScore(2, 0) // Butuh foto
        viewModel.updateScore(3, 2)

        viewModel.addPhoto(2, "/evidence.jpg") // Tambah foto
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.submitEnabled)
    }

    // ── clearDraftSaved ──

    @Test
    fun `clearDraftSaved resets draftSaved flag`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returns 1L
        coEvery { drafDao.insertItem(any()) } returns 1L
        coEvery { drafDao.insertPhoto(any()) } returns 1L
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.saveDraft()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.draftSaved)

        viewModel.clearDraftSaved()
        assertFalse(viewModel.uiState.value.draftSaved)
    }

    // ── init edge cases ──

    @Test
    fun `init with empty items shows no items`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(emptyList())

        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.items.size)
        assertEquals(0, state.totalItems)
        assertFalse(state.submitEnabled)
        assertTrue(state.groupedItems.isEmpty())
    }

    // ── Mixed validation scenarios ──

    @Test
    fun `score 1 and 2 are valid even without photos`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 1) // Minor — no photo needed
        viewModel.updateScore(2, 2) // Sesuai — no photo needed
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.items.find { it.itemId == 1L }?.isValid ?: false)
        assertTrue(viewModel.uiState.value.items.find { it.itemId == 2L }?.isValid ?: false)
    }

    @Test
    fun `item state transitions maintain correct isValid`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.find { it.itemId == 1L }

        // Default: unscored, invalid
        assertFalse(item?.isValid ?: true)

        // Score 2: valid
        viewModel.updateScore(1, 2)
        assertTrue(viewModel.uiState.value.items.find { it.itemId == 1L }?.isValid ?: false)

        // Score 0 without photo: invalid
        viewModel.updateScore(1, 0)
        assertFalse(viewModel.uiState.value.items.find { it.itemId == 1L }?.isValid ?: true)

        // Photo added: valid
        viewModel.addPhoto(1, "/photo.jpg")
        assertTrue(viewModel.uiState.value.items.find { it.itemId == 1L }?.isValid ?: false)

        // Score changed to 2 (photo kept): valid
        viewModel.updateScore(1, 2)
        assertTrue(viewModel.uiState.value.items.find { it.itemId == 1L }?.isValid ?: false)

        // Score back to 0 (photo still exists from earlier): valid
        viewModel.updateScore(1, 0)
        assertTrue(viewModel.uiState.value.items.find { it.itemId == 1L }?.isValid ?: false)
    }
}
