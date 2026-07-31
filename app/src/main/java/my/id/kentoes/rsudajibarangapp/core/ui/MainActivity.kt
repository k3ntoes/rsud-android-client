package my.id.kentoes.rsudajibarangapp.core.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import my.id.kentoes.rsudajibarangapp.core.network.NetworkConnectivityObserver
import my.id.kentoes.rsudajibarangapp.core.navigation.NavGraph
import my.id.kentoes.rsudajibarangapp.core.ui.theme.RsuAppTheme
import my.id.kentoes.rsudajibarangapp.inspection.ui.components.OfflineBanner

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val connectivityObserver = remember {
                NetworkConnectivityObserver(applicationContext)
            }
            // Lepas network callback secara eksplisit saat aktivitas dibuang (mis. rotasi) —
            // jangan bergantung pada pembatalan koleksi yang implisit.
            DisposableEffect(connectivityObserver) {
                onDispose { connectivityObserver.close() }
            }
            RsuAppTheme {
                val isOnline by connectivityObserver.isOnline.collectAsState()
                Column(modifier = Modifier.fillMaxSize()) {
                    OfflineBanner(isOffline = !isOnline)
                    Box(modifier = Modifier.weight(1f)) {
                        NavGraph()
                    }
                }
            }
        }
    }
}
