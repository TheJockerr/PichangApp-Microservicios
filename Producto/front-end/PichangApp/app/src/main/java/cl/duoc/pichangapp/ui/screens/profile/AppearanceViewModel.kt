package cl.duoc.pichangapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.pichangapp.ui.theme.ThemeManager
import cl.duoc.pichangapp.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Conecta la pantalla de Apariencia con la preferencia de tema persistida. */
@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themeManager.themeMode

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themeManager.setThemeMode(mode) }
    }
}
