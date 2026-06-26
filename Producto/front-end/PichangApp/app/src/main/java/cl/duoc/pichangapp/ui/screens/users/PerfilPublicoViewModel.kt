package cl.duoc.pichangapp.ui.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.data.model.PerfilPublicoDto
import cl.duoc.pichangapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerfilPublicoUiState(
    val isLoading: Boolean = true,
    val perfil: PerfilPublicoDto? = null,
    val error: String? = null
)

@HiltViewModel
class PerfilPublicoViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PerfilPublicoUiState())
    val state: StateFlow<PerfilPublicoUiState> = _state.asStateFlow()

    private var loaded = false

    /**
     * El backend no expone perfil-público por nombre/apellido (solo por correo, que no
     * tenemos aquí). Reutilizamos la búsqueda y elegimos la coincidencia exacta.
     */
    fun load(nombre: String, apellido: String) {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            _state.value = PerfilPublicoUiState(isLoading = true)
            try {
                val resultados = userRepository.buscarUsuarios(nombre.ifBlank { apellido })
                val match = resultados.firstOrNull {
                    it.nombre?.equals(nombre, ignoreCase = true) == true &&
                        it.apellido?.equals(apellido, ignoreCase = true) == true
                } ?: resultados.firstOrNull { it.nombre?.equals(nombre, ignoreCase = true) == true }
                ?: resultados.firstOrNull()

                if (match != null) {
                    _state.value = PerfilPublicoUiState(isLoading = false, perfil = match)
                } else {
                    _state.value = PerfilPublicoUiState(isLoading = false, error = "No se encontró el perfil")
                }
            } catch (e: Exception) {
                _state.value = PerfilPublicoUiState(isLoading = false, error = "No se pudo cargar el perfil")
            }
        }
    }
}
