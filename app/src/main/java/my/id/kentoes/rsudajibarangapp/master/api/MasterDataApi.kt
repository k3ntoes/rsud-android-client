package my.id.kentoes.rsudajibarangapp.master.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class ItemOut(
    val id: Long,
    val name: String,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class RoomOut(
    val id: Long,
    val name: String,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

interface MasterDataApi {

    @GET("master/inspection-items")
    suspend fun getItems(): List<ItemOut>

    @GET("master/rooms")
    suspend fun getRooms(): List<RoomOut>
}
