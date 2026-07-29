package my.id.kentoes.rsudajibarangapp.inspection

import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionDetailEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionPhotoEntity
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionDetailOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionListItemDto
import my.id.kentoes.rsudajibarangapp.sync.api.InspectionOutDto
import my.id.kentoes.rsudajibarangapp.sync.api.SyncApi
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

@Singleton
class InspectionHistoryRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val masterDataDao: MasterDataDao
) {

    /** Fetch list dari API + lookup room name lokal */
    suspend fun fetchInspections(
        page: Int = 1,
        perPage: Int = 20,
        status: String? = null,
        showAll: Boolean? = null
    ): List<InspectionHistoryItem> {
        val response = syncApi.getInspections(page, perPage, status, showAll)
        return response.map { item ->
            InspectionHistoryItem(
                id = item.id,
                roomId = item.roomId,
                roomName = masterDataDao.getRoomById(item.roomId)?.nama ?: "Room #${item.roomId}",
                inspectorId = item.inspectorId,
                status = item.status,
                businessDate = item.businessDate,
                createdAt = item.createdAt,
                detailCount = item.detailCount
            )
        }
    }

    /** Fetch detail dari API + simpan ke cache lokal */
    suspend fun fetchDetail(id: Long): InspectionOutDto? {
        return try {
            syncApi.getInspectionDetail(id)
        } catch (_: Exception) {
            null
        }
    }

    /** Simpan hasil submit ke cache lokal */
    suspend fun cacheInspection(dto: InspectionOutDto) {
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
                    sortOrder = photo.sortOrder
                )
            }
            if (photos.isNotEmpty()) masterDataDao.insertPhotos(photos)
        }
    }
}
