package my.id.kentoes.rsudajibarangapp.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generic response wrapper untuk REST API.
 * Dipindahkan dari core.network ke core.model agar bisa di-share antar modul.
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
)

/**
 * SyncResponse wrapper — endpoint master data & sync mengembalikan ini.
 */
@Serializable
data class SyncResponse<T>(
    val data: List<T> = emptyList(),
    @SerialName("synced_at")
    val syncedAt: String? = null
)

/**
 * PaginatedResponse untuk endpoint list yang menggunakan server-driven pagination.
 */
@Serializable
data class PaginatedResponse<T>(
    val items: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1
)
