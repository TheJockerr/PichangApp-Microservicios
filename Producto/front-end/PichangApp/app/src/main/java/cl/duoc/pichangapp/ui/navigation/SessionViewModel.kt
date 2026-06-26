package cl.duoc.pichangapp.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.core.util.SessionManager
import cl.duoc.pichangapp.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel ligero para el cierre de sesión desde el Drawer.
 * Limpia el JWT (LogoutUseCase) y emite el evento de logout; MainActivity
 * escucha ese evento y navega al login.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()          // borra token + userId del DataStore
            sessionManager.logout()  // emite logoutEvent → MainActivity navega al login
        }
    }
}
