package my.id.kentoes.rsudajibarangapp.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

data class DashboardUiState(
    val isLoading: Boolean = true,
    val draftCount: Int = 0,
    val pendingSyncCount: Int = 0,
    val syncedCount: Int = 0,
    val recentDrafts: List<DrafInspeksi> = emptyList(),
    val inspectedRoomCount: Int = 0,
    val uninspectedRoomCount: Int = 0,
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
                    isSyncing = _uiState.value.isSyncing,
                    lastSyncAt = _uiState.value.lastSyncAt,
                    syncError = _uiState.value.syncError
                )
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
        }
    }

    private fun computeInspectionStatus() {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val allRooms = masterDataDao.getAllRoomsOnce()
            // Android = klien inspector-only (ADR-0017): scope SELALU room yang di-assign.
            val scopeRooms = allRooms.filter { it.isMyRoom }
            val inspectedIds = masterDataRepository.getInspectedRoomIdsForDate(date)
            val inspectedInScope = scopeRooms.count { it.id in inspectedIds }
            _uiState.value = _uiState.value.copy(
                inspectedRoomCount = inspectedInScope,
                uninspectedRoomCount = (scopeRooms.size - inspectedInScope).coerceAtLeast(0)
            )
        }
    }
}
