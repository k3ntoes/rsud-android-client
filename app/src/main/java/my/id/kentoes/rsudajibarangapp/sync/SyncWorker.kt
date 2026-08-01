package my.id.kentoes.rsudajibarangapp.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "sync_inspection"
        const val NOTIFICATION_CHANNEL_ID = "sync_channel"
        const val NOTIFICATION_ID = 1001

        /** Enqueue sync dengan constraint Network.CONNECTED */
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()

        return try {
            // Sync master data first (hasil parsial dilaporkan internal, tidak dilempar)
            syncManager.syncMasterData()

            val results = syncManager.syncAllPending()

            val successCount = results.count { it.success }
            val failCount = results.size - successCount

            if (results.isEmpty()) {
                // Tidak ada draf pending — worker hanya menjalankan sync master data
                // (mis. pemicu saat login). Tidak perlu notifikasi "0 inspeksi".
                Result.success()
            } else if (failCount == 0) {
                showNotification(
                    "Sinkronisasi Berhasil",
                    "$successCount inspeksi berhasil dikirim"
                )
                Result.success()
            } else {
                showNotification(
                    "Sinkronisasi Sebagian Gagal",
                    "$successCount berhasil, $failCount gagal — akan dicoba lagi"
                )
                if (successCount > 0) Result.success() else Result.retry()
            }
        } catch (e: Exception) {
            showNotification(
                "Sinkronisasi Gagal",
                e.message ?: "Terjadi kesalahan saat sinkronisasi"
            )
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sinkronisasi",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi hasil sinkronisasi inspeksi"
            }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
