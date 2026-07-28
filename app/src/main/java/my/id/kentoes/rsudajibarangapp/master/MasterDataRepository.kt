package my.id.kentoes.rsudajibarangapp.master

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.master.api.ItemOut
import my.id.kentoes.rsudajibarangapp.master.api.MasterDataApi
import my.id.kentoes.rsudajibarangapp.master.api.RoomOut
import javax.inject.Inject
import javax.inject.Singleton

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

    /** Fetch items & rooms dari API, simpan ke Room. Return success message or throw. */
    suspend fun syncFromApi(): String {
        // Fetch items — BE returns list langsung tanpa wrapper
        val apiItems: List<ItemOut> = masterDataApi.getItems()
        if (apiItems.isNotEmpty()) {
            val items = apiItems.map { api ->
                MasterDataItem(
                    id = api.id,
                    nama = api.name,
                    kategori = "",
                    deskripsi = null,
                    isActive = api.isActive
                )
            }
            masterDataDao.insertItems(items)
        }

        // Fetch rooms
        val apiRooms: List<RoomOut> = masterDataApi.getRooms()
        if (apiRooms.isNotEmpty()) {
            val rooms = apiRooms.map { api ->
                RuangEntity(
                    id = api.id,
                    nama = api.name,
                    lantai = null,
                    isActive = api.isActive
                )
            }
            masterDataDao.insertRooms(rooms)
        }

        return "Data berhasil diperbarui"
    }
}
