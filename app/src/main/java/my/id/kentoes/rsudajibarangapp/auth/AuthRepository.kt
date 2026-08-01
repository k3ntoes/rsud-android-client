package my.id.kentoes.rsudajibarangapp.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.id.kentoes.rsudajibarangapp.auth.api.AuthApi
import my.id.kentoes.rsudajibarangapp.auth.api.ChangePasswordRequest
import my.id.kentoes.rsudajibarangapp.auth.api.LoginRequest
import my.id.kentoes.rsudajibarangapp.auth.api.LogoutRequest
import my.id.kentoes.rsudajibarangapp.auth.api.RefreshRequest
import my.id.kentoes.rsudajibarangapp.auth.api.TokenResponse
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import kotlinx.coroutines.CancellationException
import my.id.kentoes.rsudajibarangapp.core.database.dao.MasterDataDao
import my.id.kentoes.rsudajibarangapp.core.datastore.TokenManager
import my.id.kentoes.rsudajibarangapp.inspection.InspectionRepository
import my.id.kentoes.rsudajibarangapp.master.SyncStateStore
import retrofit2.HttpException
import java.io.IOException
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
    private val tokenManager: TokenManager,
    private val masterDataDao: MasterDataDao,
    private val syncStateStore: SyncStateStore,
    private val inspectionRepository: InspectionRepository
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserOut?>(null)
    val currentUser: StateFlow<UserOut?> = _currentUser.asStateFlow()

    /** Inisialisasi — cek token, muat user lokal, refresh dari server */
    suspend fun init() {
        val isLoggedIn = tokenManager.isLoggedIn()
        if (isLoggedIn) {
            val user = tokenManager.getUser()
            // ADR-0017 R1: sesi lama non-inspector di-force-logout — Android hanya
            // melayani role inspector; supervisor/admin_ppi via web dashboard.
            if (user != null && user.role != ROLE_INSPECTOR) {
                forceLogout()
                return
            }
            _currentUser.value = user
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
            // ADR-0017 R1: role user diubah admin jadi non-inspector → logout paksa
            if (user.role != ROLE_INSPECTOR) {
                forceLogout()
                return
            }
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
            // ADR-0017: tolak role non-inspector SEBELUM menyimpan token — jangan
            // pernah meninggalkan sesi yang bisa masuk dashboard.
            if (response.user.role != ROLE_INSPECTOR) {
                _authState.value = AuthState.Error("Akun ini hanya dapat digunakan via web dashboard")
                return false
            }
            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
            tokenManager.saveUser(response.user)
            _currentUser.value = response.user
            // Akun berbeda dari pemilik draf lokal? Bersihkan draf akun lama.
            // Draf user yang sama (login ulang) dipertahankan — inspectorId cocok.
            try {
                inspectionRepository.clearForeignDrafts(response.user.id.toString())
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Kegagalan membersihkan draf tidak boleh menggagalkan login
            }
            _authState.value = AuthState.Authenticated()
            true
        } catch (e: CancellationException) {
            throw e // jangan telan pembatalan coroutine (konsisten dengan forceLogout)
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
        } catch (e: HttpException) {
            // Server merespons — logout paksa HANYA jika token benar-benar ditolak (401/403).
            // Error lain (mis. 5xx) bukan penolakan token — jangan hapus data user.
            if (e.code() == 401 || e.code() == 403) {
                forceLogout()
            }
            false
        } catch (e: IOException) {
            // Gangguan jaringan sementara — JANGAN logout paksa, sesi masih valid.
            // Menghindari user offline kehilangan draf saat refresh gagal sesaat.
            false
        } catch (_: Exception) {
            forceLogout()
            false
        }
    }

    /** Ganti password */
    suspend fun changePassword(oldPassword: String, newPassword: String) {
        authApi.changePassword(ChangePasswordRequest(oldPassword, newPassword))
    }

    /** Logout — kirim kedua token, lalu hapus lokal */
    suspend fun logout() {
        val refreshToken = tokenManager.getRefreshToken()
        val accessToken = tokenManager.getAccessToken()
        try {
            if (refreshToken != null && accessToken != null) {
                authApi.logout(LogoutRequest(refreshToken, accessToken))
            }
        } catch (_: Exception) { /* ignore */ }
        forceLogout()
    }

    /** Ambil Access Token yang tersimpan */
    suspend fun getAccessToken(): String? = tokenManager.getAccessToken()

    /** Force Logout — hapus token & user + clear cache lokal + redirect ke login */
    suspend fun forceLogout() {
        tokenManager.clearTokens()
        try {
            clearLocalCache()
        } catch (e: CancellationException) {
            throw e // jangan telan pembatalan coroutine
        } catch (_: Exception) {
            // Kegagalan membersihkan cache tidak boleh menghalangi logout selesai
        }
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Bersihkan seluruh cache master data + SyncState agar akun berikutnya sync penuh
     * dari epoch — mencegah room/assignment akun lama bocor ke akun baru.
     * Draf TIDAK dihapus di sini: draf bertag inspectorId dan dibersihkan hanya saat
     * akun yang berbeda login (lihat login()) — user yang sama login ulang tidak
     * kehilangan draf.
     */
    private suspend fun clearLocalCache() {
        masterDataDao.clearItems()
        masterDataDao.clearRooms()
        masterDataDao.clearRoomItems()
        masterDataDao.clearUserRooms()
        masterDataDao.clearUsers()
        syncStateStore.clear()
    }

    companion object {
        /** Android = klien inspector-only (ADR-0017) — satu-satunya role yang diizinkan login. */
        private const val ROLE_INSPECTOR = "inspector"
    }
}
