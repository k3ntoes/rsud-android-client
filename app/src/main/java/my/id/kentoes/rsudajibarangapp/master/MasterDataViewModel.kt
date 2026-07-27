package my.id.kentoes.rsudajibarangapp.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import javax.inject.Inject

data class MasterDataUiState(
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val items: List<MasterDataItem> = emptyList(),
    val rooms: List<RuangEntity> = emptyList(),
    val syncMessage: String? = null,
    val groupedItems: Map<String, List<MasterDataItem>> = emptyMap()
)

@HiltViewModel
class MasterDataViewModel @Inject constructor(
    private val repository: MasterDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MasterDataUiState())
    val uiState: StateFlow<MasterDataUiState> = _uiState.asStateFlow()

    init {
        // Observasi perubahan dari Room
        viewModelScope.launch {
            repository.items.collect { items ->
                _uiState.value = _uiState.value.copy(
                    items = items,
                    groupedItems = items.groupBy { it.kategori },
                    isLoading = false
                )
            }
        }
        viewModelScope.launch {
            repository.rooms.collect { rooms ->
                _uiState.value = _uiState.value.copy(rooms = rooms, isLoading = false)
            }
        }

        // Init: sync dari API jika cache kosong
        viewModelScope.launch {
            val cached = repository.isCacheAvailable()
            if (!cached) {
                syncFromApi()
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /** Refresh data dari API (pull-to-refresh) */
    fun refresh() {
        viewModelScope.launch { syncFromApi() }
    }

    private suspend fun syncFromApi() {
        _uiState.value = _uiState.value.copy(isSyncing = true, syncMessage = null)
        val result = repository.syncFromApi()
        _uiState.value = _uiState.value.copy(
            isSyncing = false,
            syncMessage = when (result) {
                is MasterDataSyncState.SyncResult -> result.message
                else -> null
            }
        )
    }

    fun clearSyncMessage() {
        _uiState.value = _uiState.value.copy(syncMessage = null)
    }
}
