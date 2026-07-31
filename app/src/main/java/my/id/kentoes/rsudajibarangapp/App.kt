package my.id.kentoes.rsudajibarangapp

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import my.id.kentoes.rsudajibarangapp.sync.DraftPhotoCleanupWorker
import my.id.kentoes.rsudajibarangapp.sync.SyncAwareWorkerFactory
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: SyncAwareWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Cleanup periodik foto draf yatim — kebersihan storage jangka panjang
        DraftPhotoCleanupWorker.schedule(this)
    }
}
