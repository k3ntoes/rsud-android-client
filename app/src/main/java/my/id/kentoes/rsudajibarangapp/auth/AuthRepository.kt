package my.id.kentoes.rsudajibarangapp.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import my.id.kentoes.rsudajibarangapp.auth.api.AuthApi
import my.id.kentoes.rsudajibarangapp.auth.api.LoginRequest
import my.id.kentoes.rsudajibarangapp.auth.api.RefreshRequest
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

/** Status autentikasi reaktif */
sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val isRefreshing: Boolean = false) : AuthState()
    data class Error(val message: String) : AuthState()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /** Inisialisasi — cek apakah ada token tersimpan */
    suspend fun init() {
        val isLoggedIn = tokenManager.isLoggedIn()
        _authState.value = if (isLoggedIn) {
            AuthState.Authenticated()
        } else {
            AuthState.Unauthenticated
        }
    }

    /** Login — simpan token jika sukses */
    suspend fun login(username: String, password: String): Boolean {
        _authState.value = AuthState.Loading
        return try {
            val response = authApi.login(LoginRequest(username, password))
            val data = response.data
            if (response.success && data != null) {
                tokenManager.saveTokens(
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken
                )
                _authState.value = AuthState.Authenticated()
                true
            } else {
                _authState.value = AuthState.Error(response.message ?: "Login gagal")
                false
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Koneksi gagal")
            false
        }
    }

    /** Refresh token — dipanggil oleh TokenAuthenticator */
    suspend fun refreshToken(): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: return false
        return try {
            val response = authApi.refresh(RefreshRequest(refreshToken))
            val data = response.data
            if (response.success && data != null) {
                tokenManager.saveTokens(
                    accessToken = data.accessToken,
                    refreshToken = refreshToken // refresh token tetap sama
                )
                true
            } else {
                forceLogout()
                false
            }
        } catch (e: Exception) {
            forceLogout()
            false
        }
    }

    /** Logout — hapus token */
    suspend fun logout() {
        val refreshToken = tokenManager.getRefreshToken()
        try {
            refreshToken?.let { authApi.logout(RefreshRequest(it)) }
        } catch (_: Exception) { /* ignore */ }
        forceLogout()
    }

    /** Ambil Access Token yang tersimpan */
    suspend fun getAccessToken(): String? = tokenManager.getAccessToken()

    /** Force Logout — hapus token + redirect ke login */
    suspend fun forceLogout() {
        tokenManager.clearTokens()
        _authState.value = AuthState.Unauthenticated
    }
}
