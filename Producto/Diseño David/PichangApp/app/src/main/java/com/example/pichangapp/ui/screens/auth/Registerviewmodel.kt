package com.pichangapp.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.pichangapp.core.BaseViewModel
import com.pichangapp.core.NetworkResult
import com.pichangapp.domain.model.SportType
import com.pichangapp.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UiState ─────────────────────────────────────────────────────────────────
data class RegisterUiState(
    val name           : String         = "",
    val email          : String         = "",
    val password       : String         = "",
    val confirmPassword: String         = "",
    val selectedSports : Set<SportType> = emptySet(),
    val nameError      : String?        = null,
    val emailError     : String?        = null,
    val passwordError  : String?        = null,
    val confirmError   : String?        = null,
    val isLoading      : Boolean        = false,
    val registerSuccess: Boolean        = false,
    val errorMessage   : String?        = null
) {
    val isFormValid: Boolean
        get() = name.isNotBlank() && email.isNotBlank()
                && password.length >= 6 && password == confirmPassword
                && nameError == null && emailError == null
                && passwordError == null && confirmError == null
}

// ─── ViewModel ───────────────────────────────────────────────────────────────
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        val error = if (name.trim().length < 2 && name.isNotEmpty()) "Mínimo 2 caracteres" else null
        _uiState.update { it.copy(name = name, nameError = error) }
    }

    fun onEmailChange(email: String) {
        val error = if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            "Email no válido" else null
        _uiState.update { it.copy(email = email, emailError = error) }
    }

    fun onPasswordChange(password: String) {
        val error = if (password.isNotEmpty() && password.length < 6) "Mínimo 6 caracteres" else null
        val confirmError = if (_uiState.value.confirmPassword.isNotEmpty() && _uiState.value.confirmPassword != password)
            "Las contraseñas no coinciden" else null
        _uiState.update { it.copy(password = password, passwordError = error, confirmError = confirmError) }
    }

    fun onConfirmPasswordChange(confirm: String) {
        val error = if (confirm.isNotEmpty() && confirm != _uiState.value.password)
            "Las contraseñas no coinciden" else null
        _uiState.update { it.copy(confirmPassword = confirm, confirmError = error) }
    }

    fun toggleSport(sport: SportType) {
        val current = _uiState.value.selectedSports
        _uiState.update {
            it.copy(selectedSports = if (sport in current) current - sport else current + sport)
        }
    }

    fun register() {
        val s = _uiState.value
        if (!s.isFormValid) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = registerUseCase(s.name, s.email, s.password, s.confirmPassword)) {
                is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
                is NetworkResult.Error   -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}