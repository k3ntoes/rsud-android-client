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
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RoomItemEntity
import my.id.kentoes.rsudajibarangapp.sync.SyncWorker
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InspectionFormViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

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
    fun `init orders items by sort_order then item_id (ADR-0019)`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        // Pivot room 1: item 3 (sort 0) → item 1 (sort 1) → item 2 (sort 2) — BUKAN urutan abjad
        coEvery { masterDataDao.getAllRoomItems() } returns listOf(
            RoomItemEntity(id = 1, roomId = 1, itemId = 3, sortOrder = 0),
            RoomItemEntity(id = 2, roomId = 1, itemId = 1, sortOrder = 1),
            RoomItemEntity(id = 3, roomId = 1, itemId = 2, sortOrder = 2),
        )

        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        assertEquals(listOf(3L, 1L, 2L), viewModel.uiState.value.items.map { it.itemId })
    }

    @Test
    fun `init breaks sort_order ties by item_id`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        // Item 2 & 3 sama-sama sort 0 → tie-breaker item_id ASC: item 2 dulu, lalu item 3
        coEvery { masterDataDao.getAllRoomItems() } returns listOf(
            RoomItemEntity(id = 1, roomId = 1, itemId = 1, sortOrder = 5),
            RoomItemEntity(id = 2, roomId = 1, itemId = 2, sortOrder = 0),
            RoomItemEntity(id = 3, roomId = 1, itemId = 3, sortOrder = 0),
        )

        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        assertEquals(listOf(2L, 3L, 1L), viewModel.uiState.value.items.map { it.itemId })
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

    @Test
    fun `resume reorders draft items by current pivot and keeps non-pivot items last`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        val draftStates = listOf(
            ItemState(itemId = 1, nama = "Meja", kategori = "Furnitur", skor = 2),
            ItemState(itemId = 2, nama = "Kursi", kategori = "Furnitur", skor = 2),
            ItemState(itemId = 3, nama = "Lantai", kategori = "Struktur", skor = 2),
        )
        coEvery { inspectionRepository.draftToItemStates(5L, any()) } returns (10L to draftStates)
        // Pivot room 10 saat ini: item 3 (sort 0) & item 1 (sort 1); item 2 sudah dilepas admin.
        coEvery { masterDataDao.getAllRoomItems() } returns listOf(
            RoomItemEntity(id = 1, roomId = 10, itemId = 3, sortOrder = 0),
            RoomItemEntity(id = 2, roomId = 10, itemId = 1, sortOrder = 1),
        )

        viewModel.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()

        // Item di pivot diurutkan (sort_order, item_id) → 3, 1. Item non-pivot (2) TETAP di akhir.
        assertEquals(listOf(3L, 1L, 2L), viewModel.uiState.value.items.map { it.itemId })
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
        viewModel.addPhoto(2, "/photo.jpg")
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
        // BUG-FIX (diagnosa 2026-08): resume memanggil deleteDraft dengan deletePhotoFiles = false
        coEvery { inspectionRepository.deleteDraft(5L, deletePhotoFiles = false) } returns Unit
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L

        viewModel.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()

        viewModel.saveDraft()
        advanceUntilIdle()

        // Draft lama dihapus (baris DB; file foto DI-PERTAHANKAN untuk draf baru) dulu,
        // baru insert draft baru
        coVerifyOrder {
            inspectionRepository.deleteDraft(5L, deletePhotoFiles = false)
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
        // BUG-FIX (diagnosa 2026-08): resume memanggil deleteDraft dengan deletePhotoFiles = false
        coEvery { inspectionRepository.deleteDraft(5L, deletePhotoFiles = false) } returns Unit
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L

        viewModel.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()

        // All items already valid from draft (skor 0 + foto, skor 2)
        assertTrue(viewModel.uiState.value.submitEnabled)

        viewModel.submit()
        advanceUntilIdle()

        // Draft lama dihapus (baris DB; file foto DI-PERTAHANKAN untuk draf baru) dulu,
        // baru insert draft baru
        coVerifyOrder {
            inspectionRepository.deleteDraft(5L, deletePhotoFiles = false)
            drafDao.insertDraft(match { it.status == "PENDING_SYNC" })
        }
        assertTrue(viewModel.uiState.value.draftSaved)
    }

    @Test
    fun `submit after resume keeps draft photo files on disk`() = runTest(testDispatcher) {
        // REGRESSION (bug draf macet di PENDING_SYNC / "Menunggu Kirim"): saat resume
        // → submit, deleteDraft menghapus file foto draf LAMA, tapi draf baru mereferensikan
        // path yang sama → file sudah tidak ada → sync selalu gagal upload → draf tidak
        // pernah terkirim. Test ini menggunakan InspectionRepository SUNGGAH (file temp nyata).
        val photo = tempFolder.newFile("resume_photo.jpg")

        val drafDaoReal = mockk<DrafDao>()
        val masterDaoReal = mockk<MasterDataDao>()
        // allDraftsSummary dikonstruksi saat InspectionRepository dibuat — stub flow-nya dulu
        coEvery { drafDaoReal.getAllDrafts() } returns flowOf(emptyList())
        coEvery { masterDaoReal.getAllRooms() } returns flowOf(emptyList())
        val realRepository = InspectionRepository(drafDaoReal, masterDaoReal)

        // Resume dari draf 5: satu item diskor 2 (valid) dengan satu foto
        coEvery { masterDaoReal.getAllItems() } returns flowOf(sampleItems)
        coEvery { masterDaoReal.getAllRoomItems() } returns emptyList()
        coEvery { drafDaoReal.getDraftById(5L) } returns DrafInspeksi(
            id = 5, roomId = 10, localTimestamp = "2026-01-01T00:00:00Z", status = "DRAFT"
        )
        coEvery { drafDaoReal.getItemsForDraft(5L) } returns listOf(
            DrafItem(drafId = 5, itemId = 1, skor = 2, catatan = null)
        )
        coEvery { drafDaoReal.getPhotosForItem(any()) } returns listOf(
            DrafFoto(drafItemId = 1, pathLokal = photo.absolutePath)
        )
        // deleteDraft draf lama: DAO mengembalikan path foto, deleteDraftCascade dipanggil,
        // dan InspectionRepository SUNGGAH akan menghapus FILE-nya.
        coEvery { drafDaoReal.getPhotoPathsForDraft(5L) } returns listOf(photo.absolutePath)
        coEvery { drafDaoReal.deleteDraftCascade(any()) } returns Unit
        // Insert draf baru
        coEvery { drafDaoReal.insertDraft(any()) } returns 100L
        coEvery { drafDaoReal.insertItem(any()) } returns 10L
        coEvery { drafDaoReal.insertPhoto(any()) } returns 1L

        val vm = InspectionFormViewModel(context, masterDaoReal, drafDaoReal, realRepository, authRepository)
        vm.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.submitEnabled)

        vm.submit()
        advanceUntilIdle()

        // Draf baru (PENDING_SYNC) mereferensikan path yang sama — file harus MASIH ADA
        // agar sync bisa meng-upload. Jika dihapus → draf macet selamanya.
        assertTrue("File foto draf harus tetap ada setelah resume-submit", photo.exists())
    }

    @Test
    fun `second save after resume does not try to delete again`() = runTest(testDispatcher) {
        val draftStates = listOf(
            ItemState(itemId = 1, nama = "Meja", kategori = "Furnitur", skor = 2),
        )
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        coEvery { inspectionRepository.draftToItemStates(5L, any()) } returns (10L to draftStates)
        // BUG-FIX (diagnosa 2026-08): resume memanggil deleteDraft dengan deletePhotoFiles = false
        coEvery { inspectionRepository.deleteDraft(5L, deletePhotoFiles = false) } returns Unit
        coEvery { drafDao.insertDraft(any()) } returns 100L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L

        viewModel.init(roomId = 0, roomName = "", draftId = 5L)
        advanceUntilIdle()

        // Save pertama — hapus draft lama (baris DB; file foto dipertahankan) + insert baru
        viewModel.saveDraft()
        advanceUntilIdle()
        coVerify(exactly = 1) { inspectionRepository.deleteDraft(5L, deletePhotoFiles = false) }

        // Clear flag
        viewModel.clearDraftSaved()

        // Save kedua — TIDAK hapus lagi (resumeDraftId sudah null)
        viewModel.saveDraft()
        advanceUntilIdle()
        // Masih 1x, tidak bertambah — verifikasi memakai bentuk 2-arg (sama seperti panggilan produksi)
        coVerify(exactly = 1) { inspectionRepository.deleteDraft(5L, deletePhotoFiles = false) }
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
        // BUG-FIX (diagnosa 2026-08): resume memanggil deleteDraft dengan deletePhotoFiles = false
        coEvery { inspectionRepository.deleteDraft(99L, deletePhotoFiles = false) } returns Unit
        coEvery { drafDao.insertDraft(any()) } returns 200L
        coEvery { drafDao.insertItem(any()) } returns 10L
        coEvery { drafDao.insertPhoto(any()) } returns 1L

        viewModel.init(roomId = 0, roomName = "", draftId = 99L)
        advanceUntilIdle()

        viewModel.saveDraft()
        advanceUntilIdle()

        // Verifikasi bahwa delete dipanggil dengan draft id=99 (bukan id lain)
        coVerify { inspectionRepository.deleteDraft(99L, deletePhotoFiles = false) }
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
    fun `score 2 is valid without photo, score 1 is invalid without photo`() = runTest(testDispatcher) {
        coEvery { masterDataDao.getAllItems() } returns flowOf(sampleItems)
        viewModel.init(roomId = 1, roomName = "Test")
        advanceUntilIdle()

        viewModel.updateScore(1, 1) // Minor — needs photo
        viewModel.updateScore(2, 2) // Sesuai — no photo needed
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.items.find { it.itemId == 1L }?.isValid ?: true)
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
