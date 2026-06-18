package com.pichangapp.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.pichangapp.core.BaseViewModel
import com.pichangapp.core.NetworkResult
import com.pichangapp.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UiState ─────────────────────────────────────────────────────────────────
data class LoginUiState(
    val email          : String  = "",
    val password       : String  = "",
    val emailError     : String? = null,
    val passwordError  : String? = null,
    val isLoading      : Boolean = false,
    val loginSuccess   : Boolean = false,
    val errorMessage   : String? = null
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank()
                && emailError == null && passwordError == null
}

// ─── ViewModel ───────────────────────────────────────────────────────────────
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        val error = validateEmail(email)
        _uiState.update { it.copy(email = email, emailError = error) }
    }

    fun onPasswordChange(password: String) {
        val error = if (password.length < 6 && password.isNotEmpty())
            "Mínimo 6 caracteres" else null
        _uiState.update { it.copy(password = password, passwordError = error) }
    }

    fun login() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = loginUseCase(state.email, state.password)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return null
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            "Email no válido" else null
    }
}