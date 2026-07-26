package my.id.kentoes.rsudajibarangapp.inspection.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun PhotoThumbnail(
    photoPath: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(80.dp)) {
        AsyncImage(
            model = File(photoPath),
            contentDescription = "Foto bukti",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Hapus foto",
                modifier = Modifier.size(16.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
