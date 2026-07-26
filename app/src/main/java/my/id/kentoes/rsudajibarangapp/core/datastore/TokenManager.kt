package my.id.kentoes.rsudajibarangapp.core.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manajemen token JWT dengan penyimpanan terenkripsi via DataStore + Tink AEAD.
 *
 * Enkripsi/dekripsi terjadi TRANSPARAN di layer DataStore (AeadSerializer),
 * jadi TokenManager tidak perlu manual encrypt/decrypt.
 */
@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<TokenData>
) {

    suspend fun getAccessToken(): String? {
        return dataStore.data.map { it.accessToken.ifBlank { null } }.first()
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.map { it.refreshToken.ifBlank { null } }.first()
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.updateData { TokenData(accessToken = accessToken, refreshToken = refreshToken) }
    }

    suspend fun clearTokens() {
        dataStore.updateData { TokenData() }
    }

    suspend fun isLoggedIn(): Boolean = getAccessToken() != null
}
