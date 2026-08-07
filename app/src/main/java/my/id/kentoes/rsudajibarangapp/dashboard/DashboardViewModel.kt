package my.id.kentoes.rsudajibarangapp.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.master.MasterDataRepository
import my.id.kentoes.rsudajibarangapp.master.SyncStateStore
import my.id.kentoes.rsudajibarangapp.master.latestSyncTime
import my.id.kentoes.rsudajibarangapp.sync.SyncManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** Status satu ruangan pada hari ini (businessDate = today) — daftar per-room di dashboard. */
enum class RoomStatus {
    BELUM, DRAF, MENUNGGU_KIRIM, MENUNGGU_REVIEW, DISETUJUI, DITOLAK
}

/** Satu baris daftar "Status Inspeksi Hari Ini": status + target navigasi (form/draf/detail). */
data class RoomStatusItem(
    val roomId: Long,
    val roomName: String,
    val itemCount: Int,
    val scoredItems: Int = 0,
    val status: RoomStatus,
    val draftId: Long? = null,
    val inspectionId: Long? = null
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val draftCount: Int = 0,
    val pendingSyncCount: Int = 0,
    val syncedCount: Int = 0,
    val recentDrafts: List<DrafInspeksi> = emptyList(),
    val inspectedRoomCount: Int = 0,
    val uninspectedRoomCount: Int = 0,
    val roomStatuses: List<RoomStatusItem> = emptyList(),
    val isSyncing: Boolean = false,
    val lastSyncAt: String? = null,
    val syncError: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val drafDao: DrafDao,
    private val masterDataDao: MasterDataDao,
    private val masterDataRepository: MasterDataRepository,
    private val syncManager: SyncManager,
    private val syncStateStore: SyncStateStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                drafDao.getAllDrafts(),
                masterDataDao.getAllInspections().map { it.size }
            ) { drafts, inspectionCount ->
                DashboardUiState(
                    isLoading = false,
                    draftCount = drafts.count { it.status == "DRAFT" },
                    pendingSyncCount = drafts.count { it.status == "PENDING_SYNC" },
                    // Terkirim dari InspectionEntity (cache riwayat server) — BUKAN draf
                    // SYNCED (draf dihapus dari DB setelah sync sukses, jadi count-nya selalu
                    // 0). Inspeksi terkirim = baris InspectionEntity di cache lokal.
                    syncedCount = inspectionCount,
                    recentDrafts = drafts.take(5)
                )
            }.collect { state ->
                _uiState.value = state.copy(
                    inspectedRoomCount = _uiState.value.inspectedRoomCount,
                    uninspectedRoomCount = _uiState.value.uninspectedRoomCount,
                    roomStatuses = _uiState.value.roomStatuses,
                    isSyncing = _uiState.value.isSyncing,
                    lastSyncAt = _uiState.value.lastSyncAt,
                    syncError = _uiState.value.syncError
                )
                // Status per-room wajib segar saat draf/inspeksi berubah (mis. user kembali
                // dari form setelah menyimpan draf) — bukan hanya saat init/refresh.
                // .first() = one-shot read, tidak memicu re-emisi → tidak ada loop.
                computeInspectionStatus()
            }
        }

        computeInspectionStatus()
        autoSyncIfCacheEmpty()
    }

    /**
     * Auto-sync saat cache master data kosong (mis. akun baru login, setelah forceLogout
     * akun lain) — dashboard tidak boleh tampil kosong tanpa master data. Guard isSyncing
     * mencegah loop: jika sync gagal dan cache tetap kosong, tidak langsung mengulang.
     */
    private fun autoSyncIfCacheEmpty() {
        viewModelScope.launch {
            if (!masterDataRepository.isCacheAvailable()) {
                refresh()
            } else {
                // Cache sudah terisi — tampilkan waktu sync terakhir dari SyncStateStore
                _uiState.value = _uiState.value.copy(
                    lastSyncAt = syncStateStore.load().latestSyncTime()
                )
            }
        }
    }

    /** Refresh master data (auto-sync saat cache kosong / pull-to-refresh / retry) */
    fun refresh() {
        if (_uiState.value.isSyncing) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncError = null)
            // Sync per-langkah: sebagian berhasil ≠ "gagal total" — pesan mencerminkan realita
            // (H1, partial-sync). Angka dashboard berubah dari DB yang ter-update sebagian,
            // jadi pesan "Sebagian data diperbarui" benar; "Sync gagal" hanya saat nihil.
            val result = syncManager.syncMasterData()
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                lastSyncAt = if (result.failed.isEmpty()) {
                    syncStateStore.load().latestSyncTime()
                } else {
                    _uiState.value.lastSyncAt
                },
                syncError = when {
                    result.failed.isEmpty() -> null
                    result.succeeded.isEmpty() -> "Sync gagal"
                    else -> "Sebagian data diperbarui (${result.succeeded.size}/${result.total} berhasil) — ketuk retry"
                }
            )
            // Sync mengisi cache room (fresh login / auto-sync). Hitung ulang status inspeksi
            // agar card "Belum/Sudah Diinspeksi" tidak macet di 0 sejak init (rooms belum ada).
            computeInspectionStatus()
        }
    }

    private fun computeInspectionStatus() {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val allRooms = masterDataDao.getAllRoomsOnce()
            // Android = klien inspector-only (ADR-0017): scope SELALU room yang di-assign.
            val scopeRooms = allRooms.filter { it.isMyRoom }
            // Count tetap via repository (definisi "sudah" = draf ATAU inspeksi hari ini).
            val inspectedIds = masterDataRepository.getInspectedRoomIdsForDate(date)
            val inspectedInScope = scopeRooms.count { it.id in inspectedIds }
            // Daftar per-room (UX-01): status + id untuk navigasi. Semua data lokal/ter-sync
            // dari BE — tanpa endpoint baru (keputusan Q5 grill 2026-08).
            val draftsToday = drafDao.getAllDrafts().first().filter { draft ->
                val draftDate = draft.businessDate ?: if (draft.localTimestamp.length >= 10) draft.localTimestamp.take(10) else ""
                draftDate == date
            }
            val inspectionsToday = masterDataDao.getInspectionsByDate(date).first()
            val itemCounts = masterDataDao.getAllRoomItems().groupingBy { it.roomId }.eachCount()
            // Hitung jumlah item yang sudah di-score per room (dari draf hari ini)
            val scoredCounts = mutableMapOf<Long, Int>()
            draftsToday.forEach { draft ->
                val items = drafDao.getItemsForDraft(draft.id)
                scoredCounts[draft.roomId] = items.count { it.skor >= 0 }
            }
            val statuses = scopeRooms.map { room ->
                // Precedence: inspection (kebenaran server) > draf (lokal) > BELUM.
                val inspection = inspectionsToday
                    .filter { it.roomId == room.id }
                    .maxByOrNull { it.createdAt ?: "" }
                val draft = draftsToday
                    .filter { it.roomId == room.id }
                    .maxByOrNull { it.createdAt }
                val (status, draftId, inspectionId) = when {
                    inspection != null -> when (inspection.status) {
                        "APPROVED" -> Triple(RoomStatus.DISETUJUI, null, inspection.id)
                        "REJECTED" -> Triple(RoomStatus.DITOLAK, null, inspection.id)
                        else -> Triple(RoomStatus.MENUNGGU_REVIEW, null, inspection.id)
                    }
                    draft != null -> when (draft.status) {
                        "PENDING_SYNC" -> Triple(RoomStatus.MENUNGGU_KIRIM, draft.id, null)
                        else -> Triple(RoomStatus.DRAF, draft.id, null)
                    }
                    else -> Triple(RoomStatus.BELUM, null, null)
                }
                // Jika inspeksi sudah disetujui, semua item dianggap scored
                val scoredItems = if (status == RoomStatus.DISETUJUI) {
                    itemCounts[room.id] ?: 0
                } else {
                    scoredCounts[room.id] ?: 0
                }
                RoomStatusItem(
                    roomId = room.id,
                    roomName = room.nama,
                    itemCount = itemCounts[room.id] ?: 0,
                    scoredItems = scoredItems,
                    status = status,
                    draftId = draftId,
                    inspectionId = inspectionId
                )
            }.sortedWith(
                compareBy(
                    // Urut: BELUM (ordinal 0) paling atas — jangan ubah urutan enum tanpa
                    // menyesuaikan prioritas tampilan di sini.
                    { it.status.ordinal },
                    { it.roomName }
                )
            )
            _uiState.value = _uiState.value.copy(
                inspectedRoomCount = inspectedInScope,
                uninspectedRoomCount = (scopeRooms.size - inspectedInScope).coerceAtLeast(0),
                roomStatuses = statuses
            )
        }
    }
}
