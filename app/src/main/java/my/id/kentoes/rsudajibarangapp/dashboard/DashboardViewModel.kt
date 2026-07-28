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
import my.id.kentoes.rsudajibarangapp.dashboard.api.IssueFrequencyOut
import my.id.kentoes.rsudajibarangapp.dashboard.api.RoomScoreOut
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
    val topIssues: List<IssueFrequencyOut> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val drafDao: DrafDao,
    private val masterDataDao: MasterDataDao,
    private val analyticsApi: AnalyticsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Collect local DB stats
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
                // Preserve analytics data when local DB emits
                _uiState.value = state.copy(
                    lowestRooms = _uiState.value.lowestRooms,
                    topIssues = _uiState.value.topIssues
                )
            }
        }

        // Fetch analytics dari BE
        fetchAnalytics()
    }

    /** Fetch analytics dari BE */
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
                Log.w("DashboardVM", "Gagal fetch analytics: ${e.message}")
            }
        }
    }
}
