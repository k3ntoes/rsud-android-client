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

    // ── Clear draf akun lain (ganti akun) ──

    /** Semua path file foto draf milik user LAIN — untuk dihapus sebelum barisnya dihapus. */
    @Query(
        """
        SELECT pathLokal FROM draf_foto
        WHERE drafItemId IN (
            SELECT id FROM draf_item WHERE drafId IN (
                SELECT id FROM draf_inspeksi
                WHERE inspectorId IS NOT NULL AND inspectorId != :inspectorId
            )
        )
        """
    )
    suspend fun getForeignDraftPhotoPaths(inspectorId: String): List<String>

    /**
     * Hapus draf milik user LAIN (inspectorId != user aktif) — item & foto ikut terhapus
     * via CASCADE FK. Draf milik user aktif & draf legacy (inspectorId null) dipertahankan:
     * user yang sama login ulang tidak boleh kehilangan draf.
     */
    @Query("DELETE FROM draf_inspeksi WHERE inspectorId IS NOT NULL AND inspectorId != :inspectorId")
    suspend fun clearForeignDrafts(inspectorId: String)

    // ── Cleanup foto draf yatim (worker periodik) ──

    /** Semua pathLokal yang direferensikan baris draf_foto valid — pembeda file yatim di disk. */
    @Query("SELECT pathLokal FROM draf_foto")
    suspend fun getAllReferencedPhotoPaths(): List<String>

    /** Baris draf_foto tanpa header valid — parent draf_item-nya sudah tidak ada. */
    @Query(
        """
        SELECT df.* FROM draf_foto df
        LEFT JOIN draf_item di ON df.drafItemId = di.id
        WHERE di.id IS NULL
        """
    )
    suspend fun getOrphanedDraftPhotos(): List<DrafFoto>

    /** Hapus semua baris draf_foto tanpa parent valid. Mengembalikan jumlah baris yang dihapus. */
    @Query("DELETE FROM draf_foto WHERE drafItemId NOT IN (SELECT id FROM draf_item)")
    suspend fun deleteOrphanedDraftPhotos(): Int

    // ── Draf Item ──

    @Query("SELECT * FROM draf_item WHERE drafId = :drafId")
    suspend fun getItemsForDraft(drafId: Long): List<DrafItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: DrafItem): Long

    // ── Draf Foto ──

    @Query("SELECT * FROM draf_foto WHERE drafItemId = :drafItemId")
    suspend fun getPhotosForItem(drafItemId: Long): List<DrafFoto>

    /** Semua path file foto milik satu draf — untuk dihapus sebelum baris draf dihapus. */
    @Query("SELECT pathLokal FROM draf_foto WHERE drafItemId IN (SELECT id FROM draf_item WHERE drafId = :draftId)")
    suspend fun getPhotoPathsForDraft(draftId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: DrafFoto): Long

    @Delete
    suspend fun deletePhoto(photo: DrafFoto)

    /** Hapus draft — foto & item terhapus otomatis via CASCADE FK */
    @Transaction
    suspend fun deleteDraftCascade(draft: DrafInspeksi) {
        deleteDraft(draft)
    }

    /** Atomic: update status SYNCED + hapus draft (cegah ghost entry jika crash di tengah) */
    @Transaction
    suspend fun markSyncedAndDelete(draftId: Long) {
        updateDraftStatus(draftId, "SYNCED")
        getDraftById(draftId)?.let { deleteDraft(it) }
    }
}
