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
 * Provider<AuthRepository> memutus circular dependency:
 * AuthRepo → AuthApi → Retrofit → OkHttpClient → TokenAuthenticator
 *
 * Hilt multi-module akan meresolve AuthRepository dari :feature:auth saat kompilasi :app.
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
                    runBlocking { handler.forceLogout() }
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
