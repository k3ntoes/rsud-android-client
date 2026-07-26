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
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalDrafts: Int = 0,
    val draftCount: Int = 0,
    val pendingSyncCount: Int = 0,
    val syncedCount: Int = 0,
    val totalRooms: Int = 0,
    val totalItems: Int = 0,
    val recentDrafts: List<DrafInspeksi> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val drafDao: DrafDao,
    private val masterDataDao: MasterDataDao
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
                _uiState.value = state
            }
        }
    }
}
