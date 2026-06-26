package cl.duoc.pichangapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.core.datastore.HistorialPreferenceManager
import cl.duoc.pichangapp.data.repository.AuthRepository
import cl.duoc.pichangapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val loggedOut: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val historialPreferenceManager: HistorialPreferenceManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    /** Visibilidad del historial (espejo local; por defecto visible). */
    val historialVisible: StateFlow<Boolean> = historialPreferenceManager.visible
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** Cambia la visibilidad: persiste local y sincroniza con el backend. */
    fun setHistorialVisible(visible: Boolean) {
        viewModelScope.launch {
            // Optimista: guardamos local primero para que el switch responda al instante.
            historialPreferenceManager.setVisible(visible)
            try {
                val response = userRepository.setHistorialVisible(visible)
                // El backend confirma el valor real; lo reflejamos por si difiere.
                response.body()?.historialVisible?.let { confirmado ->
                    if (confirmado != visible) historialPreferenceManager.setVisible(confirmado)
                }
            } catch (_: Exception) {
                // Si falla la red, se mantiene el valor local; no bloqueamos la UI.
            }
        }
    }

    /** Elimina la cuenta del usuario y limpia la sesión local al tener éxito. */
    fun eliminarCuenta() {
        if (_state.value.isDeleting) return
        _state.value = SettingsUiState(isDeleting = true)
        viewModelScope.launch {
            try {
                val response = userRepository.eliminarCuenta()
                if (response.isSuccessful) {
                    authRepository.logout() // limpia JWT y userId del DataStore
                    _state.value = SettingsUiState(deleteSuccess = true)
                } else {
                    _state.value = SettingsUiState(errorMessage = "No se pudo eliminar la cuenta (${response.code()})")
                }
            } catch (e: Exception) {
                _state.value = SettingsUiState(errorMessage = "Error de conexión al eliminar la cuenta")
            }
        }
    }

    /** Cierra la sesión limpiando el JWT local. */
    fun cerrarSesion() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = _state.value.copy(loggedOut = true)
        }
    }

    fun consumedError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
