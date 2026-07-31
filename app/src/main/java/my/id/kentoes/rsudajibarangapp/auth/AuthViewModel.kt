package my.id.kentoes.rsudajibarangapp.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import my.id.kentoes.rsudajibarangapp.sync.SyncWorker
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val authState = authRepository.authState
    val currentUser: StateFlow<UserOut?> = authRepository.currentUser

    init {
        viewModelScope.launch {
            authRepository.init()
            // Sesi di-restore (token masih valid saat app dibuka ulang) → enqueue sync
            // master data + draf pending di background. Tanpa ini, dashboard saat app
            // dibuka ulang dengan cache kosong (mis. setelah forceLogout akun lain)
            // tetap kosong sampai user membuka daftar ruangan.
            if (authState.value is AuthState.Authenticated) {
                SyncWorker.enqueue(context)
            }
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Username dan password harus diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val success = authRepository.login(state.username, state.password)
            // Login sukses → enqueue sync master data + draf pending di background.
            // Tanpa ini, dashboard pertama kali login selalu kosong karena cache lokal
            // belum terisi — SyncWorker men-download master data penuh (items, rooms,
            // room-items, my-rooms, user-rooms, users) saat Network.CONNECTED.
            if (success) {
                SyncWorker.enqueue(context)
            }
            // Baca error dari authState jika login gagal
            val authError = if (!success) {
                val currentAuthState = authRepository.authState.value
                if (currentAuthState is AuthState.Error) currentAuthState.message else "Login gagal"
            } else null
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isSuccess = success,
                error = authError
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** Logout — hapus token & redirect */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
