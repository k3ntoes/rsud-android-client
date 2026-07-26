package my.id.kentoes.rsudajibarangapp.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity

@Dao
interface MasterDataDao {

    /** Master Data Items */
    @Query("SELECT * FROM master_data_item WHERE isActive = 1 ORDER BY kategori, nama")
    fun getAllItems(): Flow<List<MasterDataItem>>

    @Query("SELECT * FROM master_data_item WHERE id = :id")
    suspend fun getItemById(id: Long): MasterDataItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MasterDataItem>)

    @Query("DELETE FROM master_data_item")
    suspend fun clearAllItems()

    /** Ruangan */
    @Query("SELECT * FROM ruang WHERE isActive = 1 ORDER BY nama")
    fun getAllRooms(): Flow<List<RuangEntity>>

    @Query("SELECT * FROM ruang WHERE id = :id")
    suspend fun getRoomById(id: Long): RuangEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RuangEntity>)

    @Query("DELETE FROM ruang")
    suspend fun clearAllRooms()
}
