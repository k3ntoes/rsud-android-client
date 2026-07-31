package my.id.kentoes.rsudajibarangapp.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.auth.AuthRepository
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import javax.inject.Inject

data class MasterDataUiState(
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val items: List<MasterDataItem> = emptyList(),
    val rooms: List<RuangEntity> = emptyList(),
    val syncMessage: String? = null,
    val groupedItems: Map<String, List<MasterDataItem>> = emptyMap(),
    val excludeRoomIds: Set<Long> = emptySet()
)

@HiltViewModel
class MasterDataViewModel @Inject constructor(
    private val repository: MasterDataRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MasterDataUiState())
    val uiState: StateFlow<MasterDataUiState> = _uiState.asStateFlow()

    // Reactive flow so combine re-emits when filter changes
    private val _excludeRoomIds = MutableStateFlow<Set<Long>>(emptySet())

    init {
        // Observasi perubahan dari Room — gabung items + rooms via combine
        viewModelScope.launch {
            combine(
                repository.items,
                repository.rooms,
                _excludeRoomIds,
                authRepository.currentUser
            ) { items, allRooms, excludeIds, user ->
                // Inspector/supervisor: hanya room yang di-assign (isMyRoom).
                // Admin (admin_ppi): semua room — /me/rooms kosong untuk admin.
                val baseRooms = if (user?.role == ROLE_ADMIN) allRooms
                    else allRooms.filter { it.isMyRoom }
                val filteredRooms = if (excludeIds.isEmpty()) baseRooms
                    else baseRooms.filter { it.id !in excludeIds }
                _uiState.value.copy(
                    items = items,
                    groupedItems = items.groupBy { it.kategori },
                    rooms = filteredRooms,
                    excludeRoomIds = excludeIds,
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

    /** Set filter to show only uninspected rooms for given date */
    fun setUninspectedFilter(date: String) {
        viewModelScope.launch {
            val ids = repository.getInspectedRoomIdsForDate(date)
            _excludeRoomIds.value = ids
        }
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

    companion object {
        /** Role admin melihat SEMUA room; role lain hanya room yang di-assign. */
        private const val ROLE_ADMIN = "admin_ppi"
    }
}
