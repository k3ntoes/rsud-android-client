package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "inspection_detail",
    foreignKeys = [
        ForeignKey(
            entity = InspectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["inspectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("inspectionId")]
)
data class InspectionDetailEntity(
    @PrimaryKey
    val id: Long,
    val inspectionId: Long,
    val itemId: Long,
    val itemNameSnapshot: String? = null,
    val score: Int = 0
)
