package my.id.kentoes.rsudajibarangapp.core.datastore

import kotlinx.serialization.Serializable

/**
 * Data token terenkripsi yang disimpan via DataStore + AeadSerializer.
 * Access/Refresh token dienkripsi otomatis oleh datastore-tink.
 */
@Serializable
data class TokenData(
    val accessToken: String = "",
    val refreshToken: String = ""
)
