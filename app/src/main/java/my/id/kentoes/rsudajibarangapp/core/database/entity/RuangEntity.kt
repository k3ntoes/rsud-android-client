package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "ruang")
data class RuangEntity(
    @PrimaryKey
    val id: Long,
    val nama: String,
    val lantai: String? = null,
    val isActive: Boolean = true,
    val updatedAt: String? = null
)
