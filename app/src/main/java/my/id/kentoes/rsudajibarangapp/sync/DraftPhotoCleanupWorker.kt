package my.id.kentoes.rsudajibarangapp.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import my.id.kentoes.rsudajibarangapp.inspection.DraftPhotoCleaner
import java.util.concurrent.TimeUnit

/**
 * Worker pembersih foto draf yatim — berjalan periodik (harian) untuk kebersihan
 * storage jangka panjang. Dibuat manual oleh [SyncAwareWorkerFactory] karena
 * `@HiltWorker` tidak diproses KSP di project ini.
 */
class DraftPhotoCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val draftPhotoCleaner: DraftPhotoCleaner
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "draft_photo_cleanup"
        private const val PERIOD_DAYS = 1L
        private const val INITIAL_DELAY_DAYS = 1L

        /** Jadwalkan cleanup periodik — idempotent (UPDATE policy). */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DraftPhotoCleanupWorker>(
                PERIOD_DAYS, TimeUnit.DAYS
            )
                .setInitialDelay(INITIAL_DELAY_DAYS, TimeUnit.DAYS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    override suspend fun doWork(): Result = try {
        draftPhotoCleaner.cleanup()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
