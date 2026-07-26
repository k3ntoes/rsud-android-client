package my.id.kentoes.rsudajibarangapp.core.network

import kotlinx.serialization.Serializable

/**
 * Generic response wrapper untuk REST API.
 * Menyesuaikan dengan envelope response server.
 *
 * Contoh response:
 * ```json
 * {
 *   "success": true,
 *   "message": "Login berhasil",
 *   "data": { ... }
 * }
 * ```
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
)
