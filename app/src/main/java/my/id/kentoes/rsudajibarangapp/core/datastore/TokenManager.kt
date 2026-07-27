package my.id.kentoes.rsudajibarangapp.core.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
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
        // Preserve user data saat refresh token
        dataStore.updateData { current ->
            TokenData(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = current.userId,
                username = current.username,
                role = current.role,
                isActive = current.isActive
            )
        }
    }

    suspend fun saveUser(user: UserOut) {
        dataStore.updateData {
            it.copy(
                userId = user.id,
                username = user.username,
                role = user.role,
                isActive = user.isActive
            )
        }
    }

    suspend fun getUser(): UserOut? {
        val data = dataStore.data.first()
        return if (data.userId != 0) {
            UserOut(
                id = data.userId,
                username = data.username,
                role = data.role,
                isActive = data.isActive
            )
        } else null
    }

    suspend fun clearTokens() {
        dataStore.updateData { TokenData() }
    }

    suspend fun isLoggedIn(): Boolean = getAccessToken() != null
}
