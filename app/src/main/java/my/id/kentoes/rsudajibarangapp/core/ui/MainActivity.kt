package my.id.kentoes.rsudajibarangapp.core.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import my.id.kentoes.rsudajibarangapp.core.navigation.NavGraph
import my.id.kentoes.rsudajibarangapp.core.ui.theme.RsuAppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RsuAppTheme {
                NavGraph()
            }
        }
    }
}
