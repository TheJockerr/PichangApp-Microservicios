package cl.duoc.pichangapp.ui.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.core.datastore.TokenDataStore
import cl.duoc.pichangapp.core.util.JwtUtils
import cl.duoc.pichangapp.core.util.Result
import cl.duoc.pichangapp.data.model.PerfilPublicoDto
import cl.duoc.pichangapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuscarUsuariosUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<PerfilPublicoDto> = emptyList(),
    val searched: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BuscarUsuariosViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(BuscarUsuariosUiState())
    val state: StateFlow<BuscarUsuariosUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    // Nombre y apellido del usuario autenticado (para excluirlo de los resultados)
    private var currentUserNombre: String? = null
    private var currentUserApellido: String? = null

    init {
        cargarUsuarioActual()
    }

    private fun cargarUsuarioActual() {
        viewModelScope.launch {
            try {
                val token = tokenDataStore.tokenFlow.firstOrNull()
                val idFromJwt = if (!token.isNullOrEmpty()) JwtUtils.extractUserId(token) else null
                val userId: String = idFromJwt
                    ?: tokenDataStore.userIdFlow.firstOrNull()
                    ?: return@launch

                userRepository.getUserProfile(userId).collect { result ->
                    if (result is Result.Success) {
                        currentUserNombre = result.data.nombre
                        currentUserApellido = result.data.apellido
                    }
                }
            } catch (_: Exception) { }
        }
    }

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
                val raw = userRepository.buscarUsuarios(query.trim())

                val filtrados = raw
                    // Descartar entradas con nombre vacío (Gson pudo haber inyectado null)
                    .filter { !it.nombre.isNullOrBlank() }
                    // Excluir al propio usuario autenticado
                    .filter { perfil ->
                        !(perfil.nombre.equals(currentUserNombre, ignoreCase = true) &&
                          perfil.apellido.equals(currentUserApellido, ignoreCase = true))
                    }
                    // Eliminar duplicados usando correo como clave única
                    .distinctBy { it.correo ?: (it.nombre.orEmpty() + "|" + it.apellido.orEmpty()) }

                _state.value = _state.value.copy(results = filtrados, isLoading = false, searched = true)
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
