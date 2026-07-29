package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "inspection")
data class InspectionEntity(
    @PrimaryKey
    val id: Long,
    val roomId: Long,
    val inspectorId: Int,
    val status: String, // PENDING, APPROVED, REJECTED
    val businessDate: String? = null,
    val localTimestamp: String? = null,
    val rejectionReason: String? = null,
    val createdAt: String? = null,
    val rawJson: String? = null // full response JSON for offline detail
)
