package my.id.kentoes.rsudajibarangapp.sync

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import my.id.kentoes.rsudajibarangapp.inspection.DraftPhotoCleaner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [WorkerFactory] kustom yang menangani [SyncWorker] secara manual,
 * lalu mendelegasikan worker lain ke [HiltWorkerFactory].
 *
 * KSP tidak memproses anotasi `@HiltWorker` / `@WorkerKey` dari
 * `androidx.hilt:hilt-work`, sehingga [HiltWorkerFactory] tidak memiliki
 * binding untuk [SyncWorker]. Factory ini menjadi solusi tanpa mengubah
 * build system (tetap pakai KSP).
 */
@Singleton
class SyncAwareWorkerFactory @Inject constructor(
    private val hiltWorkerFactory: HiltWorkerFactory,
    private val syncManager: SyncManager,
    private val draftPhotoCleaner: DraftPhotoCleaner
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        // Worker dibuat manual karena @HiltWorker tidak diproses oleh KSP
        return when (workerClassName) {
            SyncWorker::class.java.name ->
                SyncWorker(appContext, workerParameters, syncManager)
            DraftPhotoCleanupWorker::class.java.name ->
                DraftPhotoCleanupWorker(appContext, workerParameters, draftPhotoCleaner)
            else ->
                // Untuk worker lain, delegasikan ke HiltWorkerFactory
                hiltWorkerFactory.createWorker(appContext, workerClassName, workerParameters)
        }
    }
}
