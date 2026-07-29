package my.id.kentoes.rsudajibarangapp.core.network

import kotlinx.coroutines.runBlocking
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor untuk menambahkan header Authorization: Bearer.
 * Juga mendeteksi error code TOKEN_EXPIRED / TOKEN_INVALID dari response.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    private val noAuthPaths: List<String> = listOf(
        "/auth/login",
        "/auth/refresh"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val path = originalRequest.url.encodedPath
        if (noAuthPaths.any { path.endsWith(it) }) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenManager.getAccessToken() }

        return if (token != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
