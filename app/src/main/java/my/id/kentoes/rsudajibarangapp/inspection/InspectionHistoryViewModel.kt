package my.id.kentoes.rsudajibarangapp.inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionOutDto
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import javax.inject.Inject

data class InspectionHistoryUiState(
    val isInitialLoading: Boolean = true,
    val inspections: List<InspectionHistoryItem> = emptyList(),
    val selectedDetail: InspectionOutDto? = null,
    val isLoadingDetail: Boolean = false,
    val error: String? = null,
    val filterStatus: String? = null,
    val currentPage: Int = 1,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val isRefreshing: Boolean = false,
    val inspectorName: String = "Petugas",
    val detailRoomName: String = ""
)

@HiltViewModel
class InspectionHistoryViewModel @Inject constructor(
    private val repository: InspectionHistoryRepository,
    private val masterDataDao: MasterDataDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(InspectionHistoryUiState())
    val uiState: StateFlow<InspectionHistoryUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var cacheJob: Job? = null

    /** Collect cache dari Room — cancel job sebelumnya jika ada (cegah conflict) */
    private fun collectCache(status: String? = null) {
        cacheJob?.cancel()
        cacheJob = viewModelScope.launch {
            repository.observeLocalInspections(status).collect { cached ->
                _uiState.value = _uiState.value.copy(
                    inspections = cached,
                    isInitialLoading = false
                )
            }
        }
    }

    init {
        collectCache()
        refreshFromServer()
    }

    /** Refresh dari server — update cache, Flow akan otomatis emit ulang */
    fun refreshFromServer() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            try {
                repository.fetchInspections(
                    page = 1,
                    status = _uiState.value.filterStatus
                )
                _uiState.value = _uiState.value.copy(
                    currentPage = 1,
                    isRefreshing = false,
                    hasMorePages = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Gagal refresh dari server"
                )
            }
        }
    }

    /** Load halaman berikutnya (infinite scroll) — pagination via list slicing */
    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingMore = true)
            try {
                val nextPage = state.currentPage + 1
                repository.fetchInspections(
                    page = nextPage,
                    status = state.filterStatus
                )
                _uiState.value = _uiState.value.copy(
                    currentPage = nextPage,
                    isLoadingMore = false
                )
                // ponytail: server returns flat list; pagination by page number,
                // stop at 10 pages as safety ceiling
                if (nextPage >= 10) {
                    _uiState.value = _uiState.value.copy(hasMorePages = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = e.message
                )
            }
        }
    }

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDetail = true)
            try {
                val detail = repository.fetchDetail(id)
                // Lookup room name + inspector name dari cache lokal
                val roomName = detail?.roomId?.let { masterDataDao.getRoomById(it)?.nama } ?: ""
                val inspectorText = detail?.inspectorId?.let {
                    masterDataDao.getUserById(it)?.let { u -> "${u.username} (${u.role})" }
                        ?: "Petugas #$it"
                } ?: "Petugas"
                _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false,
                    selectedDetail = detail,
                    inspectorName = inspectorText,
                    detailRoomName = roomName
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false,
                    error = e.message ?: "Gagal memuat detail"
                )
            }
        }
    }

    fun setFilter(status: String?) {
        _uiState.value = _uiState.value.copy(filterStatus = status, currentPage = 1, hasMorePages = true)
        collectCache(status) // cancel previous + collect filtered
        refreshFromServer()
    }

    fun clearDetail() {
        _uiState.value = _uiState.value.copy(selectedDetail = null)
    }
}
