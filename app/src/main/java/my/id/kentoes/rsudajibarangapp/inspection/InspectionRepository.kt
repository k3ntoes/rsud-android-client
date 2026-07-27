package my.id.kentoes.rsudajibarangapp.inspection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import my.id.kentoes.rsudajibarangapp.core.database.dao.DrafDao
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data ringkas draf untuk ditampilkan di daftar.
 */
data class DraftSummary(
    val id: Long,
    val roomId: Long,
    val roomName: String,
    val status: String,
    val localTimestamp: String,
    val createdAt: Long
)

/**
 * Draf lengkap dengan semua item dan foto.
 */
data class DraftWithItems(
    val draft: DrafInspeksi,
    val items: List<DrafItem>,
    val photos: Map<Long, List<DrafFoto>> // drafItemId → foto
)

/**
 * Payload JSON yang akan dikirim ke server (untuk EPIC-8).
 */
data class InspectionPayload(
    val roomId: Long,
    val localTimestamp: String,
    val businessDate: String,
    val items: List<PayloadItem>
)

data class PayloadItem(
    val itemId: Long,
    val skor: Int,
    val catatan: String?,
    val fotoPaths: List<String>
)

@Singleton
class InspectionRepository @Inject constructor(
    private val drafDao: DrafDao,
    private val masterDataDao: MasterDataDao
) {

    /** Observasi semua draf dengan nama ruangan. */
    val allDraftsSummary: Flow<List<DraftSummary>> =
        combine(
            drafDao.getAllDrafts(),
            masterDataDao.getAllRooms()
        ) { drafts, rooms ->
            val roomMap = rooms.associateBy { it.id }
            drafts.map { draft ->
                DraftSummary(
                    id = draft.id,
                    roomId = draft.roomId,
                    roomName = roomMap[draft.roomId]?.nama ?: "Ruangan #${draft.roomId}",
                    status = draft.status,
                    localTimestamp = draft.localTimestamp,
                    createdAt = draft.createdAt
                )
            }
        }

    /** Ambil satu draf lengkap (header + items + foto). */
    suspend fun getDraftWithItems(draftId: Long): DraftWithItems? {
        val draft = drafDao.getDraftById(draftId) ?: return null
        val items = drafDao.getItemsForDraft(draftId)
        val photos = items.associate { item ->
            item.id to drafDao.getPhotosForItem(item.id)
        }
        return DraftWithItems(draft = draft, items = items, photos = photos)
    }

    /** Hapus draf beserta item & foto (via CASCADE FK). */
    suspend fun deleteDraft(draftId: Long) {
        val draft = drafDao.getDraftById(draftId) ?: return
        drafDao.deleteDraftCascade(draft)
    }

    /** Update status draf (misal: DRAFT → PENDING_SYNC → SYNCED). */
    suspend fun updateStatus(draftId: Long, status: String) {
        drafDao.updateDraftStatus(draftId, status)
    }

    /** Siapkan payload JSON untuk dikirim ke server. */
    suspend fun preparePayload(draftId: Long): InspectionPayload? {
        val draftWithItems = getDraftWithItems(draftId) ?: return null
        val items = draftWithItems.items.map { item ->
            val fotos = draftWithItems.photos[item.id] ?: emptyList()
            PayloadItem(
                itemId = item.itemId,
                skor = item.skor,
                catatan = item.catatan,
                fotoPaths = fotos.map { it.pathLokal }
            )
        }
        val businessDate = draftWithItems.draft.localTimestamp.take(10)
        return InspectionPayload(
            roomId = draftWithItems.draft.roomId,
            localTimestamp = draftWithItems.draft.localTimestamp,
            businessDate = businessDate,
            items = items
        )
    }

    /** Konversi DraftWithItems ke ItemState list (untuk resume draft).
     *  Return Pair<roomId, items>. */
    suspend fun draftToItemStates(draftId: Long, masterItems: List<MasterDataItem>): Pair<Long, List<ItemState>> {
        val draft = drafDao.getDraftById(draftId) ?: return 0L to emptyList()
        val draftItems = drafDao.getItemsForDraft(draftId)
        val itemMap = masterItems.associateBy { it.id }

        val states = draftItems.map { item ->
            val fotos = drafDao.getPhotosForItem(item.id)
            val master = itemMap[item.itemId]
            ItemState(
                itemId = item.itemId,
                nama = master?.nama ?: "Item #${item.itemId}",
                kategori = master?.kategori ?: "",
                skor = item.skor,
                fotoPaths = fotos.map { it.pathLokal },
                catatan = item.catatan
            )
        }
        return draft.roomId to states
    }
}
