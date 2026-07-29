package my.id.kentoes.rsudajibarangapp.inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionOutDto
import javax.inject.Inject

data class InspectionHistoryUiState(
    val isLoading: Boolean = false,
    val inspections: List<InspectionHistoryItem> = emptyList(),
    val selectedDetail: InspectionOutDto? = null,
    val isLoadingDetail: Boolean = false,
    val error: String? = null,
    val filterStatus: String? = null,
    val currentPage: Int = 1,
    val isLoadingMore: Boolean = false
)

@HiltViewModel
class InspectionHistoryViewModel @Inject constructor(
    private val repository: InspectionHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InspectionHistoryUiState())
    val uiState: StateFlow<InspectionHistoryUiState> = _uiState.asStateFlow()

    init { loadInspections() }

    fun loadInspections() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val items = repository.fetchInspections(
                    status = _uiState.value.filterStatus
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inspections = items
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal memuat riwayat"
                )
            }
        }
    }

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDetail = true)
            try {
                val detail = repository.fetchDetail(id)
                _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false,
                    selectedDetail = detail
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
        _uiState.value = _uiState.value.copy(filterStatus = status)
        loadInspections()
    }

    fun clearDetail() {
        _uiState.value = _uiState.value.copy(selectedDetail = null)
    }
}
