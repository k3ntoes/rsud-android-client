package my.id.kentoes.rsudajibarangapp.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import my.id.kentoes.rsudajibarangapp.dashboard.components.RecentDraftCard
import my.id.kentoes.rsudajibarangapp.dashboard.components.StatCard
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUser: UserOut?,
    onNavigateToDrafts: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenRoomForm: (Long, String) -> Unit,
    onResumeDraft: (Long, String) -> Unit,
    onInspectionClick: (Long) -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            DashboardHeader(
                currentUser = currentUser,
                isSyncing = uiState.isSyncing,
                lastSyncAt = uiState.lastSyncAt,
                syncError = uiState.syncError,
                onRetry = viewModel::refresh,
                onLogout = onLogout
            )
        },
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = { onOpenRoomForm(0L, "") }, // Navigate or handled by caller / room selection
                icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                text = { Text("+ Mulai Inspeksi") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
                // 3 Cards Skeleton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }
                // Progress Today Skeleton
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
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
                    // ── Ringkasan Inspeksi — 3 stat card dalam 1 baris ──
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                color = MaterialTheme.colorScheme.tertiary,
                                onClick = onNavigateToDrafts
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.CheckCircle,
                                label = "Terkirim",
                                value = uiState.syncedCount.toString(),
                                color = MaterialTheme.colorScheme.secondary,
                                onClick = onNavigateToHistory
                            )
                        }
                    }

                    // ── Progress Hari Ini ──
                    item {
                        ProgressTodayCard(
                            inspectedCount = uiState.inspectedRoomCount,
                            totalCount = uiState.roomStatuses.size
                        )
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
                        // Prefix key agar tidak bertabrakan dengan key draf di LazyColumn yang
                        // sama (roomId dan draft.id sama-sama mulai dari 1) — tanpa ini aplikasi
                        // crash "Key X was already used" saat draf & status ruangan tampil bareng.
                        items(uiState.roomStatuses, key = { "room_${it.roomId}" }) { status ->
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

                        items(uiState.recentDrafts, key = { "draft_${it.draft.id}" }) { item ->
                            RecentDraftCard(draft = item.draft, roomName = item.roomName)
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
private fun ProgressTodayCard(
    inspectedCount: Int,
    totalCount: Int
) {
    val progress = if (totalCount > 0) inspectedCount.toFloat() / totalCount else 0f
    val percentage = (progress * 100).toInt()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {                    Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Progress Hari Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$inspectedCount dari $totalCount Ruangan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Circular progress
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 6.dp
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RoomStatusRow(
    item: RoomStatusItem,
    onOpenForm: (Long, String) -> Unit,
    onResumeDraft: (Long, String) -> Unit,
    onInspectionClick: (Long) -> Unit
) {
    val (statusLabel, statusColor, actionLabel, actionIcon) = when (item.status) {
        RoomStatus.BELUM -> Quadruple(
            "Belum Diperiksa",
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Mulai",
            Icons.Default.PlayArrow
        )
        RoomStatus.DRAF -> Quadruple(
            "Sedang Diperiksa",
            MaterialTheme.colorScheme.tertiary,
            "Lanjutkan",
            Icons.Default.PlayArrow
        )
        RoomStatus.MENUNGGU_KIRIM -> Quadruple(
            "Sedang Diperiksa",
            MaterialTheme.colorScheme.tertiary,
            "Lanjutkan",
            Icons.Default.PlayArrow
        )
        RoomStatus.MENUNGGU_REVIEW -> Quadruple(
            "Menunggu Review",
            MaterialTheme.colorScheme.tertiary,
            "Lihat Hasil",
            Icons.Default.Visibility
        )
        RoomStatus.DISETUJUI -> Quadruple(
            "Selesai",
            MaterialTheme.colorScheme.secondary,
            "Lihat Hasil",
            Icons.Default.Visibility
        )
        RoomStatus.DITOLAK -> Quadruple(
            "Ditolak",
            MaterialTheme.colorScheme.error,
            "Lihat Hasil",
            Icons.Default.Visibility
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (item.status) {
                    RoomStatus.BELUM -> onOpenForm(item.roomId, item.roomName)
                    RoomStatus.DRAF, RoomStatus.MENUNGGU_KIRIM -> item.draftId?.let { onResumeDraft(it, item.roomName) }
                    else -> item.inspectionId?.let(onInspectionClick)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon ruangan dalam circle hijau
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MeetingRoom,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Nama ruangan + item count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.roomName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.itemCount} Item",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Progress bar + count
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(80.dp)
            ) {
                LinearProgressIndicator(
                    progress = { if (item.itemCount > 0) item.scoredItems.toFloat() / item.itemCount else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(3.dp)
                        ),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.scoredItems} / ${item.itemCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Status badge + action button
            Column(horizontalAlignment = Alignment.End) {
                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Action button
                TextButton(
                    onClick = {
                        when (item.status) {
                            RoomStatus.BELUM -> onOpenForm(item.roomId, item.roomName)
                            RoomStatus.DRAF, RoomStatus.MENUNGGU_KIRIM -> item.draftId?.let { onResumeDraft(it, item.roomName) }
                            else -> item.inspectionId?.let(onInspectionClick)
                        }
                    }
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Header gradient hijau medis sesuai PRD: greeting, judul, nama inspector,
 * status sinkron (WIB), tombol keluar.
 * Gradient: #16A34A → #22C55E (Medical Green).
 */
@Composable
private fun DashboardHeader(
    currentUser: UserOut?,
    isSyncing: Boolean,
    lastSyncAt: String?,
    syncError: String?,
    onRetry: () -> Unit,
    onLogout: () -> Unit
) {
    // Format waktu sinkron ke WIB (Asia/Jakarta)
    fun formatToWib(isoTimestamp: String?): String {
        if (isoTimestamp == null) return "--:-- WIB"
        return try {
            // Server mengirim fraksi detik (mis. "...02.040377Z") sedangkan pola di bawah
            // tidak mengenali milidetik — strip fraksi dulu agar selalu bisa di-parse.
            val normalized = isoTimestamp.replace(Regex("\\.\\d+Z$"), "Z")
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(normalized) ?: return "--:-- WIB"
            val outputFormat = SimpleDateFormat("HH:mm", Locale.US)
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            "${outputFormat.format(date)} WIB"
        } catch (_: Exception) {
            "--:-- WIB"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF16A34A),
                        Color(0xFF22C55E)
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Ikon dekoratif transparan (shield/medical cross) di sisi kanan
        Icon(
            imageVector = Icons.Default.MedicalServices,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .alpha(0.15f),
            tint = Color.White
        )

        Column {
            // Greeting
            Text(
                text = "Selamat Datang 👋",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )

            // Judul
            Text(
                text = "Dashboard PPI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 28.sp
            )

            // Nama inspector
            Text(
                text = currentUser?.name?.ifBlank { null }
                    ?: currentUser?.username
                    ?: "Inspector",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status sinkron + tombol keluar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status sinkron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .clickable(enabled = syncError != null, onClick = onRetry)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isSyncing -> Icons.Default.SyncProblem
                            syncError != null -> Icons.Default.SyncProblem
                            else -> Icons.Default.CloudDone
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when {
                                isSyncing -> "Menyinkronkan..."
                                syncError != null -> "Sync gagal"
                                else -> "Sinkron terakhir"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        if (!isSyncing && syncError == null) {
                            Text(
                                text = formatToWib(lastSyncAt),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Tombol keluar
                OutlinedButton(
                    onClick = onLogout
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Keluar", color = Color.White)
                }
            }
        }
    }
}
