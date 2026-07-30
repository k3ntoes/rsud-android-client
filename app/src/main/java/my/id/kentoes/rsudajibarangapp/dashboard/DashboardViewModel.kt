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
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.dashboard.api.AnalyticsApi
import my.id.kentoes.rsudajibarangapp.dashboard.api.DashboardDto
import my.id.kentoes.rsudajibarangapp.dashboard.api.IssueFrequencyOut
import my.id.kentoes.rsudajibarangapp.dashboard.api.RoomScoreOut
import my.id.kentoes.rsudajibarangapp.master.MasterDataRepository
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
    val serverPendingCount: Int = 0,
    val serverMonthlyCount: Int = 0,
    val serverAvgScorePct: Double = 0.0,
    val isForbidden: Boolean = false,
    val inspectedRoomCount: Int = 0,
    val uninspectedRoomCount: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val drafDao: DrafDao,
    private val masterDataDao: MasterDataDao,
    private val analyticsApi: AnalyticsApi,
    private val masterDataRepository: MasterDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                drafDao.getAllDrafts(),
                masterDataDao.getAllRooms().map { it.size },
                masterDataDao.getAllItems().map { it.size }
            ) { drafts, roomCount, itemCount ->
                DashboardUiState(
                    isLoading = false,
                    totalDrafts = drafts.size,
                    draftCount = drafts.count { it.status == "DRAFT" },
                    pendingSyncCount = drafts.count { it.status == "PENDING_SYNC" },
                    syncedCount = drafts.count { it.status == "SYNCED" },
                    totalRooms = roomCount,
                    totalItems = itemCount,
                    recentDrafts = drafts.take(5)
                )
            }.collect { state ->
                _uiState.value = state.copy(
                    lowestRooms = _uiState.value.lowestRooms,
                    topIssues = _uiState.value.topIssues,
                    serverPendingCount = _uiState.value.serverPendingCount,
                    serverMonthlyCount = _uiState.value.serverMonthlyCount,
                    serverAvgScorePct = _uiState.value.serverAvgScorePct,
                    isForbidden = _uiState.value.isForbidden
                )
            }
        }

        computeInspectionStatus()
        fetchDashboard()
        fetchAnalytics()
    }

    private fun computeInspectionStatus() {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val allRooms = masterDataDao.getAllRoomsOnce()
            val allInspectedIds = masterDataRepository.getInspectedRoomIdsForDate(date)
            _uiState.value = _uiState.value.copy(
                inspectedRoomCount = allInspectedIds.size,
                uninspectedRoomCount = (allRooms.size - allInspectedIds.size).coerceAtLeast(0)
            )
        }
    }

    private fun fetchDashboard() {
        viewModelScope.launch {
            val yearMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
            try {
                val dash: DashboardDto = analyticsApi.getDashboard(yearMonth)
                _uiState.value = _uiState.value.copy(
                    serverPendingCount = dash.pendingCount,
                    serverMonthlyCount = dash.monthlyInspectionCount,
                    serverAvgScorePct = dash.avgScorePct,
                    isForbidden = false
                )
            } catch (e: Exception) {
                if (e.message?.contains("403") == true) {
                    _uiState.value = _uiState.value.copy(isForbidden = true)
                }
                Log.w("DashboardVM", "Gagal fetch dashboard: ${e.message}")
            }
        }
    }

    private fun fetchAnalytics() {
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
                if (e.message?.contains("403") == true) {
                    _uiState.value = _uiState.value.copy(isForbidden = true)
                }
                Log.w("DashboardVM", "Gagal fetch analytics: ${e.message}")
            }
        }
    }
}
