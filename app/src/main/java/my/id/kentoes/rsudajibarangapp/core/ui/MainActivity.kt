package my.id.kentoes.rsudajibarangapp.core.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import my.id.kentoes.rsudajibarangapp.core.navigation.BottomNavBar
import my.id.kentoes.rsudajibarangapp.core.navigation.BottomTab
import my.id.kentoes.rsudajibarangapp.core.navigation.NavGraph
import my.id.kentoes.rsudajibarangapp.core.navigation.Routes
import my.id.kentoes.rsudajibarangapp.core.network.NetworkConnectivityObserver
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
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Column(modifier = Modifier.fillMaxSize()) {
                    OfflineBanner(isOffline = !isOnline)
                    Scaffold(
                        bottomBar = {
                            // Tampilkan bottom bar hanya jika authenticated (bukan di layar login)
                            if (currentRoute != null && currentRoute != Routes.LOGIN) {
                                BottomNavBar(
                                    currentRoute = currentRoute,
                                    onTabSelected = { tab ->
                                        when (tab) {
                                            BottomTab.DASHBOARD -> {
                                                navController.navigate(Routes.DASHBOARD) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                            BottomTab.INSPECTION -> {
                                                navController.navigate(Routes.inspectionList()) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                            BottomTab.HISTORY -> {
                                                navController.navigate(Routes.inspectionHistory()) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                            BottomTab.PROFILE -> {
                                                navController.navigate(Routes.PROFILE) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            NavGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
