package my.id.kentoes.rsudajibarangapp.inspection

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.id.kentoes.rsudajibarangapp.auth.AuthRepository
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RoomItemEntity
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
    private val authRepository = mockk<AuthRepository>()
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

        // Default pivot: room 1 memiliki semua sample items (1,2,3) — form tidak kosong
        coEvery { masterDataDao.getAllRoomItems() } returns listOf(
            RoomItemEntity(id = 1, roomId = 1, itemId = 1),
            RoomItemEntity(id = 2, roomId = 1, itemId = 2),
            RoomItemEntity(id = 3, roomId = 1, itemId = 3),
        )

        // Default: belum ada user aktif (inspectorId akan null)
        every { authRepository.currentUser } returns MutableStateFlow(null)

        viewModel = InspectionFormViewModel(context, masterDataDao, drafDao, inspectionRepository, authRepository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ── init() ──

    @Test
    fun `init loads items and sets correct initial state`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        // Room 10 juga punya semua sample items
        coEvery { masterDataDao.getAllRoomItems() } returns listOf(
            RoomItemEntity(id = 1, roomId = 10, itemId = 1),
            RoomItemEntity(id = 2, roomId = 10, itemId = 2),
            RoomItemEntity(id = 3, roomId = 10, itemId = 3),
        )

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
            ItemState(itemId = 1, nama = "Meja", kategori = "Furnitur", deskripsi = "Meja kayu", skor = 2),
            ItemState(itemId = 2, nama = "Kursi", kategori = "Furnitur", skor = 0, fotoPaths = listOf("/photo1.jpg")),
        )
        coEvery { inspectionRepository.draftToItemStates(5L, any()) } returns (10L to draftStates)

        viewModel.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(10L, state.roomId) // roomId from draft, not init param
        assertEquals(2, state.items.size)
        assertEquals(2, state.items.find { it.itemId == 1L }?.skor)
        assertEquals("Meja kayu", state.items.find { it.itemId == 1L }?.deskripsi)
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
    fun `saveDraft stamps inspectorId from the logged-in user`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L
        every { authRepository.currentUser } returns MutableStateFlow(
            UserOut(id = 5, username = "petugas", role = "inspector", isActive = true)
        )
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.saveDraft()
        advanceUntilIdle()

        // inspectorId diambil dari user yang sedang login — dasar pemilahan draf per akun
        coVerify { drafDao.insertDraft(withArg { assertEquals("5", it.inspectorId) }) }
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

    // ── Pivot kosong = form kosong (keputusan review 2026-08) ──

    @Test
    fun `init with empty pivot shows no items even when master items exist`() = runTest(testDispatcher) {
        // Master items ADA, tapi pivot room_items KOSONG → form TIDAK boleh fallback "tampilkan semua"
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { masterDataDao.getAllRoomItems() } returns emptyList()

        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.items.size)
        assertEquals(0, state.totalItems)
        assertFalse(state.submitEnabled)
    }

    @Test
    fun `init filters items to only those in pivot for the room`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        // Room 1 hanya punya item 1 & 2 di pivot — item 3 tidak terasosiasi
        coEvery { masterDataDao.getAllRoomItems() } returns listOf(
            RoomItemEntity(id = 1, roomId = 1, itemId = 1),
            RoomItemEntity(id = 2, roomId = 1, itemId = 2),
        )

        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.items.size)
        assertTrue(viewModel.uiState.value.items.none { it.itemId == 3L })
    }

    // ── Deskripsi item dari entity (keputusan review 2026-08) ──

    @Test
    fun `init carries deskripsi from master item into form state`() = runTest(testDispatcher) {
        val itemsWithDescription = listOf(
            MasterDataItem(id = 1, nama = "Meja", kategori = "Furnitur", deskripsi = "Meja kayu jati"),
            MasterDataItem(id = 2, nama = "Kursi", kategori = "Furnitur", deskripsi = null),
        )
        coEvery { masterDataDao.getAllItems() } returns flowOf(itemsWithDescription)
        coEvery { masterDataDao.getAllRoomItems() } returns listOf(
            RoomItemEntity(id = 1, roomId = 1, itemId = 1),
            RoomItemEntity(id = 2, roomId = 1, itemId = 2),
        )

        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        // Deskripsi dari entity, bukan null hardcoded; item tanpa deskripsi tetap null
        assertEquals("Meja kayu jati", viewModel.uiState.value.items.find { it.itemId == 1L }?.deskripsi)
        assertNull(viewModel.uiState.value.items.find { it.itemId == 2L }?.deskripsi)
    }

    // ── Mixed validation scenarios ──

    // ── Resume draft & duplicate prevention (regression) ──

    @Test
    fun `saveDraft after init with draftId deletes old draft first`() = runTest(testDispatcher) {
        val draftStates = listOf(
            ItemState(itemId = 1, nama = "Meja", kategori = "Furnitur", skor = 2),
        )
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { inspectionRepository.draftToItemStates(5L, any()) } returns (10L to draftStates)
        coEvery { inspectionRepository.deleteDraft(5L) } returns Unit
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L

        viewModel.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()

        viewModel.saveDraft()
        advanceUntilIdle()

        // Draft lama dihapus (baris + file foto) dulu, baru insert draft baru
        coVerifyOrder {
            inspectionRepository.deleteDraft(5L)
            drafDao.insertDraft(match { it.status == "DRAFT" })
        }
        assertTrue(viewModel.uiState.value.draftSaved)
    }

    @Test
    fun `submit after init with draftId deletes old draft first`() = runTest(testDispatcher) {
        val draftStates = listOf(
            ItemState(itemId = 1, nama = "Meja", kategori = "Furnitur", skor = 2, fotoPaths = listOf("/photo.jpg")),
            ItemState(itemId = 2, nama = "Kursi", kategori = "Furnitur", skor = 0, fotoPaths = listOf("/pic.jpg")),
            ItemState(itemId = 3, nama = "Lantai", kategori = "Struktur", skor = 2),
        )
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { inspectionRepository.draftToItemStates(5L, any()) } returns (10L to draftStates)
        coEvery { inspectionRepository.deleteDraft(5L) } returns Unit
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L

        viewModel.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()

        // All items already valid from draft (skor 0 + foto, skor 2)
        assertTrue(viewModel.uiState.value.submitEnabled)

        viewModel.submit()
        advanceUntilIdle()

        // Draft lama dihapus (baris + file foto) dulu, baru insert draft baru
        coVerifyOrder {
            inspectionRepository.deleteDraft(5L)
            drafDao.insertDraft(match { it.status == "PENDING_SYNC" })
        }
        assertTrue(viewModel.uiState.value.draftSaved)
    }

    @Test
    fun `second save after resume does not try to delete again`() = runTest(testDispatcher) {
        val draftStates = listOf(
            ItemState(itemId = 1, nama = "Meja", kategori = "Furnitur", skor = 2),
        )
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { inspectionRepository.draftToItemStates(5L, any()) } returns (10L to draftStates)
        coEvery { inspectionRepository.deleteDraft(5L) } returns Unit
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L

        viewModel.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()

        // Save pertama — hapus draft lama (baris + file foto) + insert baru
        viewModel.saveDraft()
        advanceUntilIdle()
        coVerify(exactly = 1) { inspectionRepository.deleteDraft(5L) }

        // Clear flag
        viewModel.clearDraftSaved()

        // Save kedua — TIDAK hapus lagi (resumeDraftId sudah null)
        viewModel.saveDraft()
        advanceUntilIdle()
        coVerify(exactly = 1) { inspectionRepository.deleteDraft(5L) } // masih 1x, tidak bertambah
        coVerify(exactly = 2) { drafDao.insertDraft(any()) } // 2 insert: 1 dari save pertama, 1 dari save kedua
    }

    @Test
    fun `save without resumeDraftId does not delete any draft`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.saveDraft()
        advanceUntilIdle()

        // Tidak ada resume — deleteDraft tidak boleh dipanggil
        coVerify(exactly = 0) { inspectionRepository.deleteDraft(any()) }
        coVerify(exactly = 1) { drafDao.insertDraft(any()) }
    }

    @Test
    fun `saveDraft after resume deletes correct old draft id`() = runTest(testDispatcher) {
        val draftStates = listOf(
            ItemState(itemId = 1, nama = "Meja", kategori = "Furnitur", skor = 1),
            ItemState(itemId = 2, nama = "Kursi", kategori = "Furnitur", skor = 1),
        )
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { inspectionRepository.draftToItemStates(99L, any()) } returns (10L to draftStates)
        coEvery { inspectionRepository.deleteDraft(99L) } returns Unit
        coEvery { drafDao.insertDraft(any()) } returns 200L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L

        viewModel.init(roomId = 0, roomName = "", draftId = 99L)
        advanceUntilIdle()

        viewModel.saveDraft()
        advanceUntilIdle()

        // Verifikasi bahwa delete dipanggil dengan draft id=99 (bukan id lain)
        coVerify { inspectionRepository.deleteDraft(99L) }
    }

    // ── isSaving guard (cegah double click) ──

    @Test
    fun `saveDraft called twice only inserts once`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returnsMany listOf(100L, 200L, 300L)
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        // Panggil saveDraft 2x berturut-turut (simulasi double klik cepat)
        viewModel.saveDraft()
        viewModel.saveDraft()
        advanceUntilIdle()

        // isSaving mencegah save ke-2 — hanya 1 insert
        coVerify(exactly = 1) { drafDao.insertDraft(any()) }
        assertTrue(viewModel.uiState.value.draftSaved)
    }

    @Test
    fun `submit called twice only inserts once`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returnsMany listOf(100L, 200L, 300L)
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        // Score all items valid
        viewModel.updateScore(1, 2)
        viewModel.updateScore(2, 2)
        viewModel.updateScore(3, 2)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.submitEnabled)

        // Panggil submit 2x berturut-turut
        viewModel.submit()
        viewModel.submit()
        advanceUntilIdle()

        // isSaving mencegah submit ke-2 — hanya 1 insert
        coVerify(exactly = 1) { drafDao.insertDraft(any()) }
        assertTrue(viewModel.uiState.value.draftSaved)
    }

    @Test
    fun `saveDraft after previous save completes can save again`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { drafDao.insertDraft(any()) } returnsMany listOf(100L, 200L)
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        // Save pertama — sukses
        viewModel.saveDraft()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.draftSaved)
        coVerify(exactly = 1) { drafDao.insertDraft(any()) }

        // Clear flag
        viewModel.clearDraftSaved()

        // Save kedua — isSaving sudah false setelah coroutine selesai
        viewModel.saveDraft()
        advanceUntilIdle()
        coVerify(exactly = 2) { drafDao.insertDraft(any()) } // total 2 insert
    }

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
