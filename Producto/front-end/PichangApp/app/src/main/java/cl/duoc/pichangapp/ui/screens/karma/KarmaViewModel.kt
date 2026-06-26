package cl.duoc.pichangapp.ui.screens.karma

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.core.datastore.TokenDataStore
import cl.duoc.pichangapp.core.util.JwtUtils
import cl.duoc.pichangapp.core.util.Result
import cl.duoc.pichangapp.data.model.KarmaDto
import cl.duoc.pichangapp.data.model.KarmaHistoryDto
import cl.duoc.pichangapp.data.repository.EventRepository
import cl.duoc.pichangapp.domain.usecase.GetKarmaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PICHANGAPP_DEBUG"
private const val BASE_URL = "https://pichangapp-microservicios-production.up.railway.app"

// Patrón que detecta "evento: 42" o "evento: 42)" al final del reason del backend
private val EVENT_ID_REGEX = Regex("""evento:\s*(\d+)\)?$""")

data class KarmaUiState(
    val isLoading: Boolean = true,
    val karma: KarmaDto? = null,
    val error: String? = null
)

@HiltViewModel
class KarmaViewModel @Inject constructor(
    private val getKarmaUseCase: GetKarmaUseCase,
    private val tokenDataStore: TokenDataStore,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(KarmaUiState())
    val state: StateFlow<KarmaUiState> = _state.asStateFlow()

    // Guard: evita múltiples llamadas al backend si los datos ya se cargaron
    private var dataLoaded = false

    init {
        loadKarma()
    }

    fun refresh() {
        dataLoaded = false
        loadKarma()
    }

    fun loadKarma() {
        // Si ya cargamos datos exitosamente, no volvemos a hacer las llamadas
        if (dataLoaded) {
            Log.d(TAG, "[KarmaViewModel] Datos ya cargados, omitiendo llamada al backend.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // --- DEBUG: Token y userId ---
            val token = tokenDataStore.tokenFlow.firstOrNull()
            Log.d(TAG, "[KarmaViewModel] Token: ${if (token.isNullOrEmpty()) "NULL ⚠️" else "OK (${token.length} chars)"}")

            val userIdFromJwt = if (!token.isNullOrEmpty()) JwtUtils.extractUserId(token) else null
            val userIdFromStore = tokenDataStore.userIdFlow.firstOrNull()
            Log.d(TAG, "[KarmaViewModel] userId JWT='${userIdFromJwt}' DataStore='${userIdFromStore}'")

            val userId = userIdFromJwt ?: userIdFromStore
            Log.d(TAG, "[KarmaViewModel] userId FINAL: ${userId ?: "NULL → abortando ⛔"}")

            if (userId.isNullOrEmpty()) {
                _state.value = _state.value.copy(
                    error = "No se pudo obtener el ID del usuario",
                    isLoading = false
                )
                return@launch
            }

            // --- Load Karma ---
            Log.d(TAG, "[KarmaViewModel] GET $BASE_URL/api/v1/karma/$userId")
            getKarmaUseCase(userId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val score = result.data.puntaje
                        val calculatedCategory = when {
                            score >= 80 -> "Excelente"
                            score >= 60 -> "Bueno"
                            score >= 40 -> "Regular"
                            else -> "Bajo"
                        }
                        Log.d(TAG, "[KarmaViewModel] Karma OK → puntaje=$score categoría='$calculatedCategory'")
                        val updatedKarma = result.data.copy(categoria = calculatedCategory)

                        // Resolver nombres de evento en el historial
                        val historialConNombres = resolverNombresDeEvento(updatedKarma.history)
                        val karmaFinal = updatedKarma.copy(history = historialConNombres)

                        _state.value = _state.value.copy(karma = karmaFinal, isLoading = false)
                        dataLoaded = true
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[KarmaViewModel] Error karma: ${result.message}")
                        _state.value = _state.value.copy(error = result.message, isLoading = false)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    // Extrae IDs de evento únicos de los reasons, los resuelve en paralelo y reemplaza
    // el fragmento "evento: {id}" por el nombre real del evento en cada ítem.
    private suspend fun resolverNombresDeEvento(history: List<KarmaHistoryDto>): List<KarmaHistoryDto> {
        // Recolectar IDs únicos que aparecen en los reasons
        val idsUnicos = history.mapNotNull { item ->
            EVENT_ID_REGEX.find(item.reason)?.groupValues?.get(1)?.toIntOrNull()
        }.toSet()

        if (idsUnicos.isEmpty()) return history

        // Resolver cada ID → nombre (ignorar silenciosamente los que fallen)
        val idANombre = mutableMapOf<Int, String>()
        for (id in idsUnicos) {
            try {
                val evento = eventRepository.getEventById(id)
                idANombre[id] = evento.name
                Log.d(TAG, "[KarmaViewModel] Evento $id → '${evento.name}'")
            } catch (e: Exception) {
                Log.w(TAG, "[KarmaViewModel] No se pudo resolver evento $id: ${e.message}")
            }
        }

        // Reemplazar "evento: {id}" por el nombre en cada reason
        return history.map { item ->
            val match = EVENT_ID_REGEX.find(item.reason)
            val id = match?.groupValues?.get(1)?.toIntOrNull()
            val nombre = id?.let { idANombre[it] }
            if (nombre != null) {
                item.copy(reason = item.reason.replace(match!!.value, "evento: $nombre"))
            } else {
                item
            }
        }
    }
}
