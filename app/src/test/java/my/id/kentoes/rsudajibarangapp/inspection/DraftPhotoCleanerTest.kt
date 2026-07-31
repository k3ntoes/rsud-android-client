package my.id.kentoes.rsudajibarangapp.inspection

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DraftPhotoCleanerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context = mockk<Context>()
    private val drafDao = mockk<DrafDao>(relaxed = true)

    private fun createCleaner(): DraftPhotoCleaner {
        every { context.getExternalFilesDir(null) } returns tempFolder.root
        return DraftPhotoCleaner(context, drafDao)
    }

    /** Foto draf hidup di subfolder "photos" dari externalFilesDir. */
    private fun photosDir(): File = tempFolder.newFolder("photos")

    private fun newPhotoFile(dir: File, name: String): File =
        File(dir, name).apply { createNewFile() }

    @Test
    fun `cleanup deletes orphaned rows and their files`() = runTest {
        val photo = newPhotoFile(photosDir(), "orphan.jpg")
        coEvery { drafDao.getOrphanedDraftPhotos() } returns listOf(
            DrafFoto(id = 1, drafItemId = 999, pathLokal = photo.absolutePath)
        )
        coEvery { drafDao.deleteOrphanedDraftPhotos() } returns 1
        coEvery { drafDao.getAllReferencedPhotoPaths() } returns emptyList()

        val removed = createCleaner().cleanup()

        coVerify(exactly = 1) { drafDao.deleteOrphanedDraftPhotos() }
        assertFalse(photo.exists()) // file foto orfan ikut dihapus
        assertEquals(1, removed)
    }

    @Test
    fun `cleanup deletes unreferenced files older than grace period`() = runTest {
        val dir = photosDir()
        val oldFile = newPhotoFile(dir, "old_orphan.jpg")
        oldFile.setLastModified(System.currentTimeMillis() - 48L * 60 * 60 * 1000)
        val recentFile = newPhotoFile(dir, "recent_capture.jpg") // lastModified = sekarang
        coEvery { drafDao.getOrphanedDraftPhotos() } returns emptyList()
        coEvery { drafDao.getAllReferencedPhotoPaths() } returns emptyList()

        val removed = createCleaner().cleanup()

        assertFalse(oldFile.exists()) // yatim & tua → dihapus
        assertTrue(recentFile.exists()) // masih dalam grace period → dipertahankan
        assertEquals(1, removed)
    }

    @Test
    fun `cleanup keeps referenced files`() = runTest {
        val referencedFile = newPhotoFile(photosDir(), "referenced.jpg")
        coEvery { drafDao.getOrphanedDraftPhotos() } returns emptyList()
        coEvery { drafDao.getAllReferencedPhotoPaths() } returns listOf(referencedFile.absolutePath)

        createCleaner().cleanup()

        assertTrue(referencedFile.exists()) // masih direferensikan draf_foto → aman
    }

    @Test
    fun `cleanup keeps subdirectories`() = runTest {
        val subDir = File(photosDir(), "nested").apply { mkdirs() }
        coEvery { drafDao.getOrphanedDraftPhotos() } returns emptyList()
        coEvery { drafDao.getAllReferencedPhotoPaths() } returns emptyList()

        createCleaner().cleanup()

        assertTrue(subDir.exists()) // hanya file yang dihapus, bukan direktori
    }

    @Test
    fun `cleanup does not throw when photos dir is missing`() = runTest {
        every { context.getExternalFilesDir(null) } returns tempFolder.newFolder("empty_dir")
        coEvery { drafDao.getOrphanedDraftPhotos() } returns emptyList()
        coEvery { drafDao.getAllReferencedPhotoPaths() } returns emptyList()

        DraftPhotoCleaner(context, drafDao).cleanup() // tidak boleh crash
    }
}
