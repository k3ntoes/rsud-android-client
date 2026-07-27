package my.id.kentoes.rsudajibarangapp.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import my.id.kentoes.rsudajibarangapp.auth.AuthState
import my.id.kentoes.rsudajibarangapp.auth.AuthViewModel
import my.id.kentoes.rsudajibarangapp.auth.ui.LoginScreen
import my.id.kentoes.rsudajibarangapp.auth.ui.ProfileScreen
import my.id.kentoes.rsudajibarangapp.dashboard.DashboardScreen
import my.id.kentoes.rsudajibarangapp.inspection.InspectionFormScreen
import my.id.kentoes.rsudajibarangapp.inspection.ui.DaftarDrafScreen
import my.id.kentoes.rsudajibarangapp.master.ui.MasterDataListScreen

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
    const val INSPECTION_LIST = "inspection_list"
    const val INSPECTION_FORM = "inspection_form/{roomId}/{roomName}?draftId={draftId}"
    const val DRAFT_LIST = "draft_list"

    fun inspectionForm(roomId: String, roomName: String, draftId: Long? = null): String {
        return if (draftId != null) {
            "inspection_form/$roomId/$roomName?draftId=$draftId"
        } else {
            "inspection_form/$roomId/$roomName"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    // Tampilkan loading screen saat init
    if (authState is AuthState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Routes.DASHBOARD
        else -> Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.INSPECTION_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            val currentUser by authViewModel.currentUser.collectAsState()
            DashboardScreen(
                currentUser = currentUser,
                onNavigateToInspection = {
                    navController.navigate(Routes.INSPECTION_LIST)
                },
                onNavigateToDrafts = {
                    navController.navigate(Routes.DRAFT_LIST)
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.INSPECTION_LIST) {
            MasterDataListScreen(
                onNavigateBack = { navController.popBackStack() },
                onRoomSelected = { roomId, roomName ->
                    navController.navigate(Routes.inspectionForm(roomId.toString(), roomName))
                }
            )
        }
        composable(
            route = Routes.INSPECTION_FORM,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("roomName") { type = NavType.StringType },
                navArgument("draftId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val roomId = it.arguments?.getString("roomId")?.toLongOrNull() ?: 0L
            val roomName = it.arguments?.getString("roomName") ?: ""
            val draftId = it.arguments?.getString("draftId")?.toLongOrNull()
            InspectionFormScreen(
                roomId = roomId,
                roomName = roomName,
                draftId = draftId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.DRAFT_LIST) {
            DaftarDrafScreen(
                onNavigateBack = { navController.popBackStack() },
                onResumeDraft = { draftId ->
                    navController.navigate(Routes.inspectionForm("0", "Resume Draft", draftId))
                }
            )
        }
    }
}

