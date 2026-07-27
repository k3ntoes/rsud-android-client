package my.id.kentoes.rsudajibarangapp.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.id.kentoes.rsudajibarangapp.auth.api.AuthApi
import my.id.kentoes.rsudajibarangapp.auth.api.ChangePasswordRequest
import my.id.kentoes.rsudajibarangapp.auth.api.LoginRequest
import my.id.kentoes.rsudajibarangapp.auth.api.RefreshRequest
import my.id.kentoes.rsudajibarangapp.auth.api.TokenResponse
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
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

    private val _currentUser = MutableStateFlow<UserOut?>(null)
    val currentUser: StateFlow<UserOut?> = _currentUser.asStateFlow()

    /** Inisialisasi — cek token, muat user lokal, refresh dari server */
    suspend fun init() {
        val isLoggedIn = tokenManager.isLoggedIn()
        if (isLoggedIn) {
            _currentUser.value = tokenManager.getUser()
            _authState.value = AuthState.Authenticated()
            // Refresh profil dari server (abaikan error jika offline)
            refreshCurrentUser()
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    /** Refresh profil user dari endpoint /me */
    suspend fun refreshCurrentUser() {
        try {
            val user = authApi.me()
            tokenManager.saveUser(user)
            _currentUser.value = user
        } catch (_: Exception) {
            // Gagal fetch profil — pakai data lokal yang sudah ada
        }
    }

    /** Login — simpan token & user */
    suspend fun login(username: String, password: String): Boolean {
        _authState.value = AuthState.Loading
        return try {
            val response: TokenResponse = authApi.login(LoginRequest(username, password))
            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
            tokenManager.saveUser(response.user)
            _currentUser.value = response.user
            _authState.value = AuthState.Authenticated()
            true
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Login gagal")
            false
        }
    }

    /** Refresh token — dipanggil oleh TokenAuthenticator */
    suspend fun refreshToken(): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: return false
        return try {
            val response: TokenResponse = authApi.refresh(RefreshRequest(refreshToken))
            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = refreshToken // refresh token tetap sama
            )
            true
        } catch (e: Exception) {
            forceLogout()
            false
        }
    }

    /** Ganti password */
    suspend fun changePassword(oldPassword: String, newPassword: String) {
        authApi.changePassword(ChangePasswordRequest(oldPassword, newPassword))
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

    /** Force Logout — hapus token & user + redirect ke login */
    suspend fun forceLogout() {
        tokenManager.clearTokens()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
}
