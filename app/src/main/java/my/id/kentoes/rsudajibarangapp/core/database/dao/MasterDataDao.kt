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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MasterDataItem>)

    /** Ruangan */
    @Query("SELECT * FROM ruang WHERE isActive = 1 ORDER BY nama")
    fun getAllRooms(): Flow<List<RuangEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RuangEntity>)
}
