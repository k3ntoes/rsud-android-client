package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "user_room")
data class UserRoomEntity(
    @PrimaryKey
    val id: Long,
    val userId: Int,
    val roomId: Long,
    val createdAt: String? = null
)
