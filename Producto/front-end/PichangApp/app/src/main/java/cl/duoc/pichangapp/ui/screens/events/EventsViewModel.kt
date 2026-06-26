package cl.duoc.pichangapp.ui.screens.events

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.data.model.CreateEventRequest
import cl.duoc.pichangapp.data.model.EventDto
import cl.duoc.pichangapp.data.repository.EventRepository
import cl.duoc.pichangapp.core.datastore.TokenDataStore
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val tokenDataStore: TokenDataStore,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _events = MutableStateFlow<List<EventDto>>(emptyList())
    val events: StateFlow<List<EventDto>> = _events.asStateFlow()

    val currentUserId = tokenDataStore.userIdFlow

    private val _myEvents = MutableStateFlow<List<EventDto>>(emptyList())
    val myEvents: StateFlow<List<EventDto>> = _myEvents.asStateFlow()

    private val _organizingEvents = MutableStateFlow<List<EventDto>>(emptyList())
    val organizingEvents: StateFlow<List<EventDto>> = _organizingEvents.asStateFlow()

    private val _eventDetail = MutableStateFlow<EventDto?>(null)
    val eventDetail: StateFlow<EventDto?> = _eventDetail.asStateFlow()

    private val _registrations = MutableStateFlow<List<cl.duoc.pichangapp.data.model.EventRegistrationDto>>(emptyList())
    val registrations: StateFlow<List<cl.duoc.pichangapp.data.model.EventRegistrationDto>> = _registrations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Bloqueo anti doble-tap al crear un evento (evita eventos duplicados).
    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastLat: Double? = null
    private var lastLng: Double? = null

    fun refresh() {
        lastLat?.let { lat -> lastLng?.let { lng -> loadEvents(lat, lng) } }
        loadMyEvents()
        loadOrganizingEvents()
    }

    fun loadEvents(lat: Double, lng: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                lastLat = lat
                lastLng = lng
                _events.value = eventRepository.getEvents(lat, lng).filter { it.status == "ACTIVO" }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMyEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _myEvents.value = eventRepository.getMyEvents().filter { it.status == "ACTIVO" }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOrganizingEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _organizingEvents.value = eventRepository.getOrganizingEvents().filter { it.status == "ACTIVO" }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun joinEvent(eventId: Int): Result<Unit> {
        return try {
            val response = eventRepository.joinEvent(eventId)
            if (response.isSuccessful) {
                refresh()
                loadEventDetail(eventId)
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Error al unirse al evento")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error de conexión"))
        }
    }
    
    suspend fun leaveEvent(eventId: Int): Result<Unit> {
        return try {
            val response = eventRepository.leaveEvent(eventId)
            if (response.isSuccessful) {
                refresh()
                loadEventDetail(eventId)
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createEvent(request: CreateEventRequest): Result<Unit> {
        // Si ya hay una creación en curso, ignorar (anti doble-tap).
        if (_isCreating.value) return Result.failure(IllegalStateException("Creación en curso"))
        _isCreating.value = true
        return try {
            eventRepository.createEvent(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isCreating.value = false
        }
    }
    
    suspend fun finishEvent(eventId: Int): Result<Unit> {
        return try {
            val response = eventRepository.finishEvent(eventId)
            if (response.isSuccessful) {
                refresh()
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Error al finalizar evento")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error de conexión"))
        }
    }

    fun loadEventDetail(eventId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _eventDetail.value = eventRepository.getEventById(eventId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRegistrations(eventId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _registrations.value = eventRepository.getRegistrations(eventId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun deleteEvent(eventId: Int): Result<Unit> {
        return try {
            val response = eventRepository.deleteEvent(eventId)
            if (response.isSuccessful) {
                refresh()
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Error al eliminar el evento")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error de conexión"))
        }
    }

    suspend fun markAttendance(eventId: Int, userId: Int, attended: Boolean): Result<Unit> {
        return try {
            val response = eventRepository.markAttendance(eventId, userId, attended)
            if (response.isSuccessful) {
                // Remover el registro de la lista local inmediatamente
                _registrations.value = _registrations.value.filter { it.userId != userId }
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string(), "Error al registrar asistencia")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error de conexión"))
        }
    }

    private fun parseErrorMessage(errorBody: String?, fallback: String): String {
        if (errorBody.isNullOrBlank()) return fallback
        return try {
            val json = JSONObject(errorBody)
            json.optString("message").takeIf { it.isNotEmpty() }
                ?: json.optString("error").takeIf { it.isNotEmpty() }
                ?: json.optString("detail").takeIf { it.isNotEmpty() }
                ?: fallback
        } catch (e: Exception) {
            // Si no es JSON, devolver el texto plano pero evitar mensajes técnicos
            if (errorBody.length < 200) errorBody else fallback
        }
    }

    suspend fun getUserName(userId: Int): String {
        return try {
            val user = eventRepository.getUserById(userId)
            "${user.nombre} ${user.apellido}"
        } catch (e: Exception) {
            "Usuario"
        }
    }

    // ======================= Búsqueda de dirección (Places) =======================

    private val placesClient: PlacesClient? by lazy {
        if (Places.isInitialized()) Places.createClient(appContext) else null
    }
    private var autocompleteSessionToken: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

    private val _addressSuggestions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val addressSuggestions: StateFlow<List<AutocompletePrediction>> = _addressSuggestions.asStateFlow()

    private val _addressError = MutableStateFlow<String?>(null)
    val addressError: StateFlow<String?> = _addressError.asStateFlow()

    // Ubicación resuelta por la búsqueda (sugerencia seleccionada o geocodificación).
    private val _searchedLocation = MutableStateFlow<LatLng?>(null)
    val searchedLocation: StateFlow<LatLng?> = _searchedLocation.asStateFlow()

    private val _searchedAddress = MutableStateFlow<String?>(null)
    val searchedAddress: StateFlow<String?> = _searchedAddress.asStateFlow()

    private var searchJob: Job? = null

    /** Reinicia el estado de búsqueda (al entrar a la pantalla de creación). */
    fun resetAddressSearch() {
        searchJob?.cancel()
        _addressSuggestions.value = emptyList()
        _addressError.value = null
        _searchedLocation.value = null
        _searchedAddress.value = null
        autocompleteSessionToken = AutocompleteSessionToken.newInstance()
    }

    /**
     * Busca sugerencias de dirección con autocompletado.
     * Aplica debounce de 500 ms y solo consulta con 3+ caracteres.
     */
    fun searchAddress(query: String) {
        _addressError.value = null
        searchJob?.cancel()

        if (query.trim().length < 3) {
            _addressSuggestions.value = emptyList()
            return
        }

        val client = placesClient
        if (client == null) {
            _addressError.value = "Búsqueda de direcciones no disponible"
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // debounce
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(autocompleteSessionToken)
                .setCountries("CL")
                .setQuery(query.trim())
                .build()

            client.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    _addressSuggestions.value = response.autocompletePredictions
                    _addressError.value =
                        if (response.autocompletePredictions.isEmpty())
                            "Dirección no encontrada, intenta con otra"
                        else null
                }
                .addOnFailureListener {
                    _addressSuggestions.value = emptyList()
                    _addressError.value = "Dirección no encontrada, intenta con otra"
                }
        }
    }

    /** Obtiene el detalle de un lugar seleccionado y actualiza la ubicación. */
    fun selectPlace(placeId: String) {
        val client = placesClient ?: return
        val fields = listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME)
        val request = FetchPlaceRequest.builder(placeId, fields)
            .setSessionToken(autocompleteSessionToken)
            .build()

        client.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                place.latLng?.let { _searchedLocation.value = it }
                _searchedAddress.value = place.address ?: place.name
                _addressSuggestions.value = emptyList()
                _addressError.value = null
                // Nuevo token de sesión tras completar una búsqueda.
                autocompleteSessionToken = AutocompleteSessionToken.newInstance()
            }
            .addOnFailureListener {
                _addressError.value = "No se pudo obtener la ubicación seleccionada"
            }
    }

    /**
     * Resuelve una dirección escrita libremente mediante geocodificación
     * (cuando el usuario presiona "Buscar" sin elegir una sugerencia).
     */
    fun geocodeAddress(query: String) {
        if (query.trim().isBlank()) return
        _addressError.value = null
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(appContext, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(query.trim(), 1)
                }
                if (!results.isNullOrEmpty()) {
                    val addr = results[0]
                    _searchedLocation.value = LatLng(addr.latitude, addr.longitude)
                    _searchedAddress.value = addr.getAddressLine(0) ?: query.trim()
                    _addressSuggestions.value = emptyList()
                    _addressError.value = null
                } else {
                    _addressError.value = "Dirección no encontrada, intenta con otra"
                }
            } catch (e: Exception) {
                _addressError.value = "Dirección no encontrada, intenta con otra"
            }
        }
    }
}
