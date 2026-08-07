package my.id.kentoes.rsudajibarangapp.inspection.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import my.id.kentoes.rsudajibarangapp.BuildConfig
import my.id.kentoes.rsudajibarangapp.core.model.inspectionStatusLabel
import my.id.kentoes.rsudajibarangapp.inspection.InspectionHistoryViewModel
import my.id.kentoes.rsudajibarangapp.sync.api.PhotoOutDto
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionDetailScreen(
    inspectionId: Long,
    onNavigateBack: () -> Unit,
    onReinspection: (roomId: Long, roomName: String) -> Unit = { _, _ -> },
    viewModel: InspectionHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(inspectionId) {
        viewModel.loadDetail(inspectionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Inspeksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearDetail()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoadingDetail -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.selectedDetail == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Detail tidak ditemukan", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                val detail = uiState.selectedDetail!!
                // UX Phase 7: warna token M3 (bukan hex) + label status via inspectionStatusLabel()
                val statusColor = when (detail.status) {
                    "APPROVED" -> MaterialTheme.colorScheme.secondary
                    "REJECTED" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.tertiary
                }
                // Ringkasan skor dari details[].score — data existing, tanpa endpoint baru
                val scoreCounts = detail.details.groupingBy { it.score }.eachCount()
                val berisiko = scoreCounts[0] ?: 0
                val minor = scoreCounts[1] ?: 0
                val sesuai = scoreCounts[2] ?: 0
                val totalScored = detail.details.size

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    // Header card — status, room, inspector, date. Depth via surfaceContainerHigh
                    // + chip status berwarna (bukan tint alpha 8% yang wash-out).
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(statusColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (detail.status) {
                                                "APPROVED" -> Icons.Default.CheckCircle
                                                "REJECTED" -> Icons.Default.Warning
                                                else -> Icons.Default.HourglassEmpty
                                            },
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(detail.status.inspectionStatusLabel(), fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium)
                                        Text(uiState.inspectorName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    detail.businessDate?.let {
                                        Text(it.take(10), style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text("Room: ${uiState.detailRoomName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                // Ringkasan skor — informatif (Phase 7): berapa item Berisiko/Minor/Sesuai
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    ScoreCountText("$berisiko Berisiko", MaterialTheme.colorScheme.error)
                                    ScoreCountText("$minor Minor", MaterialTheme.colorScheme.tertiary)
                                    ScoreCountText("$sesuai Sesuai", MaterialTheme.colorScheme.secondary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        if (totalScored > 0) sesuai.toFloat() / totalScored else 0f
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "$sesuai dari $totalScored item sesuai standar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                detail.rejectionReason?.let { reason ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Alasan ditolak: $reason",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    // Aksi: inspeksi ulang room yang sama dengan form KOSONG (bukan resume draf)
                    item {
                        FilledTonalButton(
                            onClick = { onReinspection(detail.roomId, uiState.detailRoomName.ifBlank { "Ruangan #${detail.roomId}" }) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Inspeksi Ulang", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    // Detail items
                    item {
                        Text("Item Inspeksi (${detail.details.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }

                    items(detail.details) { itemDetail ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(itemDetail.itemNameSnapshot,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        when (itemDetail.score) {
                                            0 -> "Berisiko"
                                            1 -> "Minor"
                                            2 -> "Sesuai"
                                            else -> "-"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = when (itemDetail.score) {
                                            0 -> MaterialTheme.colorScheme.error
                                            1 -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.secondary
                                        }
                                    )
                                }
                                // Photo thumbnails — ADR-0016: lokal-first, fallback URL server
                                if (itemDetail.photos.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(itemDetail.photos, key = { it.id }) { photo ->
                                            PhotoThumbnailCard(
                                                photo = photo,
                                                localPath = uiState.detailPhotoLocalPaths[photo.id],
                                                isReuploading = uiState.isReuploading,
                                                onReupload = { viewModel.reuploadPhoto(inspectionId, photo.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreCountText(count: String, color: Color) {
    Text(
        text = count,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun PhotoThumbnailCard(
    photo: PhotoOutDto,
    localPath: String?,
    isReuploading: Boolean,
    onReupload: () -> Unit
) {
    // ADR-0016: lokal-first — tampilkan file backup di photos_sent jika ada, fallback URL server.
    // Menjawab masalah "foto tidak muncul di riwayat" (file lokal sudah dihapus sistem dulu).
    val localFile = localPath?.let { File(it) }?.takeIf { it.exists() }
    // ponytail: URL path assumes BE serves uploads at <BASE_URL>/uploads/ — adjust if BE uses CDN/signed URL
    val imageModel: Any = localFile ?: "${BuildConfig.BASE_URL}uploads/${photo.photoFileName}"
    Card(
        modifier = Modifier.size(96.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fallback icon — shows when AsyncImage fails (image is transparent on failure)
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp)
            )
            AsyncImage(
                model = imageModel,
                contentDescription = "Foto inspeksi",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
            // Re-upload dari backup lokal (ADR-0016) — hanya jika backup masih ada
            if (localFile != null) {
                IconButton(
                    onClick = onReupload,
                    enabled = !isReuploading,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    if (isReuploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Re-upload foto dari backup lokal",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
