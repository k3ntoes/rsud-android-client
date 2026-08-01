package my.id.kentoes.rsudajibarangapp.inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.auth.AuthRepository
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionOutDto
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import java.io.File
import javax.inject.Inject

data class InspectionHistoryUiState(
    val isInitialLoading: Boolean = true,
    val inspections: List<InspectionHistoryItem> = emptyList(),
    val selectedDetail: InspectionOutDto? = null,
    val isLoadingDetail: Boolean = false,
    val error: String? = null,
    val filterStatus: String? = null,
    val filterDate: String? = null,
    val currentPage: Int = 1,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = true,
    val isRefreshing: Boolean = false,
    val inspectorName: String = "Petugas",
    val detailRoomName: String = "",
    /** Peta server photo id → path file backup lokal di photos_sent (ADR-0016) — tampilan lokal-first. */
    val detailPhotoLocalPaths: Map<Long, String> = emptyMap(),
    val isReuploading: Boolean = false
)

@HiltViewModel
class InspectionHistoryViewModel @Inject constructor(
    private val repository: InspectionHistoryRepository,
    private val masterDataDao: MasterDataDao,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InspectionHistoryUiState())
    val uiState: StateFlow<InspectionHistoryUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var cacheJob: Job? = null
    private var loadMoreJob: Job? = null

    // Naik setiap daftar diganti (refresh selesai / filter berubah). loadNextPage
    // menangkap nilai ini saat mulai dan membuang hasil fetch jika sudah berubah —
    // menutup race dua arah antara refresh dan load-more.
    private var loadEpoch = 0

    /** Collect cache dari Room — cancel job sebelumnya jika ada (cegah conflict) */
    private fun collectCache(status: String? = null, date: String? = null) {
        cacheJob?.cancel()
        cacheJob = viewModelScope.launch {
            repository.observeLocalInspections(status, date).collect { cached ->
                _uiState.value = _uiState.value.copy(
                    inspections = cached,
                    isInitialLoading = false
                )
            }
        }
    }

    fun setFilterDate(date: String?) {
        loadMoreJob?.cancel() // load halaman lama untuk filter lain tidak boleh menimpa state baru
        loadEpoch++
        _uiState.value = _uiState.value.copy(filterDate = date, currentPage = 1, hasMorePages = true)
        collectCache(status = _uiState.value.filterStatus, date = date)
        if (date != null) refreshFromServer()
    }

    init {
        collectCache()
        refreshFromServer()
    }

    /** Refresh dari server — update cache, Flow akan otomatis emit ulang */
    fun refreshFromServer() {
        refreshJob?.cancel()
        loadMoreJob?.cancel() // hasil loadMore yang basi tidak boleh menimpa state refresh
        refreshJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            try {
                val result = repository.fetchInspections(
                    page = 1,
                    status = _uiState.value.filterStatus
                )
                // Invalidasi loadMore yang mungkin mulai saat refresh berjalan
                loadEpoch++
                _uiState.value = _uiState.value.copy(
                    currentPage = result.currentPage,
                    isRefreshing = false,
                    hasMorePages = result.currentPage < result.totalPages
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Gagal refresh dari server"
                )
            }
        }
    }

    /** Load halaman berikutnya (infinite scroll) — pagination server-driven via totalPages */
    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMorePages) return

        val epoch = loadEpoch
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            _uiState.value = state.copy(isLoadingMore = true, error = null)
            try {
                val nextPage = state.currentPage + 1
                val result = repository.fetchInspections(
                    page = nextPage,
                    status = state.filterStatus
                )
                if (epoch != loadEpoch) {
                    // State sudah diganti oleh refresh/filter saat fetch berjalan — buang hasil basi.
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    currentPage = result.currentPage,
                    isLoadingMore = false,
                    hasMorePages = result.currentPage < result.totalPages
                )
            } catch (e: CancellationException) {
                // Load dibatalkan karena refresh/filter — bersihkan flag, jangan timpa state baru.
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
                throw e
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
                // Lookup room name dari cache lokal; inspector name dari user LOGIN
                // (ADR-0017: Android inspector-only — GET /api/auth/users admin-only → 403,
                // jadi cache UserEntity dihapus; hanya nama user sendiri yang tersedia).
                val roomName = detail?.roomId?.let { masterDataDao.getRoomById(it)?.nama } ?: ""
                val currentUser = authRepository.currentUser.value
                val inspectorText = detail?.inspectorId?.let { inspectorId ->
                    if (currentUser != null && currentUser.id == inspectorId) {
                        listOfNotNull(currentUser.name?.ifBlank { null }, currentUser.username)
                            .joinToString(" · ")
                    } else {
                        "Petugas #$inspectorId"
                    }
                } ?: "Petugas"
                // ADR-0016: lokal-first — hanya path backup yang file-nya masih ada di disk
                val localPaths = detail?.let { dto ->
                    masterDataDao.getPhotosForInspection(dto.id)
                        .mapNotNull { photo ->
                            photo.localPath?.takeIf { File(it).exists() }?.let { photo.id to it }
                        }
                        .toMap()
                } ?: emptyMap()
                _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false,
                    selectedDetail = detail,
                    inspectorName = inspectorText,
                    detailRoomName = roomName,
                    detailPhotoLocalPaths = localPaths
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
        val date = _uiState.value.filterDate
        loadMoreJob?.cancel() // load halaman lama untuk filter lain tidak boleh menimpa state baru
        loadEpoch++
        _uiState.value = _uiState.value.copy(filterStatus = status, currentPage = 1, hasMorePages = true)
        collectCache(status, date) // cancel previous + collect filtered
        refreshFromServer()
    }

    fun clearDetail() {
        _uiState.value = _uiState.value.copy(selectedDetail = null, detailPhotoLocalPaths = emptyMap())
    }

    /**
     * Re-upload foto dari backup lokal (photos_sent) ke server (ADR-0016).
     * Menunggu endpoint replace backend (kontrak §4.6) — jika gagal, error ditampilkan.
     */
    fun reuploadPhoto(inspectionId: Long, photoId: Long) {
        val localPath = _uiState.value.detailPhotoLocalPaths[photoId]
            ?: run {
                _uiState.value = _uiState.value.copy(error = "Backup foto lokal tidak ditemukan")
                return
            }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReuploading = true, error = null)
            try {
                repository.replacePhoto(inspectionId, photoId, localPath)
                _uiState.value = _uiState.value.copy(isReuploading = false)
                loadDetail(inspectionId) // refresh nama file server baru
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isReuploading = false,
                    error = e.message ?: "Gagal re-upload foto"
                )
            }
        }
    }
}
