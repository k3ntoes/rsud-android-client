package my.id.kentoes.rsudajibarangapp.master.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import my.id.kentoes.rsudajibarangapp.core.model.ApiResponse
import retrofit2.http.GET

@Serializable
data class MasterDataItemResponse(
    val id: Long,
    val nama: String,
    val kategori: String,
    val deskripsi: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class RuangResponse(
    val id: Long,
    val nama: String,
    val lantai: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

interface MasterDataApi {

    @GET("master/items")
    suspend fun getItems(): ApiResponse<List<MasterDataItemResponse>>

    @GET("master/rooms")
    suspend fun getRooms(): ApiResponse<List<RuangResponse>>
}
