package my.id.kentoes.rsudajibarangapp.core.database

import androidx.room3.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafItem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Test DAO-level untuk pemilahan draf per akun (clearForeignDrafts) menggunakan
 * Room in-memory di JVM (Robolectric) — memverifikasi semantik SQL yang sebenarnya,
 * bukan hanya mock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DrafDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DrafDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.drafDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    /** Insert draf + 1 item + 1 foto (jika photoPath diberikan). Mengembalikan drafId. */
    private suspend fun insertDraftWithPhoto(inspectorId: String?, photoPath: String?): Long {
        val draftId = dao.insertDraft(
            DrafInspeksi(
                roomId = 1,
                localTimestamp = "2026-07-30T10:00:00Z",
                inspectorId = inspectorId
            )
        )
        val itemId = dao.insertItem(
            DrafItem(drafId = draftId, itemId = 100, skor = 2)
        )
        if (photoPath != null) {
            dao.insertPhoto(DrafFoto(drafItemId = itemId, pathLokal = photoPath))
        }
        return draftId
    }

    // ── clearForeignDrafts ──

    @Test
    fun `clearForeignDrafts deletes drafts of other users but keeps own and legacy null`() = runTest {
        val ownId = insertDraftWithPhoto("1", "/photos/own.jpg")
        val foreignId = insertDraftWithPhoto("2", "/photos/foreign.jpg")
        val legacyId = insertDraftWithPhoto(null, "/photos/legacy.jpg")

        dao.clearForeignDrafts("1")

        // Draf user aktif & draf legacy (tanpa inspectorId) dipertahankan
        assertNotNull(dao.getDraftById(ownId))
        assertNotNull(dao.getDraftById(legacyId))
        // Draf milik user lain terhapus
        assertNull(dao.getDraftById(foreignId))
    }

    @Test
    fun `clearForeignDrafts cascades deletion to items and photos of foreign drafts only`() = runTest {
        val ownId = insertDraftWithPhoto("1", "/photos/own.jpg")
        val foreignId = insertDraftWithPhoto("2", "/photos/foreign.jpg")
        val foreignItemId = dao.getItemsForDraft(foreignId).first().id
        val ownItemId = dao.getItemsForDraft(ownId).first().id

        dao.clearForeignDrafts("1")

        // Item & foto milik draf user lain ikut terhapus via CASCADE FK
        assertTrue(dao.getItemsForDraft(foreignId).isEmpty())
        assertTrue(dao.getPhotosForItem(foreignItemId).isEmpty())
        // Item & foto milik draf sendiri tidak terpengaruh
        assertEquals(1, dao.getItemsForDraft(ownId).size)
        assertEquals(1, dao.getPhotosForItem(ownItemId).size)
    }

    @Test
    fun `clearForeignDrafts keeps everything when no foreign drafts exist`() = runTest {
        val ownId = insertDraftWithPhoto("1", "/photos/own.jpg")
        val legacyId = insertDraftWithPhoto(null, "/photos/legacy.jpg")

        dao.clearForeignDrafts("1")

        assertNotNull(dao.getDraftById(ownId))
        assertNotNull(dao.getDraftById(legacyId))
    }

    @Test
    fun `clearForeignDrafts deletes all drafts when inspector matches nothing`() = runTest {
        val foreignId1 = insertDraftWithPhoto("2", "/photos/a.jpg")
        val foreignId2 = insertDraftWithPhoto("3", "/photos/b.jpg")

        dao.clearForeignDrafts("99")

        assertNull(dao.getDraftById(foreignId1))
        assertNull(dao.getDraftById(foreignId2))
        // Semua draf hilang → flow getAllDrafts kosong
        assertTrue(dao.getAllDrafts().first().isEmpty())
    }

    // ── getForeignDraftPhotoPaths ──

    @Test
    fun `getForeignDraftPhotoPaths returns only photos of other users`() = runTest {
        insertDraftWithPhoto("1", "/photos/own.jpg")
        insertDraftWithPhoto("2", "/photos/foreign.jpg")
        insertDraftWithPhoto(null, "/photos/legacy.jpg")

        val paths = dao.getForeignDraftPhotoPaths("1")

        // Hanya foto milik user LAIN — bukan milik user aktif, bukan legacy null
        assertEquals(listOf("/photos/foreign.jpg"), paths)
    }

    @Test
    fun `getForeignDraftPhotoPaths returns empty when no foreign drafts exist`() = runTest {
        insertDraftWithPhoto("1", "/photos/own.jpg")
        insertDraftWithPhoto(null, "/photos/legacy.jpg")

        val paths = dao.getForeignDraftPhotoPaths("1")

        assertTrue(paths.isEmpty())
    }
}
