package my.id.kentoes.rsudajibarangapp.inspection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemCard(
    itemNumber: Int,
    itemId: Long,
    nama: String,
    deskripsi: String?,
    currentScore: Int,
    fotoPaths: List<String>,
    currentCatatan: String?,
    onScoreSelected: (Int) -> Unit,
    onAddPhoto: () -> Unit,
    onDeletePhoto: (String) -> Unit,
    onCatatanChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Aksen status: garis vertikal di sisi kiri kartu mengikuti skor — peta visual
    // sekilas (belum diskor = netral, Berisiko = error, Minor = tertiary, Sesuai = secondary).
    // Kartu PUTIH (surface) di atas page abu → kontras, bukan abu di atas abu.
    val accentColor = when (currentScore) {
        0 -> MaterialTheme.colorScheme.error
        1 -> MaterialTheme.colorScheme.tertiary
        2 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = itemNumber.toString(),
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
            // Header logic untuk badge
            val badgeColor = when {
                (currentScore == 0 || currentScore == 1) && fotoPaths.isEmpty() -> MaterialTheme.colorScheme.error
                (currentScore == 0 || currentScore == 1) && fotoPaths.isNotEmpty() -> androidx.compose.ui.graphics.Color(0xFF22C55E)
                currentScore == 2 -> androidx.compose.ui.graphics.Color(0xFF22C55E)
                else -> androidx.compose.ui.graphics.Color.Transparent
            }
            val badgeText = when {
                (currentScore == 0 || currentScore == 1) && fotoPaths.isEmpty() -> "Wajib Foto"
                (currentScore == 0 || currentScore == 1) && fotoPaths.isNotEmpty() -> "Selesai"
                currentScore == 2 -> "Selesai"
                else -> ""
            }
            if (currentScore != -1) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            // Nama item
            Text(
                text = nama,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!deskripsi.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = deskripsi,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score indicator
            ScoreIndicator(
                currentScore = currentScore,
                onScoreSelected = onScoreSelected
            )

            // Validasi: skor 0 wajib foto
            if ((currentScore == 0 || currentScore == 1) && fotoPaths.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Wajib diisi jika skor 0 atau 1",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Foto
            Text(
                text = "Foto Bukti (${fotoPaths.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fotoPaths.forEach { path ->
                    PhotoThumbnail(
                        photoPath = path,
                        onDelete = { onDeletePhoto(path) }
                    )
                }

                if (fotoPaths.size < 5) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable(onClick = onAddPhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Ambil foto", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Text("Tambah", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Catatan — UX-04: OutlinedTextField default unfocused (border outline + label
            // onSurfaceVariant + container transparan) terbaca sebagai DISABLED. Kasih fill
            // surfaceContainerHighest + border outline tegas agar jelas interaktif.
            var catatanText by remember(currentCatatan) { mutableStateOf(currentCatatan ?: "") }
            OutlinedTextField(
                value = catatanText,
                onValueChange = {
                    catatanText = it
                    onCatatanChanged(it)
                },
                label = { Text("Catatan (opsional)") },
                placeholder = { Text("Tulis detail temuan...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                singleLine = false,
                shape = RoundedCornerShape(8.dp),
                supportingText = { Text("${catatanText.length}/300") },
                // Fill surfaceContainerHighest kontras dengan card putih → jelas input area.
                // Border outline (gelap) + label normal → tidak lagi terbaca disabled.
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            }
        }
    }
}
