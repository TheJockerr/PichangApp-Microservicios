package cl.duoc.pichangapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.core.datastore.NotificationPreferencesManager
import cl.duoc.pichangapp.core.datastore.NotificationPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Preferencias locales de notificaciones (persistidas en DataStore). */
@HiltViewModel
class NotificationPreferencesViewModel @Inject constructor(
    private val manager: NotificationPreferencesManager
) : ViewModel() {

    val prefs: StateFlow<NotificationPrefs> = manager.prefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationPrefs())

    fun setEvents(v: Boolean)    = viewModelScope.launch { manager.setEvents(v) }
    fun setKarma(v: Boolean)     = viewModelScope.launch { manager.setKarma(v) }
    fun setReminders(v: Boolean) = viewModelScope.launch { manager.setReminders(v) }
}
