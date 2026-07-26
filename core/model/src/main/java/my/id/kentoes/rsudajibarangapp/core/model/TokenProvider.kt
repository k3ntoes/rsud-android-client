package my.id.kentoes.rsudajibarangapp.core.model

/**
 * Kontrak untuk penyedia token — dipakai oleh AuthInterceptor.
 * Implementasi konkrit ada di :core:datastore (TokenManager).
 * Interface ini memungkinkan :core:network tidak perlu dependen ke :core:datastore.
 */
interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun clearTokens()
}
