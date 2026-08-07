package my.id.kentoes.rsudajibarangapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Palet RSUD Medical Green — DIKUNCI (keputusan 2026-08): dynamic color / Material You dimatikan
// agar warna konsisten di semua device — warna status harus bisa diprediksi di aplikasi
// kerja lapangan. Seed: HIJAU MEDIS (primary — identitas RSUD Ajibarang, #16A34A),
// hijau muda (secondary — sesuai/approved), kuning (tertiary — pending/menunggu),
// merah (error/danger). Background #F5FAF7 (light green-white).
// Semua container role + surfaceContainer diisi agar depth kartu terlihat & kontras aman light/dark.
private val LightColors = lightColorScheme(
    primary = Color(0xFF16A34A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBF7D0),
    onPrimaryContainer = Color(0xFF002106),
    secondary = Color(0xFF22C55E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA7F3D0),
    onSecondaryContainer = Color(0xFF002109),
    tertiary = Color(0xFFF59E0B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF451A03),
    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF450A0A),
    background = Color(0xFFF5FAF7),
    onBackground = Color(0xFF1F2937),
    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFF9CA3AF),
    outlineVariant = Color(0xFFD1D5DB),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0FDF4),
    surfaceContainer = Color(0xFFECFDF5),
    surfaceContainerHigh = Color(0xFFD1FAE5),
    surfaceContainerHighest = Color(0xFFA7F3D0)
)

/** Warna sukses khusus — tidak ada role resmi M3, tapi dipakai untuk badge "Selesai". */
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenLight = Color(0xFFD1FAE5)
val SuccessGreenDark = Color(0xFF065F46)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4ADE80),
    onPrimary = Color(0xFF00390A),
    primaryContainer = Color(0xFF15803D),
    onPrimaryContainer = Color(0xFFBBF7D0),
    secondary = Color(0xFF86EFAC),
    onSecondary = Color(0xFF002109),
    secondaryContainer = Color(0xFF15803D),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFFFCD34D),
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF92400E),
    onTertiaryContainer = Color(0xFFFEF3C7),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1E3A2F),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF6B7280),
    outlineVariant = Color(0xFF374151),
    surfaceContainerLowest = Color(0xFF0F172A),
    surfaceContainerLow = Color(0xFF162032),
    surfaceContainer = Color(0xFF1E293B),
    surfaceContainerHigh = Color(0xFF263244),
    surfaceContainerHighest = Color(0xFF334155)
)

/**
 * Shape tokens M3 — konsisten dengan radius kartu yang sudah dipakai (8–16dp).
 * Kardinalitas card/form mengikuti token ini agar tampilan seragam di seluruh layar.
 */
private val RsuShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun RsuAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = RsuShapes,
        content = content
    )
}
