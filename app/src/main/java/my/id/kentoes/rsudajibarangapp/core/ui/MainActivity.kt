package my.id.kentoes.rsudajibarangapp.core.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import my.id.kentoes.rsudajibarangapp.core.network.NetworkConnectivityObserver
import my.id.kentoes.rsudajibarangapp.core.navigation.NavGraph
import my.id.kentoes.rsudajibarangapp.core.ui.theme.RsuAppTheme
import my.id.kentoes.rsudajibarangapp.inspection.ui.components.OfflineBanner
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var connectivityObserver: NetworkConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RsuAppTheme {
                val isOnline by connectivityObserver.isOnline.collectAsState(initial = true)
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
