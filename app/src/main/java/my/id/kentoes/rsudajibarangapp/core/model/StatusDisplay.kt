package my.id.kentoes.rsudajibarangapp.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Display properties untuk status draf inspeksi. Warna TIDAK di sini — ambil
 * [draftStatusColor] (token M3) di call-site agar benar di light & dark mode.
 */
data class StatusDisplay(
    val icon: ImageVector,
    val label: String
)

/** Konversi status string ke [StatusDisplay] — reusable di semua screen. */
fun String.toStatusDisplay(): StatusDisplay {
    return when (this) {
        "DRAFT" -> StatusDisplay(
            icon = Icons.Default.HourglassEmpty,
            label = "Draf"
        )
        "PENDING_SYNC" -> StatusDisplay(
            icon = Icons.Default.SyncProblem,
            label = "Menunggu Kirim"
        )
        "SYNCED" -> StatusDisplay(
            icon = Icons.Default.CheckCircle,
            label = "Terkirim"
        )
        else -> StatusDisplay(
            icon = Icons.Default.Description,
            label = this
        )
    }
}

/** Warna status draf dari token M3 — composable agar ikut dynamic color & dark mode. */
@Composable
fun draftStatusColor(status: String): Color = when (status) {
    "DRAFT" -> MaterialTheme.colorScheme.primary
    "PENDING_SYNC" -> MaterialTheme.colorScheme.tertiary
    "SYNCED" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

/**
 * Label bahasa Indonesia untuk status inspeksi (server-side).
 * Warna TIDAK di sini — pakai token M3 di call-site via `when(status)` (lihat Riwayat/Detail).
 */
fun String.inspectionStatusLabel(): String = when (this) {
    "PENDING" -> "Menunggu Review"
    "APPROVED" -> "Disetujui"
    "REJECTED" -> "Ditolak"
    else -> this
}
