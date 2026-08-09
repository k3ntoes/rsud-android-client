package my.id.kentoes.rsudajibarangapp.inspection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.core.network.NetworkConnectivityObserver
import my.id.kentoes.rsudajibarangapp.sync.SyncManager
import my.id.kentoes.rsudajibarangapp.sync.SyncWorker
import javax.inject.Inject

data class DaftarDrafUiState(
    val isLoading: Boolean = true,
    val drafts: List<DraftSummary> = emptyList(),
    val deletingId: Long? = null,
    val deletedMessage: String? = null,
    val draftToConfirmDelete: DraftSummary? = null,
    val syncMessage: String? = null
)

@HiltViewModel
class DaftarDrafViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: InspectionRepository,
    private val networkObserver: NetworkConnectivityObserver,
    private val syncManager: SyncManager
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _uiState = MutableStateFlow(DaftarDrafUiState())
    val uiState: StateFlow<DaftarDrafUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allDraftsSummary.collect { drafts ->
                _uiState.value = _uiState.value.copy(
                    drafts = drafts,
                    isLoading = false
                )
            }
        }
        // Auto-retry submit (SYNC_REQUIRED / ROOM_NOT_ASSIGNED): beri tahu user lewat
        // snackbar bahwa jeda singkat terjadi karena data master sedang disinkronkan,
        // bukan karena error. Pesan tampil via uiState.syncMessage di DaftarDrafScreen.
        viewModelScope.launch {
            syncManager.retryEvents.collect { message ->
                _uiState.value = _uiState.value.copy(syncMessage = message)
            }
        }
    }

    /** Tampilkan dialog konfirmasi hapus */
    fun requestDelete(draft: DraftSummary) {
        _uiState.value = _uiState.value.copy(draftToConfirmDelete = draft)
    }

    /** Batal hapus */
    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(draftToConfirmDelete = null)
    }

    /** Konfirmasi dan hapus draf */
    fun confirmDelete() {
        val draft = _uiState.value.draftToConfirmDelete ?: return
        _uiState.value = _uiState.value.copy(draftToConfirmDelete = null, deletingId = draft.id)

        viewModelScope.launch {
            repository.deleteDraft(draft.id)
            _uiState.value = _uiState.value.copy(
                deletingId = null,
                deletedMessage = "Draf berhasil dihapus"
            )
        }
    }

    /** Trigger sync manual — enqueue WorkManager untuk semua draf PENDING_SYNC */
    fun triggerSync() {
        val pendingCount = _uiState.value.drafts.count { it.status == "PENDING_SYNC" }
        if (pendingCount == 0) {
            _uiState.value = _uiState.value.copy(syncMessage = "Tidak ada draf yang perlu dikirim")
            return
        }
        SyncWorker.enqueue(context)
        _uiState.value = _uiState.value.copy(
            syncMessage = "Sinkronisasi $pendingCount draf dijadwalkan"
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(deletedMessage = null, syncMessage = null)
    }
}
