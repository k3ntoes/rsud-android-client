package my.id.kentoes.rsudajibarangapp.core.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafFoto
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafInspeksi
import my.id.kentoes.rsudajibarangapp.core.database.entity.DrafItem

@Dao
interface DrafDao {

    // ── Draf Inspeksi ──

    @Query("SELECT * FROM draf_inspeksi ORDER BY createdAt DESC")
    fun getAllDrafts(): Flow<List<DrafInspeksi>>

    @Query("SELECT * FROM draf_inspeksi WHERE status = :status ORDER BY createdAt DESC")
    fun getDraftsByStatus(status: String): Flow<List<DrafInspeksi>>

    @Query("SELECT * FROM draf_inspeksi WHERE id = :id")
    suspend fun getDraftById(id: Long): DrafInspeksi?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DrafInspeksi): Long

    @Query("UPDATE draf_inspeksi SET status = :status WHERE id = :id")
    suspend fun updateDraftStatus(id: Long, status: String)

    @Delete
    suspend fun deleteDraft(draft: DrafInspeksi)

    // ── Draf Item ──

    @Query("SELECT * FROM draf_item WHERE drafId = :drafId")
    suspend fun getItemsForDraft(drafId: Long): List<DrafItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: DrafItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<DrafItem>)

    @Query("UPDATE draf_item SET skor = :skor, catatan = :catatan WHERE id = :id")
    suspend fun updateItemSkor(id: Long, skor: Int, catatan: String?)

    @Delete
    suspend fun deleteItem(item: DrafItem)

    // ── Draf Foto ──

    @Query("SELECT * FROM draf_foto WHERE drafItemId = :drafItemId")
    suspend fun getPhotosForItem(drafItemId: Long): List<DrafFoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: DrafFoto): Long

    @Delete
    suspend fun deletePhoto(photo: DrafFoto)

    /** Hapus draft — foto & item terhapus otomatis via CASCADE FK */
    @Transaction
    suspend fun deleteDraftCascade(draft: DrafInspeksi) {
        deleteDraft(draft)
    }
}
