package cl.duoc.pichangapp.ui.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.data.model.PerfilPublicoDto
import cl.duoc.pichangapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuscarUsuariosUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<PerfilPublicoDto> = emptyList(),
    val searched: Boolean = false,   // true tras la primera búsqueda (para el estado vacío)
    val error: String? = null
)

@HiltViewModel
class BuscarUsuariosViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BuscarUsuariosUiState())
    val state: StateFlow<BuscarUsuariosUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query)
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _state.value = _state.value.copy(results = emptyList(), searched = false, isLoading = false, error = null)
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // debounce
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val results = userRepository.buscarUsuarios(query.trim())
                _state.value = _state.value.copy(results = results, isLoading = false, searched = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    searched = true,
                    results = emptyList(),
                    error = "No se pudo completar la búsqueda"
                )
            }
        }
    }
}
