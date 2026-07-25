package my.id.kentoes.rsudajibarangapp.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val LOGIN = "login"
    const val INSPECTION_LIST = "inspection_list"
    const val INSPECTION_FORM = "inspection_form/{roomId}"
    const val DRAFT_LIST = "draft_list"

    fun inspectionForm(roomId: String) = "inspection_form/$roomId"
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            PlaceholderScreen("Login")
        }
        composable(Routes.INSPECTION_LIST) {
            PlaceholderScreen("Daftar Inspeksi")
        }
        composable(Routes.INSPECTION_FORM) {
            PlaceholderScreen("Form Inspeksi")
        }
        composable(Routes.DRAFT_LIST) {
            PlaceholderScreen("Draf Tersimpan")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}

@Preview(showBackground = true)
@Composable
private fun NavGraphPreview() {
    NavGraph()
}
