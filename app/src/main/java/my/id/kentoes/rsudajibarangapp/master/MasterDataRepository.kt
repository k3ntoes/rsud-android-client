package my.id.kentoes.rsudajibarangapp.master

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.master.api.MasterDataApi
import javax.inject.Inject
import javax.inject.Singleton

/** Status sinkronisasi master data */
sealed class MasterDataSyncState {
    data object Idle : MasterDataSyncState()
    data object Syncing : MasterDataSyncState()
    data class SyncResult(val success: Boolean, val message: String? = null) : MasterDataSyncState()
}

@Singleton
class MasterDataRepository @Inject constructor(
    private val masterDataApi: MasterDataApi,
    private val masterDataDao: MasterDataDao
) {
    /** Observasi items dari cache lokal */
    val items: Flow<List<MasterDataItem>> = masterDataDao.getAllItems()

    /** Observasi rooms dari cache lokal */
    val rooms: Flow<List<RuangEntity>> = masterDataDao.getAllRooms()

    /** Cek apakah cache lokal sudah terisi */
    suspend fun isCacheAvailable(): Boolean {
        val itemsCount = masterDataDao.getAllItems().first().size
        return itemsCount > 0
    }

    /** Fetch items & rooms dari API, simpan ke Room */
    suspend fun syncFromApi(): MasterDataSyncState {
        return try {
            // Fetch items
            val itemsResponse = masterDataApi.getItems()
            if (itemsResponse.success && itemsResponse.data != null) {
                val items = itemsResponse.data.map { api ->
                    MasterDataItem(
                        id = api.id,
                        nama = api.nama,
                        kategori = api.kategori,
                        deskripsi = api.deskripsi,
                        isActive = api.isActive
                    )
                }
                masterDataDao.insertItems(items)
            }

            // Fetch rooms
            val roomsResponse = masterDataApi.getRooms()
            if (roomsResponse.success && roomsResponse.data != null) {
                val rooms = roomsResponse.data.map { api ->
                    RuangEntity(
                        id = api.id,
                        nama = api.nama,
                        lantai = api.lantai,
                        isActive = api.isActive
                    )
                }
                masterDataDao.insertRooms(rooms)
            }

            MasterDataSyncState.SyncResult(true, "Data berhasil diperbarui")
        } catch (e: Exception) {
            MasterDataSyncState.SyncResult(false, e.message ?: "Gagal sinkronisasi")
        }
    }
}
