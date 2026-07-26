package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Header draf inspeksi.
 * Satu DrafInspeksi memiliki banyak DrafItem.
 */
@Entity(tableName = "draf_inspeksi")
data class DrafInspeksi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val localTimestamp: String, // UTC ISO 8601
    val inspectorId: String? = null,
    val status: String = "DRAFT", // DRAFT, PENDING_SYNC, SYNCED
    val catatan: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
