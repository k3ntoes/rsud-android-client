package my.id.kentoes.rsudajibarangapp.sync.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import my.id.kentoes.rsudajibarangapp.core.network.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

@Serializable
data class UploadPhotoResponse(
    @SerialName("file_name")
    val fileName: String
)

@Serializable
data class SubmitInspectionRequest(
    @SerialName("room_id")
    val roomId: Long,
    @SerialName("local_timestamp")
    val localTimestamp: String,
    val items: List<SubmitItem>
)

@Serializable
data class SubmitItem(
    @SerialName("item_id")
    val itemId: Long,
    val skor: Int,
    val catatan: String? = null,
    @SerialName("foto_files")
    val fotoFiles: List<String> // nama file dari hasil upload
)

interface SyncApi {

    /** Upload foto — return nama file di server */
    @Multipart
    @POST("upload/photo")
    suspend fun uploadPhoto(
        @Part photo: MultipartBody.Part
    ): ApiResponse<UploadPhotoResponse>

    /** Submit inspeksi lengkap (JSON + daftar nama foto) */
    @POST("inspection/submit")
    suspend fun submitInspection(
        @retrofit2.http.Body request: SubmitInspectionRequest
    ): ApiResponse<Unit>
}
