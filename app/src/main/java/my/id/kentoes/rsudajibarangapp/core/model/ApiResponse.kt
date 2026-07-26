package my.id.kentoes.rsudajibarangapp.core.model

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
