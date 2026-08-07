package my.id.kentoes.rsudajibarangapp.dashboard

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import my.id.kentoes.rsudajibarangapp.dashboard.components.RecentDraftCard
import my.id.kentoes.rsudajibarangapp.dashboard.components.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUser: UserOut?,
    onNavigateToDrafts: () -> Unit,
    onOpenRoomForm: (Long, String) -> Unit,
    onResumeDraft: (Long) -> Unit,
    onInspectionClick: (Long) -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dashboard", fontWeight = FontWeight.Bold)
                        Text(
                            // ADR-0017: role kini konstan inspector — header menampilkan nama · username
                            text = if (currentUser != null) {
                                listOfNotNull(currentUser.name?.ifBlank { null }, currentUser.username)
                                    .joinToString(" · ")
                            } else {
                                "RSUD Ajibarang"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    OutlinedButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Keluar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isSyncing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ── Status Sync ──
                    item {
                        SyncStatusBar(
                            isSyncing = uiState.isSyncing,
                            lastSyncAt = uiState.lastSyncAt,
                            syncError = uiState.syncError,
                            onRetry = viewModel::refresh
                        )
                    }

                    // ── Stats Grid ──
                    item {
                        Text(
                            "Ringkasan Inspeksi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.HourglassEmpty,
                                label = "Draf",
                                value = uiState.draftCount.toString(),
                                color = MaterialTheme.colorScheme.primary,
                                onClick = onNavigateToDrafts
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.SyncProblem,
                                label = "Menunggu Kirim",
                                value = uiState.pendingSyncCount.toString(),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.CheckCircle,
                                label = "Terkirim",
                                value = uiState.syncedCount.toString(),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    // ── Status Inspeksi Hari Ini — daftar per-room (UX-02, grill 2026-08) ──
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Status Inspeksi Hari Ini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${uiState.inspectedRoomCount} dari ${uiState.roomStatuses.size} ruangan selesai",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.roomStatuses.isEmpty()) {
                        item {
                            Text(
                                "Belum ada ruangan di-assign — tarik ke bawah untuk sync",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(uiState.roomStatuses, key = { it.roomId }) { status ->
                            RoomStatusRow(
                                item = status,
                                onOpenForm = onOpenRoomForm,
                                onResumeDraft = onResumeDraft,
                                onInspectionClick = onInspectionClick
                            )
                        }
                    }

                    // ── Recent Drafts ──
                    if (uiState.recentDrafts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Aktivitas Terbaru",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        items(uiState.recentDrafts, key = { it.id }) { draft ->
                            RecentDraftCard(draft = draft)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Indikator status sync: "Menyinkronkan..." saat sync berjalan, "Sync gagal — ketuk retry"
 * saat error (klik untuk mencoba lagi), "Terakhir sync: <waktu>" saat sukses.
 */
@Composable
private fun SyncStatusBar(
    isSyncing: Boolean,
    lastSyncAt: String?,
    syncError: String?,
    onRetry: () -> Unit
) {
    val (icon, text, color) = when {
        isSyncing -> Triple(
            Icons.Default.SyncProblem,
            "Menyinkronkan...",
            MaterialTheme.colorScheme.primary
        )
        syncError != null -> Triple(
            Icons.Default.SyncProblem,
            "Sync gagal — ketuk untuk mencoba lagi",
            MaterialTheme.colorScheme.error
        )
        lastSyncAt != null -> Triple(
            Icons.Default.CheckCircle,
            "Terakhir sync: ${lastSyncAt.replace('T', ' ').take(16)}",
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        else -> return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = syncError != null, onClick = onRetry)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

/**
 * Satu baris status per-room di dashboard. Klik menyesuaikan status:
 * BELUM → form baru; DRAF/MENUNGGU_KIRIM → resume draf; sisanya → detail inspeksi.
 * Warna chip memakai token M3 (bukan hex) agar benar di light & dark mode.
 */
@Composable
private fun RoomStatusRow(
    item: RoomStatusItem,
    onOpenForm: (Long, String) -> Unit,
    onResumeDraft: (Long) -> Unit,
    onInspectionClick: (Long) -> Unit
) {
    val (label, icon, color) = when (item.status) {
        RoomStatus.BELUM -> Triple(
            "Belum Diinspeksi", Icons.Default.HourglassEmpty, MaterialTheme.colorScheme.onSurfaceVariant
        )
        RoomStatus.DRAF -> Triple(
            "Draf", Icons.Default.Description, MaterialTheme.colorScheme.primary
        )
        RoomStatus.MENUNGGU_KIRIM -> Triple(
            "Menunggu Kirim", Icons.Default.SyncProblem, MaterialTheme.colorScheme.tertiary
        )
        RoomStatus.MENUNGGU_REVIEW -> Triple(
            "Menunggu Review", Icons.Default.HourglassEmpty, MaterialTheme.colorScheme.tertiary
        )
        RoomStatus.DISETUJUI -> Triple(
            "Disetujui", Icons.Default.CheckCircle, MaterialTheme.colorScheme.secondary
        )
        RoomStatus.DITOLAK -> Triple(
            "Ditolak", Icons.Default.Warning, MaterialTheme.colorScheme.error
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (item.status) {
                    RoomStatus.BELUM -> onOpenForm(item.roomId, item.roomName)
                    RoomStatus.DRAF, RoomStatus.MENUNGGU_KIRIM -> item.draftId?.let(onResumeDraft)
                    else -> item.inspectionId?.let(onInspectionClick)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = item.roomName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.itemCount} item",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}
