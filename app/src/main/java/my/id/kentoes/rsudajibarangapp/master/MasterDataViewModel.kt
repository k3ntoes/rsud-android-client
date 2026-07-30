package my.id.kentoes.rsudajibarangapp.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
        // Observasi perubahan dari Room — gabung items + rooms via combine
        viewModelScope.launch {
            combine(
                repository.items,
                repository.rooms
            ) { items, rooms ->
                _uiState.value.copy(
                    items = items,
                    groupedItems = items.groupBy { it.kategori },
                    rooms = rooms,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
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
        try {
            repository.syncItems()
            repository.syncMyRooms()
            _uiState.value = _uiState.value.copy(isSyncing = false, syncMessage = "Data berhasil diperbarui")
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                syncMessage = e.message ?: "Gagal sinkronisasi"
            )
        }
    }

    fun clearSyncMessage() {
        _uiState.value = _uiState.value.copy(syncMessage = null)
    }
}
