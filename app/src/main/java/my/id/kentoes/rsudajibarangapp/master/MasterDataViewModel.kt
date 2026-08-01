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
    val groupedItems: Map<String, List<MasterDataItem>> = emptyMap(),
    val excludeRoomIds: Set<Long> = emptySet(),
    /** Jumlah item per room dihitung dari pivot room_items — bukan heuristik nama. */
    val roomItemCounts: Map<Long, Int> = emptyMap()
)

@HiltViewModel
class MasterDataViewModel @Inject constructor(
    private val repository: MasterDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MasterDataUiState())
    val uiState: StateFlow<MasterDataUiState> = _uiState.asStateFlow()

    // Reactive flow so combine re-emits when filter changes
    private val _excludeRoomIds = MutableStateFlow<Set<Long>>(emptySet())
    // Jumlah item per room dari pivot — dimuat sekali (init) & setelah sync
    private val _roomItemCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())

    init {
        // Observasi perubahan dari Room — gabung items + rooms via combine
        viewModelScope.launch {
            combine(
                repository.items,
                repository.rooms,
                _excludeRoomIds,
                _roomItemCounts
            ) { items, allRooms, excludeIds, roomItemCounts ->
                // Android = klien inspector-only (ADR-0017): HANYA room yang di-assign (isMyRoom).
                val baseRooms = allRooms.filter { it.isMyRoom }
                val filteredRooms = if (excludeIds.isEmpty()) baseRooms
                    else baseRooms.filter { it.id !in excludeIds }
                _uiState.value.copy(
                    items = items,
                    groupedItems = items.groupBy { it.kategori },
                    rooms = filteredRooms,
                    excludeRoomIds = excludeIds,
                    roomItemCounts = roomItemCounts,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        // Load item counts dari pivot — perlu coroutine (suspend)
        viewModelScope.launch { loadRoomItemCounts() }

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
        // Per-langkah (H1, partial-sync): data yang sukses tetap dipakai, pesan jujur.
        val steps: List<Pair<String, suspend () -> Unit>> = listOf(
            "Items" to { repository.syncItems() },
            "Ruangan" to { repository.syncRooms() },
            "Pivot Room-Item" to { repository.syncRoomItems() },
            "Ruangan Saya" to { repository.syncMyRooms() }
        )
        val succeeded = mutableListOf<String>()
        val failed = mutableListOf<String>()
        var firstError: String? = null
        steps.forEach { (name, step) ->
            runCatching { step() }
                .onSuccess { succeeded += name }
                .onFailure {
                    failed += name
                    if (firstError == null) firstError = it.message
                }
        }
        loadRoomItemCounts()
        _uiState.value = _uiState.value.copy(
            isSyncing = false,
            syncMessage = when {
                failed.isEmpty() -> "Data berhasil diperbarui"
                succeeded.isEmpty() -> firstError ?: "Gagal sinkronisasi"
                else -> "Sebagian data diperbarui (${succeeded.size}/${steps.size} berhasil) — ketuk retry"
            }
        )
    }

    /** Muat jumlah item per room dari pivot room_items (setelah sync, mapping sudah segar). */
    private suspend fun loadRoomItemCounts() {
        val counts = repository.getRoomItemMap().mapValues { it.value.size }
        _roomItemCounts.value = counts
    }

    fun clearSyncMessage() {
        _uiState.value = _uiState.value.copy(syncMessage = null)
    }
}
