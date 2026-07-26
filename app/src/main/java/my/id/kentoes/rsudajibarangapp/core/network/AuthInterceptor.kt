package my.id.kentoes.rsudajibarangapp.core.network

import kotlinx.coroutines.runBlocking
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor untuk menambahkan header Authorization: Bearer.
 *
 * Melewatkan endpoint yang tidak perlu autentikasi (login, refresh)
 * menggunakan [noAuthPaths] — bisa dikustomisasi via constructor.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    /** Endpoint yang TIDAK perlu header Authorization */
    private val noAuthPaths: List<String> = listOf(
        "/login",
        "/refresh"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header untuk endpoint publik
        val path = originalRequest.url.encodedPath
        if (noAuthPaths.any { path.endsWith(it) }) {
            return chain.proceed(originalRequest)
        }

        // Ambil token dan tambahkan ke header
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
