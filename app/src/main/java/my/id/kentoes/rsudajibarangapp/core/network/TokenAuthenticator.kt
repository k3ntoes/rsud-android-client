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
 *
 * Saat menerima 401 Unauthorized:
 * 1. Refresh token via [AuthRepository.refreshToken]
 * 2. Jika berhasil → retry request original dengan token baru
 * 3. Jika gagal → [AuthRepository.forceLogout], return null
 *
 * Gunakan [Provider] untuk memutus circular dependency:
 * AuthRepo → AuthApi → Retrofit → OkHttpClient → TokenAuthenticator
 *
 * Dilengkapi [refreshLock] mencegah race condition multiple 401 bersamaan.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authRepositoryProvider: Provider<AuthRepository>
) : Authenticator {

    private val refreshLock = Any()
    private var isRefreshing = false

    override fun authenticate(route: Route?, response: Response): Request? {
        // Hanya handle 401 yang sudah punya Authorization header
        // (artinya token expired, bukan endpoint publik)
        if (response.request.header("Authorization") == null) return null
        // Maksimal 1 retry — hindari infinite loop
        if (responseCount(response) > 1) return null

        return synchronized(refreshLock) {
            if (isRefreshing) return@synchronized null

            isRefreshing = true
            try {
                val authRepo = authRepositoryProvider.get()
                val refreshed = runBlocking { authRepo.refreshToken() }
                if (refreshed) {
                    val newToken = runBlocking { authRepo.getAccessToken() }
                    newToken?.let {
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $it")
                            .build()
                    }
                } else {
                    runBlocking { authRepo.forceLogout() }
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
