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

// Palet RSUD yang DIKUNCI (keputusan 2026-08): dynamic color / Material You dimatikan
// agar warna konsisten di semua device — warna status harus bisa diprediksi di aplikasi
// kerja lapangan. Seed: HIJAU (primary — identitas RSUD Ajibarang), teal (secondary —
// Sesuai/approved), amber hangat (tertiary — pending/menunggu/minor), merah (error).
// Semua container role + surfaceContainer diisi agar depth kartu terlihat & kontras
// aman light/dark.
private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF00300C),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = Color(0xFF8A5C00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE082),
    onTertiaryContainer = Color(0xFF2B1F00),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDEE2E7),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF2F2F5),
    surfaceContainer = Color(0xFFECECEF),
    surfaceContainerHigh = Color(0xFFE6E6EA),
    surfaceContainerHighest = Color(0xFFE0E0E4)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF00390A),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF00332E),
    secondaryContainer = Color(0xFF00695C),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFFFFD166),
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF6D5100),
    onTertiaryContainer = Color(0xFFFFE082),
    error = Color(0xFFEF5350),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E),
    surfaceContainerLowest = Color(0xFF141414),
    surfaceContainerLow = Color(0xFF1D1D1D),
    surfaceContainer = Color(0xFF222222),
    surfaceContainerHigh = Color(0xFF292929),
    surfaceContainerHighest = Color(0xFF303030)
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
