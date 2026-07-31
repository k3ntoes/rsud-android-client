package my.id.kentoes.rsudajibarangapp.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import my.id.kentoes.rsudajibarangapp.inspection.DraftPhotoCleaner
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test unit untuk [DraftPhotoCleanupWorker] — memverifikasi `doWork()` memanggil
 * [DraftPhotoCleaner.cleanup] dan mengembalikan `Result` yang benar.
 *
 * Worker ini punya konstruktor 3-arg (Context, WorkerParameters, cleaner), jadi
 * `TestListenableWorkerBuilder` tidak bisa dipakai (hanya mendukung konstruktor
 * 2-arg `(Context, WorkerParameters)`). Sebagai gantinya `doWork()` dipanggil
 * langsung sebagai suspend function dalam `runTest` — pola standar untuk
 * `CoroutineWorker` — dengan `WorkerParameters` mockk.
 */
class DraftPhotoCleanupWorkerTest {

    private val context = mockk<Context>()
    private val workerParams = mockk<WorkerParameters>()
    private val draftPhotoCleaner = mockk<DraftPhotoCleaner>()

    @Test
    fun `doWork memanggil cleaner dan mengembalikan Result success`() = runTest {
        coEvery { draftPhotoCleaner.cleanup() } returns 3

        val worker = DraftPhotoCleanupWorker(context, workerParams, draftPhotoCleaner)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { draftPhotoCleaner.cleanup() }
    }

    @Test
    fun `doWork mengembalikan Result retry saat cleaner melempar exception`() = runTest {
        coEvery { draftPhotoCleaner.cleanup() } throws RuntimeException("gagal bersihkan")

        val worker = DraftPhotoCleanupWorker(context, workerParams, draftPhotoCleaner)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify(exactly = 1) { draftPhotoCleaner.cleanup() }
    }

    @Test
    fun `WORK_NAME constant is draft_photo_cleanup`() {
        assertEquals("draft_photo_cleanup", DraftPhotoCleanupWorker.WORK_NAME)
    }
}
