package my.id.kentoes.rsudajibarangapp.sync.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import my.id.kentoes.rsudajibarangapp.core.model.PaginatedResponse

@Serializable
data class UploadPhotoResponse(
    @SerialName("photo_file_name")
    val fileName: String,
    @SerialName("thumbnail_file_name")
    val thumbnailName: String? = null,
    @SerialName("file_size")
    val fileSize: Long? = null
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
    /**
     * Catatan inspektur per item (Q2, grill-with-docs 2026-08). Opsional — BE
     * (Pydantic default) mengabaikan field tak dikenal, jadi mengirim sekarang aman;
     * kolom `catatan` di BE menyusul (koordinasi kontrak §4.1).
     */
    val catatan: String? = null,
    val photos: List<PhotoSubmit> = emptyList()
)

@Serializable
data class PhotoSubmit(
    @SerialName("file_name")
    val fileName: String,
    @SerialName("sort_order")
    val sortOrder: Int = 0
)

@Serializable
data class PhotoOutDto(
    val id: Long,
    @SerialName("photo_file_name")
    val photoFileName: String,
    @SerialName("thumbnail_file_name")
    val thumbnailFileName: String? = null,
    @SerialName("sort_order")
    val sortOrder: Int = 0
)

@Serializable
data class InspectionDetailOutDto(
    val id: Long,
    @SerialName("item_id")
    val itemId: Long,
    @SerialName("item_name_snapshot")
    val itemNameSnapshot: String,
    val score: Int,
    val photos: List<PhotoOutDto> = emptyList()
)

@Serializable
data class InspectionOutDto(
    val id: Long,
    @SerialName("room_id")
    val roomId: Long,
    @SerialName("inspector_id")
    val inspectorId: Int,
    val status: String,
    @SerialName("business_date")
    val businessDate: String? = null,
    @SerialName("local_timestamp")
    val localTimestamp: String? = null,
    @SerialName("rejection_reason")
    val rejectionReason: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val details: List<InspectionDetailOutDto> = emptyList()
)

@Serializable
data class InspectionListItemDto(
    val id: Long,
    @SerialName("room_id")
    val roomId: Long,
    @SerialName("inspector_id")
    val inspectorId: Int,
    val status: String,
    @SerialName("business_date")
    val businessDate: String? = null,
    @SerialName("local_timestamp")
    val localTimestamp: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("detail_count")
    val detailCount: Int = 0
)

@Serializable
data class ApiErrorDto(
    val detail: String,
    val code: String
)

interface SyncApi {

    /** Upload foto — return nama file di server */
    @Multipart
    @POST("upload")
    suspend fun uploadPhoto(
        @Part file: MultipartBody.Part
    ): UploadPhotoResponse

    /** Submit inspeksi lengkap (JSON + daftar nama foto) */
    @POST("inspections")
    suspend fun submitInspection(
        @Body request: InspectionSubmit
    ): InspectionOutDto

    /** List inspeksi (paginated) */
    @GET("inspections")
    suspend fun getInspections(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("status") status: String? = null
    ): PaginatedResponse<InspectionListItemDto>

    /** Detail inspeksi */
    @GET("inspections/{id}")
    suspend fun getInspectionDetail(
        @Path("id") id: Long
    ): InspectionOutDto

    /**
     * Replace foto inspeksi (re-upload foto rusak/hilang) — ADR-0016.
     * Endpoint SUDAH diimplementasikan di backend (ADR-0012) — file lama + thumbnail
     * lama dihapus server, lalu regenerate. Response berisi PhotoOutDto nama file baru.
     */
    @Multipart
    @PUT("inspections/{id}/photos/{photoId}")
    suspend fun replacePhoto(
        @Path("id") id: Long,
        @Path("photoId") photoId: Long,
        @Part file: MultipartBody.Part
    ): PhotoOutDto
}
