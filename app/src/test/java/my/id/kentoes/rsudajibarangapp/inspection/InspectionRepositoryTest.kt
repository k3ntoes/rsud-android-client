package my.id.kentoes.rsudajibarangapp.inspection

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InspectionRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val drafDao = mockk<DrafDao>(relaxed = true)
    private val masterDataDao = mockk<MasterDataDao>(relaxed = true)
    private val repository = InspectionRepository(drafDao, masterDataDao)

    @Test
    fun `clearForeignDrafts deletes foreign photo files and clears their rows`() = runTest {
        val photo = tempFolder.newFile("draft_photo.jpg")
        coEvery { drafDao.getForeignDraftPhotoPaths("5") } returns listOf(photo.absolutePath)

        repository.clearForeignDrafts("5")

        coVerify(exactly = 1) { drafDao.clearForeignDrafts("5") }
        assertFalse(photo.exists()) // file foto draf akun lama ikut dihapus
    }

    @Test
    fun `clearForeignDrafts clears rows even when no foreign photos exist`() = runTest {
        coEvery { drafDao.getForeignDraftPhotoPaths("5") } returns emptyList()

        repository.clearForeignDrafts("5")

        coVerify(exactly = 1) { drafDao.clearForeignDrafts("5") }
    }

    @Test
    fun `clearForeignDrafts does not throw when photo file is missing`() = runTest {
        coEvery { drafDao.getForeignDraftPhotoPaths("5") } returns listOf("/nonexistent/path/photo.jpg")

        repository.clearForeignDrafts("5") // best-effort — tidak boleh crash

        coVerify(exactly = 1) { drafDao.clearForeignDrafts("5") }
    }

    // ── deleteDraft ──

    @Test
    fun `deleteDraft deletes photo files and clears draft rows`() = runTest {
        val photo = tempFolder.newFile("draft_photo.jpg")
        val draft = DrafInspeksi(id = 5, roomId = 10, localTimestamp = "2026-01-01T00:00:00Z", status = "DRAFT")
        coEvery { drafDao.getDraftById(5L) } returns draft
        coEvery { drafDao.getPhotoPathsForDraft(5L) } returns listOf(photo.absolutePath)

        repository.deleteDraft(5L)

        coVerify(exactly = 1) { drafDao.deleteDraftCascade(draft) }
        assertFalse(photo.exists()) // file foto ikut dihapus — tidak menumpuk file yatim
    }

    @Test
    fun `deleteDraft reads photo paths BEFORE cascade delete`() = runTest {
        // REGRESSION (review 2026-08): query getPhotoPathsForDraft pernah diletakkan
        // SETELAH deleteDraftCascade — padahal CASCADE FK menghapus baris draf_foto,
        // sehingga query mengembalikan kosong dan file foto tidak pernah terhapus.
        val photo = tempFolder.newFile("draft_photo_order.jpg")
        val draft = DrafInspeksi(id = 5, roomId = 10, localTimestamp = "2026-01-01T00:00:00Z", status = "DRAFT")
        coEvery { drafDao.getDraftById(5L) } returns draft
        coEvery { drafDao.getPhotoPathsForDraft(5L) } returns listOf(photo.absolutePath)

        repository.deleteDraft(5L)

        coVerifyOrder {
            drafDao.getPhotoPathsForDraft(5L)
            drafDao.deleteDraftCascade(draft)
        }
        assertFalse(photo.exists())
    }

    @Test
    fun `deleteDraft with deletePhotoFiles false keeps photo files and skips path query`() = runTest {
        // BUG-FIX (2026-08): saat resume draf → submit, draf baru mereferensikan path foto
        // yang SAMA — file harus dipertahankan (deletePhotoFiles = false). Tidak boleh ada
        // query path tambahan (hemat I/O) dan file tidak boleh ikut terhapus.
        val photo = tempFolder.newFile("resume_kept_photo.jpg")
        val draft = DrafInspeksi(id = 5, roomId = 10, localTimestamp = "2026-01-01T00:00:00Z", status = "DRAFT")
        coEvery { drafDao.getDraftById(5L) } returns draft

        repository.deleteDraft(5L, deletePhotoFiles = false)

        coVerify(exactly = 0) { drafDao.getPhotoPathsForDraft(any()) }
        coVerify(exactly = 1) { drafDao.deleteDraftCascade(draft) }
        assertTrue(photo.exists()) // file foto dipertahankan untuk draf baru
    }

    @Test
    fun `deleteDraft does nothing when draft not found`() = runTest {
        coEvery { drafDao.getDraftById(99L) } returns null

        repository.deleteDraft(99L)

        coVerify(exactly = 0) { drafDao.deleteDraftCascade(any()) }
        coVerify(exactly = 0) { drafDao.getPhotoPathsForDraft(any()) }
    }

    @Test
    fun `deleteDraft does not throw when photo file is missing`() = runTest {
        val draft = DrafInspeksi(id = 5, roomId = 10, localTimestamp = "2026-01-01T00:00:00Z", status = "DRAFT")
        coEvery { drafDao.getDraftById(5L) } returns draft
        coEvery { drafDao.getPhotoPathsForDraft(5L) } returns listOf("/nonexistent/path/photo.jpg")

        repository.deleteDraft(5L) // best-effort — tidak boleh crash

        coVerify(exactly = 1) { drafDao.deleteDraftCascade(draft) }
    }

    // ── deleteSyncedDraft ──

    @Test
    fun `deleteSyncedDraft deletes photo files and marks draft synced`() = runTest {
        val photo = tempFolder.newFile("synced_photo.jpg")
        coEvery { drafDao.getPhotoPathsForDraft(7L) } returns listOf(photo.absolutePath)

        repository.deleteSyncedDraft(7L)

        coVerify(exactly = 1) { drafDao.markSyncedAndDelete(7L) }
        assertFalse(photo.exists()) // file foto draf terkirim ikut dihapus
    }

    @Test
    fun `deleteSyncedDraft does not throw when photo file is missing`() = runTest {
        coEvery { drafDao.getPhotoPathsForDraft(7L) } returns listOf("/nonexistent/path/photo.jpg")

        repository.deleteSyncedDraft(7L) // best-effort — tidak boleh crash

        coVerify(exactly = 1) { drafDao.markSyncedAndDelete(7L) }
    }
}
