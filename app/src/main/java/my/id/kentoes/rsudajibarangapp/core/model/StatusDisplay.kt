package my.id.kentoes.rsudajibarangapp.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Display properties untuk status draf inspeksi.
 */
data class StatusDisplay(
    val color: Color,
    val icon: ImageVector,
    val label: String
)

/** Konversi status string ke [StatusDisplay] — reusable di semua screen. */
fun String.toStatusDisplay(): StatusDisplay {
    return when (this) {
        "DRAFT" -> StatusDisplay(
            color = Color(0xFF1565C0),
            icon = Icons.Default.HourglassEmpty,
            label = "Draf"
        )
        "PENDING_SYNC" -> StatusDisplay(
            color = Color(0xFFF9A825),
            icon = Icons.Default.SyncProblem,
            label = "Menunggu Kirim"
        )
        "SYNCED" -> StatusDisplay(
            color = Color(0xFF388E3C),
            icon = Icons.Default.CheckCircle,
            label = "Terkirim"
        )
        else -> StatusDisplay(
            color = Color.Gray,
            icon = Icons.Default.Description,
            label = this
        )
    }
}
