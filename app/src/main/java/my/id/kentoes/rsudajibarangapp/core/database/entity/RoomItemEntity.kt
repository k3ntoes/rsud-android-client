package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "room_item")
data class RoomItemEntity(
    @PrimaryKey
    val id: Long,
    val roomId: Long,
    val itemId: Long,
    /** Urutan tampilan item dalam checklist ruangan — kontrak BE ADR-0013 (ADR-0019 Android). */
    val sortOrder: Int = 0,
    val createdAt: String? = null
)
