package my.id.kentoes.rsudajibarangapp.inspection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Skor0Color = Color(0xFFD32F2F)   // Merah — Berisiko
private val Skor1Color = Color(0xFFF9A825)   // Kuning — Minor
private val Skor2Color = Color(0xFF388E3C)   // Hijau — Sesuai

data class ScoreOption(
    val value: Int,
    val label: String,
    val color: Color
)

private val scoreOptions = listOf(
    ScoreOption(0, "Berisiko", Skor0Color),
    ScoreOption(1, "Minor", Skor1Color),
    ScoreOption(2, "Sesuai", Skor2Color)
)

@Composable
fun ScoreIndicator(
    currentScore: Int,
    onScoreSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Skor",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            scoreOptions.forEach { option ->
                val isSelected = currentScore == option.value
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        // Toggle: jika sudah dipilih → reset ke -1 (belum)
                        if (isSelected) onScoreSelected(-1) else onScoreSelected(option.value)
                    },
                    label = {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = option.color
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = option.color.copy(alpha = 0.15f),
                        selectedLabelColor = option.color
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = option.color.copy(alpha = 0.5f),
                        selectedBorderColor = option.color,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
