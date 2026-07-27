package my.id.kentoes.rsudajibarangapp.inspection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafItem
import my.id.kentoes.rsudajibarangapp.sync.SyncWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class InspectionFormUiState(
    val roomId: Long = 0,
    val roomName: String = "",
    val isLoading: Boolean = false,
    val items: List<ItemState> = emptyList(),
    val groupedItems: Map<String, List<ItemState>> = emptyMap(),
    val totalItems: Int = 0,
    val scoredItems: Int = 0,
    val validItems: Int = 0,
    val draftSaved: Boolean = false,
    val submitEnabled: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InspectionFormViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val masterDataDao: MasterDataDao,
    private val drafDao: DrafDao,
    private val inspectionRepository: InspectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InspectionFormUiState())
    val uiState: StateFlow<InspectionFormUiState> = _uiState.asStateFlow()

    /** Map itemId → ItemState untuk update cepat */
    private val itemStates = mutableMapOf<Long, ItemState>()

    /** Inisialisasi dengan roomId dan roomName — dan opsional draftId untuk resume */
    fun init(roomId: Long, roomName: String, draftId: Long? = null) {
        _uiState.value = _uiState.value.copy(roomId = roomId, roomName = roomName, isLoading = true)

        viewModelScope.launch {
            val allMasterItems = masterDataDao.getAllItems().first()

            val states = if (draftId != null && draftId > 0) {
                // Resume dari draft — load skor, foto, catatan yang sudah tersimpan
                val (draftRoomId, draftStates) = inspectionRepository.draftToItemStates(draftId, allMasterItems)
                _uiState.value = _uiState.value.copy(roomId = draftRoomId)
                draftStates
            } else {
                // Form baru — semua item default (-1, kosong)
                allMasterItems.map { entity ->
                    ItemState(
                        itemId = entity.id,
                        nama = entity.nama,
                        kategori = entity.kategori,
                        skor = -1,
                        fotoPaths = emptyList(),
                        catatan = null
                    )
                }
            }

            states.forEach { itemStates[it.itemId] = it }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                items = states,
                groupedItems = states.groupBy { it.kategori }
            )
            updateCounts()
        }
    }

    /** Update skor untuk item tertentu */
    fun updateScore(itemId: Long, skor: Int) {
        val current = itemStates[itemId] ?: return
        itemStates[itemId] = current.copy(skor = skor)
        // Jika skor berubah dari 0 ke nilai lain, foto tetap (re-validasi)
        emitItems()
    }

    /** Ambil foto — path foto hasil kamera */
    fun addPhoto(itemId: Long, photoPath: String) {
        val current = itemStates[itemId] ?: return
        itemStates[itemId] = current.copy(
            fotoPaths = current.fotoPaths + photoPath
        )
        emitItems()
    }

    /** Hapus foto dari item */
    fun deletePhoto(itemId: Long, photoPath: String) {
        val current = itemStates[itemId] ?: return
        itemStates[itemId] = current.copy(
            fotoPaths = current.fotoPaths - photoPath
        )
        emitItems()
    }

    /** Update catatan item */
    fun updateCatatan(itemId: Long, catatan: String) {
        val current = itemStates[itemId] ?: return
        itemStates[itemId] = current.copy(
            catatan = catatan.ifBlank { null }
        )
        emitItems()
    }

    fun saveDraft() = save("DRAFT")

    /** Kirim — semua item harus valid */
    fun submit() {
        if (!_uiState.value.submitEnabled) return
        save("PENDING_SYNC", enqueueSync = true)
    }

    /** Simpan draf ke Room — status DRAFT atau PENDING_SYNC */
    private fun save(status: String, enqueueSync: Boolean = false) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                .format(Date())

            val draftId = drafDao.insertDraft(
                DrafInspeksi(
                    roomId = _uiState.value.roomId,
                    localTimestamp = now,
                    status = status
                )
            )

            itemStates.values.forEach { itemState ->
                val itemDbId = drafDao.insertItem(
                    DrafItem(
                        drafId = draftId,
                        itemId = itemState.itemId,
                        skor = itemState.skor,
                        catatan = itemState.catatan
                    )
                )
                itemState.fotoPaths.forEach { path ->
                    drafDao.insertPhoto(
                        DrafFoto(drafItemId = itemDbId, pathLokal = path)
                    )
                }
            }

            if (enqueueSync) SyncWorker.enqueue(context)
            _uiState.value = _uiState.value.copy(draftSaved = true)
        }
    }

    fun clearDraftSaved() {
        _uiState.value = _uiState.value.copy(draftSaved = false)
    }

    private fun emitItems() {
        val states = itemStates.values.toList()
        _uiState.value = _uiState.value.copy(
            items = states,
            groupedItems = states.groupBy { it.kategori }
        )
        updateCounts()
    }

    private fun updateCounts() {
        val states = itemStates.values
        val total = states.size
        val scored = states.count { it.isScored }
        val valid = states.count { it.isValid }
        _uiState.value = _uiState.value.copy(
            totalItems = total,
            scoredItems = scored,
            validItems = valid,
            submitEnabled = total > 0 && valid == total
        )
    }
}
