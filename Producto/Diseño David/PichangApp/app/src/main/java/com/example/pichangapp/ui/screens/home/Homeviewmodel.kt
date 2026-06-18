package com.pichangapp.ui.screens.home

import androidx.lifecycle.viewModelScope
import com.pichangapp.core.BaseViewModel
import com.pichangapp.core.NetworkResult
import com.pichangapp.core.UiState
import com.pichangapp.domain.model.Event
import com.pichangapp.domain.model.SportType
import com.pichangapp.domain.usecase.GetNearbyEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── UiState ─────────────────────────────────────────────────────────────────
data class HomeUiState(
    val eventsState    : UiState<List<Event>> = UiState.Idle,
    val selectedSport  : SportType?           = null,
    val radiusKm       : Double               = 10.0,
    val searchQuery    : String               = "",
    val isRefreshing   : Boolean              = false
) {
    val filteredEvents: List<Event>
        get() {
            val events = (eventsState as? UiState.Success)?.data ?: return emptyList()
            return events.filter { event ->
                val matchesSport = selectedSport == null || event.sport == selectedSport
                val matchesQuery = searchQuery.isBlank()
                        || event.title.contains(searchQuery, ignoreCase = true)
                        || event.location.address.contains(searchQuery, ignoreCase = true)
                matchesSport && matchesQuery
            }
        }
}

// ─── ViewModel ───────────────────────────────────────────────────────────────
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNearbyEventsUseCase: GetNearbyEventsUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Coordenadas actuales del usuario (actualizadas por GPS)
    private var userLat = -33.4489   // Santiago por defecto
    private var userLon = -70.6693

    init { loadEvents() }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(eventsState = UiState.Loading) }
            val result = getNearbyEventsUseCase(
                latitude  = userLat,
                longitude = userLon,
                radiusKm  = _uiState.value.radiusKm,
                sport     = _uiState.value.selectedSport
            )
            _uiState.update {
                it.copy(
                    eventsState = when (result) {
                        is NetworkResult.Success -> UiState.Success(result.data)
                        is NetworkResult.Error   -> UiState.Error(result.message)
                        is NetworkResult.Loading -> UiState.Loading
                    },
                    isRefreshing = false
                )
            }
        }
    }

    fun onSportFilterChange(sport: SportType?) {
        _uiState.update { it.copy(selectedSport = sport) }
        loadEvents()
    }

    fun onRadiusChange(radiusKm: Double) {
        _uiState.update { it.copy(radiusKm = radiusKm) }
        loadEvents()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadEvents()
    }

    fun updateUserLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
        loadEvents()
    }
}