package cl.duoc.pichangapp.ui.screens.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.core.datastore.TokenDataStore
import cl.duoc.pichangapp.core.util.JwtUtils
import cl.duoc.pichangapp.core.util.Result
import cl.duoc.pichangapp.data.model.NotificationDto
import cl.duoc.pichangapp.domain.usecase.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PICHANGAPP_DEBUG"

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    // Guard: evita múltiples llamadas al backend si los datos ya se cargaron
    private var dataLoaded = false

    init {
        loadNotifications()
    }

    fun refresh() {
        dataLoaded = false
        loadNotifications()
    }

    fun loadNotifications() {
        if (dataLoaded) {
            Log.d(TAG, "[NotificationsVM] Datos ya cargados, omitiendo llamada.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Extraer userId del JWT (igual que KarmaViewModel)
            val token = tokenDataStore.tokenFlow.firstOrNull()
            val userIdFromJwt = if (!token.isNullOrEmpty()) JwtUtils.extractUserId(token) else null
            val userIdFromStore = tokenDataStore.userIdFlow.firstOrNull()
            val userId = userIdFromJwt ?: userIdFromStore

            Log.d(TAG, "[NotificationsVM] userId JWT='$userIdFromJwt' DataStore='$userIdFromStore' FINAL='$userId'")

            if (userId.isNullOrEmpty()) {
                _state.value = _state.value.copy(
                    error = "No se pudo obtener el ID del usuario",
                    isLoading = false
                )
                return@launch
            }

            getNotificationsUseCase(userId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        Log.d(TAG, "[NotificationsVM] OK → ${result.data.size} notificaciones")
                        _state.value = _state.value.copy(
                            notifications = result.data,
                            isLoading = false,
                            error = null
                        )
                        dataLoaded = true
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[NotificationsVM] Error: ${result.message}")
                        _state.value = _state.value.copy(error = result.message, isLoading = false)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }
}
