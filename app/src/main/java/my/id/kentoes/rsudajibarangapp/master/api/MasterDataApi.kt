package my.id.kentoes.rsudajibarangapp.master.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import my.id.kentoes.rsudajibarangapp.core.model.PaginatedResponse
import my.id.kentoes.rsudajibarangapp.core.model.SyncResponse
import retrofit2.http.GET
import retrofit2.http.Query

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

@Serializable
data class RoomItemDto(
    val id: Long,
    @SerialName("room_id")
    val roomId: Long,
    @SerialName("item_id")
    val itemId: Long,
    @SerialName("created_at")
    val createdAt: String? = null
)

interface MasterDataApi {

    @GET("inspection-items")
    suspend fun getItems(
        @Query("since") since: String? = null
    ): PaginatedResponse<ItemOut>

    @GET("rooms")
    suspend fun getRooms(
        @Query("since") since: String? = null
    ): PaginatedResponse<RoomOut>

    @GET("room-items")
    suspend fun getRoomItems(
        @Query("since") since: String? = null
    ): SyncResponse<RoomItemDto>
}
