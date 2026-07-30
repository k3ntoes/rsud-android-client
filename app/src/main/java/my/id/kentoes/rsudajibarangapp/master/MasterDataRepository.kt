package my.id.kentoes.rsudajibarangapp.master

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import my.id.kentoes.rsudajibarangapp.auth.api.AuthApi
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RoomItemEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserRoomEntity
import my.id.kentoes.rsudajibarangapp.master.api.ItemOut
import my.id.kentoes.rsudajibarangapp.master.api.MasterDataApi
import my.id.kentoes.rsudajibarangapp.master.api.RoomItemDto
import my.id.kentoes.rsudajibarangapp.master.api.RoomOut
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterDataRepository @Inject constructor(
    private val masterDataApi: MasterDataApi,
    private val authApi: AuthApi,
    private val masterDataDao: MasterDataDao
) {
    /** Observasi items dari cache lokal */
    val items: Flow<List<MasterDataItem>> = masterDataDao.getAllItems()

    /** Observasi rooms dari cache lokal */
    val rooms: Flow<List<RuangEntity>> = masterDataDao.getAllRooms()

    /** Cek apakah item cache lokal sudah terisi */
    suspend fun isCacheAvailable(): Boolean {
        val itemsCount = masterDataDao.getAllItems().first().size
        return itemsCount > 0
    }

    /** Build mapping roomId → list of itemIds dari cache lokal */
    suspend fun getRoomItemMap(): Map<Long, List<Long>> {
        val items = masterDataDao.getAllRoomItems()
        return items.groupBy({ it.roomId }, { it.itemId })
    }

    /** Fetch items & rooms dari API, simpan ke Room. */
    suspend fun syncFromApi(): String {
        syncItems()
        syncRooms()
        return "Data berhasil diperbarui"
    }

    suspend fun syncItems() {
        val response = masterDataApi.getItems()
        val apiItems = response.items
        if (apiItems.isNotEmpty()) {
            val items = apiItems.map { api ->
                MasterDataItem(
                    id = api.id,
                    nama = api.name,
                    kategori = "",
                    deskripsi = null,
                    isActive = api.isActive,
                    updatedAt = api.updatedAt
                )
            }
            masterDataDao.insertItems(items)
        }
    }

    suspend fun syncRooms() {
        val response = masterDataApi.getRooms()
        val apiRooms = response.items
        if (apiRooms.isNotEmpty()) {
            val rooms = apiRooms.map { api ->
                RuangEntity(
                    id = api.id,
                    nama = api.name,
                    lantai = null,
                    isActive = api.isActive,
                    updatedAt = api.updatedAt
                )
            }
            masterDataDao.insertRooms(rooms)
        }
    }

    suspend fun syncRoomItems() {
        val response = masterDataApi.getRoomItems()
        val apiItems = response.data
        masterDataDao.clearRoomItems()
        if (apiItems.isNotEmpty()) {
            masterDataDao.insertRoomItems(apiItems.map { dto ->
                RoomItemEntity(
                    id = dto.id,
                    roomId = dto.roomId,
                    itemId = dto.itemId,
                    createdAt = dto.createdAt
                )
            })
        }
    }

    suspend fun syncMyRooms() {
        val response = authApi.getMyRooms()
        // MyRooms are RoomOut — store them as RuangEntity
        val apiRooms = response.data
        if (apiRooms.isNotEmpty()) {
            val rooms = apiRooms.map { api ->
                RuangEntity(
                    id = api.id,
                    nama = api.name,
                    isActive = api.isActive,
                    updatedAt = api.updatedAt
                )
            }
            masterDataDao.insertRooms(rooms)
        }
    }

    suspend fun syncUsers() {
        val apiUsers = authApi.getUsers()
        masterDataDao.clearUsers()
        if (apiUsers.isNotEmpty()) {
            masterDataDao.insertUsers(apiUsers.map { user ->
                UserEntity(
                    id = user.id,
                    username = user.username,
                    role = user.role,
                    isActive = user.isActive
                )
            })
        }
    }

    suspend fun syncUserRooms() {
        val response = authApi.getUserRooms()
        val apiItems = response.data
        masterDataDao.clearUserRooms()
        if (apiItems.isNotEmpty()) {
            masterDataDao.insertUserRooms(apiItems.map { dto ->
                UserRoomEntity(
                    id = dto.id,
                    userId = dto.userId,
                    roomId = dto.roomId,
                    createdAt = dto.createdAt
                )
            })
        }
    }
}
