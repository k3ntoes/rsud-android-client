package my.id.kentoes.rsudajibarangapp.inspection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionDetailEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionPhotoEntity
import my.id.kentoes.rsudajibarangapp.sync.SentPhotoStorage
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.PhotoOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class InspectionHistoryItem(
    val id: Long,
    val roomId: Long,
    val roomName: String,
    val inspectorId: Int,
    val status: String,
    val businessDate: String?,
    val createdAt: String?,
    val detailCount: Int
)

data class InspectionDetailItem(
    val id: Long,
    val itemId: Long,
    val itemName: String,
    val score: Int,
    val photos: List<PhotoDetail>
)

data class PhotoDetail(
    val id: Long,
    val photoFileName: String,
    val thumbnailFileName: String?,
    val sortOrder: Int
)

data class PaginatedResult(
    val items: List<InspectionHistoryItem>,
    val totalPages: Int,
    val currentPage: Int
)

@Singleton
class InspectionHistoryRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val masterDataDao: MasterDataDao,
    private val sentPhotoStorage: SentPhotoStorage
) {

    /** Flow dari cache lokal — tampilkan instant sebelum refresh server */
    fun observeLocalInspections(status: String? = null, date: String? = null): Flow<List<InspectionHistoryItem>> {
        val flow = when {
            status != null && date != null -> masterDataDao.getInspectionsByStatusAndDate(status, date)
            date != null -> masterDataDao.getInspectionsByDate(date)
            status != null -> masterDataDao.getInspectionsByStatus(status)
            else -> masterDataDao.getAllInspections()
        }
        return combine(flow, masterDataDao.getAllRooms()) { inspections, rooms ->
            val roomMap = rooms.associateBy { it.id }
            inspections.map { entity ->
                InspectionHistoryItem(
                    id = entity.id,
                    roomId = entity.roomId,
                    roomName = roomMap[entity.roomId]?.nama ?: "Room #${entity.roomId}",
                    inspectorId = entity.inspectorId,
                    status = entity.status,
                    businessDate = entity.businessDate,
                    createdAt = entity.createdAt,
                    detailCount = 0 // tidak tersimpan di InspectionEntity
                )
            }
        }
    }

    /** Fetch dari server + update cache lokal + return hasil. Support pagination. */
    suspend fun fetchInspections(
        page: Int = 1,
        perPage: Int = 20,
        status: String? = null
    ): PaginatedResult {
        val response = syncApi.getInspections(page, perPage, status)
        val roomMap = masterDataDao.getAllRoomsOnce().associateBy { it.id }
        val items = response.items.map { item ->
            InspectionHistoryItem(
                id = item.id,
                roomId = item.roomId,
                roomName = roomMap[item.roomId]?.nama ?: "Room #${item.roomId}",
                inspectorId = item.inspectorId,
                status = item.status,
                businessDate = item.businessDate,
                createdAt = item.createdAt,
                detailCount = item.detailCount
            )
        }
        // Cache hasil fetch ke Room
        items.forEach { item ->
            val entity = InspectionEntity(
                id = item.id,
                roomId = item.roomId,
                inspectorId = item.inspectorId,
                status = item.status,
                businessDate = item.businessDate,
                createdAt = item.createdAt
            )
            masterDataDao.insertInspection(entity)
        }
        return PaginatedResult(items = items, totalPages = response.totalPages, currentPage = response.page)
    }

    /** Fetch detail dari API */
    suspend fun fetchDetail(id: Long): InspectionOutDto? {
        return try {
            syncApi.getInspectionDetail(id)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Simpan hasil submit ke cache lokal.
     *
     * @param photoLocalPaths peta server photo id → path file lokal di photos_sent (ADR-0016),
     *   diisi oleh SyncManager setelah memindahkan file terkompresi. Kolom `localPath` dipakai
     *   untuk tampilan lokal-first di detail riwayat.
     */
    suspend fun cacheInspection(dto: InspectionOutDto, photoLocalPaths: Map<Long, String> = emptyMap()) {
        val entity = InspectionEntity(
            id = dto.id,
            roomId = dto.roomId,
            inspectorId = dto.inspectorId,
            status = dto.status,
            businessDate = dto.businessDate,
            localTimestamp = dto.localTimestamp,
            rejectionReason = dto.rejectionReason,
            createdAt = dto.createdAt
        )
        masterDataDao.insertInspection(entity)

        val details = dto.details.map { detail ->
            InspectionDetailEntity(
                id = detail.id,
                inspectionId = dto.id,
                itemId = detail.itemId,
                itemNameSnapshot = detail.itemNameSnapshot,
                score = detail.score
            )
        }
        if (details.isNotEmpty()) masterDataDao.insertDetails(details)

        dto.details.forEach { detail ->
            val photos = detail.photos.map { photo ->
                InspectionPhotoEntity(
                    id = photo.id,
                    detailId = detail.id,
                    photoFileName = photo.photoFileName,
                    thumbnailFileName = photo.thumbnailFileName,
                    sortOrder = photo.sortOrder,
                    localPath = photoLocalPaths[photo.id]
                )
            }
            if (photos.isNotEmpty()) masterDataDao.insertPhotos(photos)
        }
    }

    /**
     * Re-upload foto dari backup lokal (photos_sent) ke server (ADR-0016).
     *
     * Endpoint replace (`PUT inspections/{id}/photos/{photoId}`) belum ada di backend
     * (kontrak §4.6) — method ini siap dipanggil begitu endpoint tersedia. File lokal
     * dipindahkan ke nama file server baru agar nama tetap = nama server (lookup trivial).
     */
    suspend fun replacePhoto(inspectionId: Long, photoId: Long, localPath: String): PhotoOutDto {
        val file = File(localPath)
        if (!file.exists()) throw IllegalStateException("File backup lokal tidak ditemukan")

        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipart = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val updated = syncApi.replacePhoto(inspectionId, photoId, multipart)

        // Pertahankan invariant nama file lokal = nama file server (ADR-0016)
        val newLocalPath = sentPhotoStorage
            .moveToSent(mapOf(updated.photoFileName to localPath))[updated.photoFileName]
            ?: localPath
        masterDataDao.updatePhotoAfterReplace(
            photoId = photoId,
            fileName = updated.photoFileName,
            thumbnailName = updated.thumbnailFileName,
            localPath = newLocalPath
        )
        return updated
    }

}
