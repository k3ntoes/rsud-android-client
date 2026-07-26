package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Satu baris item dalam draf inspeksi.
 * Setiap item memiliki skor 0/1/2 dan foto opsional.
 */
@Entity(
    tableName = "draf_item",
    foreignKeys = [
        ForeignKey(
            entity = DrafInspeksi::class,
            parentColumns = ["id"],
            childColumns = ["drafId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("drafId")]
)
data class DrafItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val drafId: Long,
    val itemId: Long,
    val skor: Int = -1, // -1=belum, 0=Berisiko, 1=Minor, 2=Sesuai
    val catatan: String? = null
)
