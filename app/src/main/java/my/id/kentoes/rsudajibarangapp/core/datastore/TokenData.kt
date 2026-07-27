package my.id.kentoes.rsudajibarangapp.core.datastore

import kotlinx.serialization.Serializable

/**
 * Data token terenkripsi yang disimpan via DataStore + AeadSerializer.
 * Access/Refresh token dienkripsi otomatis oleh datastore-tink.
 */
@Serializable
data class TokenData(
    val accessToken: String = "",
    val refreshToken: String = "",
    val userId: Int = 0,
    val username: String = "",
    val role: String = "",
    val isActive: Boolean = true
)
