package my.id.kentoes.rsudajibarangapp.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.id.kentoes.rsudajibarangapp.auth.AuthRepository
import my.id.kentoes.rsudajibarangapp.auth.api.UserOut
import javax.inject.Inject

data class ProfileUiState(
    val currentUser: UserOut? = null,
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val success: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }
    }

    fun onOldPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(oldPassword = value, error = null, success = null)
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value, error = null, success = null)
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, error = null, success = null)
    }

    fun changePassword() {
        val state = _uiState.value
        if (state.oldPassword.isBlank()) {
            _uiState.value = state.copy(error = "Password lama harus diisi")
            return
        }
        if (state.newPassword.length < 6) {
            _uiState.value = state.copy(error = "Password baru minimal 6 karakter")
            return
        }
        if (state.newPassword != state.confirmPassword) {
            _uiState.value = state.copy(error = "Konfirmasi password tidak cocok")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, success = null)
            try {
                authRepository.changePassword(state.oldPassword, state.newPassword)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    oldPassword = "",
                    newPassword = "",
                    confirmPassword = "",
                    success = "Password berhasil diubah"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal mengubah password"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(success = null, error = null)
    }
}
