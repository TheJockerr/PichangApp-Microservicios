package cl.duoc.pichangapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    fun cambiarPassword(actual: String, nueva: String) {
        if (_state.value.isLoading) return
        _state.value = ChangePasswordUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val response = userRepository.changePassword(actual, nueva)
                when {
                    response.isSuccessful ->
                        _state.value = ChangePasswordUiState(success = true)
                    response.code() == 400 || response.code() == 401 ->
                        _state.value = ChangePasswordUiState(errorMessage = "Contraseña actual incorrecta")
                    else ->
                        _state.value = ChangePasswordUiState(errorMessage = "No se pudo actualizar la contraseña")
                }
            } catch (e: Exception) {
                _state.value = ChangePasswordUiState(errorMessage = "Error de conexión")
            }
        }
    }

    /** Limpia el estado tras consumir un evento de éxito/error. */
    fun consumed() {
        _state.value = _state.value.copy(success = false, errorMessage = null)
    }
}
