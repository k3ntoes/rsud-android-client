package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "room_item")
data class RoomItemEntity(
    @PrimaryKey
    val id: Long,
    val roomId: Long,
    val itemId: Long,
    val createdAt: String? = null
)
