package my.id.kentoes.rsudajibarangapp.inspection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Warna skor dari token M3 (bukan hex): 0=error, 1=tertiary (amber), 2=secondary (teal). */
@Composable
private fun skorColor(skor: Int): Color = when (skor) {
    0 -> MaterialTheme.colorScheme.error
    1 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.secondary
}

private data class ScoreOption(
    val value: Int,
    val label: String
)

private val scoreOptions = listOf(
    ScoreOption(0, "Berisiko"),
    ScoreOption(1, "Minor"),
    ScoreOption(2, "Sesuai")
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
                val color = skorColor(option.value)
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
                                tint = color
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.15f),
                        selectedLabelColor = color
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = color.copy(alpha = 0.5f),
                        selectedBorderColor = color,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
