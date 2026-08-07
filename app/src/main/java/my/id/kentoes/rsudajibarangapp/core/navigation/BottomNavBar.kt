package my.id.kentoes.rsudajibarangapp.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD(Routes.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
    INSPECTION(Routes.INSPECTION_LIST, "Inspeksi", Icons.Default.MeetingRoom),
    HISTORY(Routes.INSPECTION_HISTORY, "Riwayat", Icons.Default.History),
    PROFILE(Routes.PROFILE, "Profil", Icons.Default.Person)
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar {
        BottomTab.entries.forEach { tab ->
            val selected = when (tab) {
                BottomTab.DASHBOARD -> currentRoute == Routes.DASHBOARD
                BottomTab.INSPECTION -> currentRoute?.startsWith("inspection_list") == true
                BottomTab.HISTORY -> currentRoute?.startsWith("inspection_history") == true
                BottomTab.PROFILE -> currentRoute == Routes.PROFILE
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.title) },
                label = { Text(tab.title) }
            )
        }
    }
}
