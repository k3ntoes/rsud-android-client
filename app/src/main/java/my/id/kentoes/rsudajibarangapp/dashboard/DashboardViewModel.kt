package my.id.kentoes.rsudajibarangapp.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.auth.AuthRepository
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.dashboard.api.AnalyticsApi
import my.id.kentoes.rsudajibarangapp.dashboard.api.IssueFrequencyOut
import my.id.kentoes.rsudajibarangapp.dashboard.api.RoomScoreOut
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
    val totalDrafts: Int = 0,
    val draftCount: Int = 0,
    val pendingSyncCount: Int = 0,
    val syncedCount: Int = 0,
    val totalRooms: Int = 0,
    val totalItems: Int = 0,
    val recentDrafts: List<DrafInspeksi> = emptyList(),
    val lowestRooms: List<RoomScoreOut> = emptyList(),
    val topIssues: List<IssueFrequencyOut> = emptyList(),
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
    private val analyticsApi: AnalyticsApi,
    private val masterDataRepository: MasterDataRepository,
    private val syncManager: SyncManager,
    private val syncStateStore: SyncStateStore,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                drafDao.getAllDrafts(),
                masterDataDao.getAllRooms().map { it.size },
                masterDataDao.getAllItems().map { it.size },
                masterDataDao.getAllInspections().map { it.size }
            ) { drafts, roomCount, itemCount, inspectionCount ->
                DashboardUiState(
                    isLoading = false,
                    totalDrafts = inspectionCount,
                    draftCount = drafts.count { it.status == "DRAFT" },
                    pendingSyncCount = drafts.count { it.status == "PENDING_SYNC" },
                    // Terkirim & Total dari InspectionEntity (cache riwayat server) — BUKAN
                    // draf SYNCED (draf dihapus dari DB setelah sync sukses, jadi count-nya
                    // selalu 0). Inspeksi terkirim = baris InspectionEntity di cache lokal.
                    syncedCount = inspectionCount,
                    totalRooms = roomCount,
                    totalItems = itemCount,
                    recentDrafts = drafts.take(5)
                )
            }.collect { state ->
                _uiState.value = state.copy(
                    lowestRooms = _uiState.value.lowestRooms,
                    topIssues = _uiState.value.topIssues,
                    inspectedRoomCount = _uiState.value.inspectedRoomCount,
                    uninspectedRoomCount = _uiState.value.uninspectedRoomCount,
                    isSyncing = _uiState.value.isSyncing,
                    lastSyncAt = _uiState.value.lastSyncAt,
                    syncError = _uiState.value.syncError
                )
            }
        }

        computeInspectionStatus()
        fetchAnalytics()
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
            try {
                syncManager.syncMasterData()
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    lastSyncAt = syncStateStore.load().latestSyncTime()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncError = e.message ?: "Sync gagal"
                )
            }
        }
    }

    private fun computeInspectionStatus() {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val allRooms = masterDataDao.getAllRoomsOnce()
            val role = authRepository.currentUser.value?.role
            // Inspector/supervisor: hanya room yang di-assign (isMyRoom). Admin_ppi: semua room.
            // Konsisten dengan filter daftar ruangan di MasterDataViewModel.
            val scopeRooms = if (role == ROLE_ADMIN) allRooms else allRooms.filter { it.isMyRoom }
            val inspectedIds = masterDataRepository.getInspectedRoomIdsForDate(date)
            val inspectedInScope = scopeRooms.count { it.id in inspectedIds }
            _uiState.value = _uiState.value.copy(
                inspectedRoomCount = inspectedInScope,
                uninspectedRoomCount = (scopeRooms.size - inspectedInScope).coerceAtLeast(0)
            )
        }
    }

    private fun fetchAnalytics() {
        // Analytics HANYA untuk supervisor/admin_ppi (keputusan review 2026-08) —
        // inspector tidak fetch dan tidak render (list tetap kosong di UI).
        // CATATAN: gate ini baca role SEKALI saat init — aman karena AuthRepository.init()
        // menyetel currentUser SEBELUM emit Authenticated. Jika urutan itu berubah,
        // analytics supervisor/admin tidak akan pernah termuat (perlu re-collect role).
        val role = authRepository.currentUser.value?.role
        if (role != ROLE_SUPERVISOR && role != ROLE_ADMIN) return
        viewModelScope.launch {
            val yearMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
            try {
                val lowestRooms = analyticsApi.getLowestRooms(yearMonth = yearMonth, limit = 3)
                val topIssues = analyticsApi.getTopIssues(yearMonth = yearMonth, limit = 10)
                _uiState.value = _uiState.value.copy(
                    lowestRooms = lowestRooms,
                    topIssues = topIssues
                )
            } catch (e: Exception) {
                Log.w("DashboardVM", "Gagal fetch analytics: ${e.message}")
            }
        }
    }

    companion object {
        /** Role admin melihat SEMUA room; role lain hanya room yang di-assign. */
        private const val ROLE_ADMIN = "admin_ppi"
        /** Role supervisor — bersama admin, satu-satunya yang berhak melihat analytics. */
        private const val ROLE_SUPERVISOR = "supervisor"
    }
}
