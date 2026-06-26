package cl.duoc.pichangapp.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.data.model.EventDto
import cl.duoc.pichangapp.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MisEventosUiState(
    val isLoading: Boolean = true,
    val inscrito: List<EventDto> = emptyList(),
    val organizando: List<EventDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MisEventosViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MisEventosUiState())
    val state: StateFlow<MisEventosUiState> = _state.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Se muestran todos los estados (ACTIVO/FINALIZADO/CANCELADO) con su badge.
                val inscrito = eventRepository.getMyEvents()
                val organizando = eventRepository.getOrganizingEvents()
                _state.value = MisEventosUiState(
                    isLoading = false,
                    inscrito = inscrito,
                    organizando = organizando
                )
            } catch (e: Exception) {
                _state.value = MisEventosUiState(isLoading = false, error = "No se pudieron cargar tus eventos")
            }
        }
    }
}
