package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Bukti foto untuk item dalam draf.
 * Satu DrafItem bisa memiliki banyak DrafFoto (unlimited).
 */
@Entity(
    tableName = "draf_foto",
    foreignKeys = [
        ForeignKey(
            entity = DrafItem::class,
            parentColumns = ["id"],
            childColumns = ["drafItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("drafItemId")]
)
data class DrafFoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val drafItemId: Long,
    val pathLokal: String // path file foto di penyimpanan lokal
)
