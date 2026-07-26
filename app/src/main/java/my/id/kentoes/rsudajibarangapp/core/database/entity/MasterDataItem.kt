package my.id.kentoes.rsudajibarangapp.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "master_data_item")
data class MasterDataItem(
    @PrimaryKey
    val id: Long,
    val nama: String,
    val kategori: String,
    val deskripsi: String? = null,
    val isActive: Boolean = true
)
