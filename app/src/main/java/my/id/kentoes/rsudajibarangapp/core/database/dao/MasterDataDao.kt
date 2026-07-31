package my.id.kentoes.rsudajibarangapp.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionDetailEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.InspectionPhotoEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.MasterDataItem
import my.id.kentoes.rsudajibarangapp.core.database.entity.RoomItemEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.RuangEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserEntity
import my.id.kentoes.rsudajibarangapp.core.database.entity.UserRoomEntity

@Dao
interface MasterDataDao {

    // ── Master Data Items ──

    @Query("SELECT * FROM master_data_item WHERE isActive = 1 ORDER BY kategori, nama")
    fun getAllItems(): Flow<List<MasterDataItem>>

    @Query("SELECT * FROM master_data_item WHERE isActive = 1")
    suspend fun getAllItemsOnce(): List<MasterDataItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MasterDataItem>)

    @Query("DELETE FROM master_data_item")
    suspend fun clearItems()

    // ── Ruangan ──

    @Query("SELECT * FROM ruang WHERE isActive = 1 ORDER BY nama")
    fun getAllRooms(): Flow<List<RuangEntity>>

    @Query("SELECT * FROM ruang WHERE id = :id")
    suspend fun getRoomById(id: Long): RuangEntity?

    @Query("SELECT * FROM ruang WHERE isActive = 1")
    suspend fun getAllRoomsOnce(): List<RuangEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RuangEntity>)

    @Query("DELETE FROM ruang")
    suspend fun clearRooms()

    /** Hapus penanda isMyRoom semua room — dipanggil syncMyRooms sebelum menandai ulang (replace-all). */
    @Query("UPDATE ruang SET isMyRoom = 0")
    suspend fun resetMyRooms()

    // ── Room Items (pivot) ──

    @Query("SELECT * FROM room_item")
    suspend fun getAllRoomItems(): List<RoomItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomItems(items: List<RoomItemEntity>)

    @Query("DELETE FROM room_item")
    suspend fun clearRoomItems()

    // ── User Rooms (pivot) ──

    @Query("SELECT * FROM user_room")
    suspend fun getAllUserRooms(): List<UserRoomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRooms(items: List<UserRoomEntity>)

    @Query("DELETE FROM user_room")
    suspend fun clearUserRooms()

    // ── Inspection Status by Date ──

    @Query("SELECT DISTINCT roomId FROM draf_inspeksi WHERE businessDate = :date")
    suspend fun getDraftRoomIdsForDate(date: String): List<Long>

    @Query("SELECT DISTINCT roomId FROM inspection WHERE businessDate = :date")
    suspend fun getInspectedRoomIdsForDate(date: String): List<Long>

    // ── Inspection History ──

    @Query("SELECT * FROM inspection ORDER BY createdAt DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspection WHERE status = :status ORDER BY createdAt DESC")
    fun getInspectionsByStatus(status: String): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspection WHERE businessDate = :date ORDER BY createdAt DESC")
    fun getInspectionsByDate(date: String): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspection WHERE status = :status AND businessDate = :date ORDER BY createdAt DESC")
    fun getInspectionsByStatusAndDate(status: String, date: String): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspection ORDER BY createdAt DESC")
    suspend fun getAllInspectionsOnce(): List<InspectionEntity>

    @Query("SELECT * FROM inspection WHERE id = :id")
    suspend fun getInspectionById(id: Long): InspectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Query("DELETE FROM inspection WHERE id = :id")
    suspend fun deleteInspection(id: Long)

    // ── Inspection Details ──

    @Query("SELECT * FROM inspection_detail WHERE inspectionId = :inspectionId")
    suspend fun getDetailsForInspection(inspectionId: Long): List<InspectionDetailEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetails(details: List<InspectionDetailEntity>)

    // ── Inspection Photos ──

    @Query("SELECT * FROM inspection_photo WHERE detailId = :detailId")
    suspend fun getPhotosForDetail(detailId: Long): List<InspectionPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<InspectionPhotoEntity>)

    /** Update referensi + path lokal foto setelah re-upload (ADR-0016) — nama file server baru. */
    @Query("UPDATE inspection_photo SET photoFileName = :fileName, thumbnailFileName = :thumbnailName, localPath = :localPath WHERE id = :photoId")
    suspend fun updatePhotoAfterReplace(photoId: Long, fileName: String, thumbnailName: String?, localPath: String?)

    /** Semua foto milik satu inspeksi (join via inspection_detail) — untuk tampilan lokal-first (ADR-0016). */
    @Query("""
        SELECT p.* FROM inspection_photo p
        INNER JOIN inspection_detail d ON p.detailId = d.id
        WHERE d.inspectionId = :inspectionId
    """)
    suspend fun getPhotosForInspection(inspectionId: Long): List<InspectionPhotoEntity>

    // ── User Cache ──

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM user")
    suspend fun clearUsers()
}
