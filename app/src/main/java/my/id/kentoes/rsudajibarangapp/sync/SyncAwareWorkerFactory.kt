package my.id.kentoes.rsudajibarangapp.sync

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
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
    private val syncManager: SyncManager
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workParameters: WorkerParameters
    ): ListenableWorker? {
        // SyncWorker dibuat manual karena @HiltWorker tidak diproses oleh KSP
        return if (workerClassName == SyncWorker::class.java.name) {
            SyncWorker(appContext, workParameters, syncManager)
        } else {
            // Untuk worker lain, delegasikan ke HiltWorkerFactory
            hiltWorkerFactory.createWorker(appContext, workerClassName, workParameters)
        }
    }
}
