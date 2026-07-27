package my.id.kentoes.rsudajibarangapp.sync.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

@Serializable
data class UploadPhotoResponse(
    @SerialName("file_name")
    val fileName: String,
    @SerialName("thumbnail_name")
    val thumbnailName: String? = null
)

@Serializable
data class InspectionSubmit(
    @SerialName("room_id")
    val roomId: Long,
    @SerialName("local_timestamp")
    val localTimestamp: String,
    @SerialName("business_date")
    val businessDate: String,
    val details: List<DetailSubmit>
)

@Serializable
data class DetailSubmit(
    @SerialName("item_id")
    val itemId: Long,
    val score: Int,
    val photos: List<PhotoSubmit> = emptyList()
)

@Serializable
data class PhotoSubmit(
    @SerialName("file_name")
    val fileName: String,
    @SerialName("sort_order")
    val sortOrder: Int = 0
)

interface SyncApi {

    /** Upload foto — return nama file di server */
    @Multipart
    @POST("upload")
    suspend fun uploadPhoto(
        @Part photo: MultipartBody.Part
    ): UploadPhotoResponse

    /** Submit inspeksi lengkap (JSON + daftar nama foto) */
    @POST("inspections")
    suspend fun submitInspection(
        @Body request: InspectionSubmit
    ): Unit
}
