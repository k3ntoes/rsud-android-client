package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "inspection_photo",
    foreignKeys = [
        ForeignKey(
            entity = InspectionDetailEntity::class,
            parentColumns = ["id"],
            childColumns = ["detailId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("detailId")]
)
data class InspectionPhotoEntity(
    @PrimaryKey
    val id: Long,
    val detailId: Long,
    val photoFileName: String,
    val thumbnailFileName: String? = null,
    val sortOrder: Int = 0
)
