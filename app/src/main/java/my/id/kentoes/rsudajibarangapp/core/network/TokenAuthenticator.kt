package my.id.kentoes.rsudajibarangapp.core.network

import kotlinx.coroutines.runBlocking
import my.id.kentoes.rsudajibarangapp.auth.AuthRepository
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * OkHttp Authenticator untuk auto-refresh Access Token.
 * Mendeteksi error code TOKEN_EXPIRED (refresh) vs TOKEN_INVALID (force logout).
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authRepositoryProvider: Provider<AuthRepository>
) : Authenticator {

    private val refreshLock = Any()
    private var isRefreshing = false

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") == null) return null
        if (responseCount(response) > 1) return null

        // Parse error code — force logout for TOKEN_INVALID
        val errorCode = ApiErrorUtil.extractErrorCode(response)
        if (errorCode == "TOKEN_INVALID") {
            runBlocking { authRepositoryProvider.get().forceLogout() }
            return null
        }

        return synchronized(refreshLock) {
            if (isRefreshing) return@synchronized null

            isRefreshing = true
            try {
                val handler = authRepositoryProvider.get()
                val refreshed = runBlocking { handler.refreshToken() }
                if (refreshed) {
                    val newToken = runBlocking { handler.getAccessToken() }
                    newToken?.let {
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $it")
                            .build()
                    }
                } else {
                    // refreshToken() sudah menangani forceLogout sendiri saat token ditolak server
                    // (401/403). Kegagalan jaringan TIDAK memicu logout paksa di sini — sesi tetap valid.
                    null
                }
            } finally {
                isRefreshing = false
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
