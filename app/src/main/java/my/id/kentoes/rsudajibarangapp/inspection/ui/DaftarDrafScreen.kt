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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import my.id.kentoes.rsudajibarangapp.core.model.toStatusDisplay
import my.id.kentoes.rsudajibarangapp.inspection.DaftarDrafViewModel
import my.id.kentoes.rsudajibarangapp.inspection.DraftSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarDrafScreen(
    onNavigateBack: () -> Unit,
    onResumeDraft: (draftId: Long) -> Unit,
    onStartInspection: () -> Unit = {},
    viewModel: DaftarDrafViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.deletedMessage, uiState.syncMessage) {
        val msg = uiState.deletedMessage ?: uiState.syncMessage
        msg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Dialog konfirmasi hapus
    uiState.draftToConfirmDelete?.let { draft ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Hapus Draf") },
            text = { Text("Yakin ingin menghapus draf inspeksi \"${draft.roomName}\"? Data tidak bisa dikembalikan.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Draf Tersimpan", fontWeight = FontWeight.Bold)
                        Text(
                            "${uiState.drafts.size} draf",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    val hasPending = uiState.drafts.any { it.status == "PENDING_SYNC" }
                    if (hasPending) {
                        IconButton(
                            onClick = viewModel::triggerSync
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.drafts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Belum ada draf tersimpan",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // UX-06: empty state dengan CTA — pandu aksi berikutnya
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onStartInspection) {
                            Text("Mulai Inspeksi")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(uiState.drafts, key = { it.id }) { draft ->
                        DraftCard(
                            draft = draft,
                            isDeleting = uiState.deletingId == draft.id,
                            onResume = { onResumeDraft(draft.id) },
                            onDelete = { viewModel.requestDelete(draft) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftCard(
    draft: DraftSummary,
    isDeleting: Boolean,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val display = draft.status.toStatusDisplay()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MeetingRoom,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.roomName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = display.icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = display.color
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = display.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = display.color
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = draft.localTimestamp.take(10), // YYYY-MM-DD
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (draft.status != "SYNCED") {
                IconButton(
                    onClick = onResume,
                    enabled = !isDeleting
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Lanjutkan",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                enabled = !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
