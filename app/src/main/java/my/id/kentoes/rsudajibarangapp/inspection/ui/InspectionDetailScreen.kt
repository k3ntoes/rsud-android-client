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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import my.id.kentoes.rsudajibarangapp.inspection.InspectionHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionDetailScreen(
    inspectionId: Long,
    onNavigateBack: () -> Unit,
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
                val statusColor = when (detail.status) {
                    "APPROVED" -> Color(0xFF388E3C)
                    "REJECTED" -> MaterialTheme.colorScheme.error
                    else -> Color(0xFFF9A825)
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    // Header card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (detail.status) {
                                            "APPROVED" -> Icons.Default.CheckCircle
                                            "REJECTED" -> Icons.Default.Warning
                                            else -> Icons.Default.HourglassEmpty
                                        },
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Status: ${detail.status}", fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium)
                                        detail.businessDate?.let {
                                            Text("Tanggal: ${it.take(10)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                detail.rejectionReason?.let { reason ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Alasan ditolak: $reason",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    // Detail items
                    item {
                        Text("Item Inspeksi", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }

                    items(detail.details) { itemDetail ->
                        Card(modifier = Modifier.fillMaxWidth()) {
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
                                            1 -> Color(0xFFF9A825)
                                            else -> Color(0xFF388E3C)
                                        }
                                    )
                                }
                                if (itemDetail.photos.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${itemDetail.photos.size} foto",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
