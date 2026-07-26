package my.id.kentoes.rsudajibarangapp.core.model

/**
 * Handler untuk refresh token — dipakai oleh TokenAuthenticator.
 * Implementasi di AuthRepository (:feature:auth).
 * Interface ini memungkinkan :core:network tidak perlu dependen ke :feature:auth.
 */
interface TokenRefreshHandler {
    suspend fun refreshToken(): Boolean
    suspend fun getAccessToken(): String?
    suspend fun forceLogout()
}
