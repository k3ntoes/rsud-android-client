package my.id.kentoes.rsudajibarangapp.inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.core.network.NetworkConnectivityObserver
import javax.inject.Inject

data class DaftarDrafUiState(
    val isLoading: Boolean = true,
    val drafts: List<DraftSummary> = emptyList(),
    val deletingId: Long? = null,
    val deletedMessage: String? = null,
    val draftToConfirmDelete: DraftSummary? = null
)

@HiltViewModel
class DaftarDrafViewModel @Inject constructor(
    private val repository: InspectionRepository,
    private val networkObserver: NetworkConnectivityObserver
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

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(deletedMessage = null)
    }
}
