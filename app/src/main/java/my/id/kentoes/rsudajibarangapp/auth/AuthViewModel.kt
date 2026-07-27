package my.id.kentoes.rsudajibarangapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val authState = authRepository.authState
    val currentUser: StateFlow<UserOut?> = authRepository.currentUser

    init {
        viewModelScope.launch {
            authRepository.init()
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
