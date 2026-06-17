package cl.duoc.pichangapp.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.duoc.pichangapp.core.datastore.settingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/** Preferencia de tema seleccionable por el usuario. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Persiste y expone la preferencia de tema del usuario.
 *
 * Se expone como [StateFlow] para que `MainActivity` recomponga el tema al instante.
 * Valor inicial = [ThemeMode.DARK]: el modo oscuro es el predeterminado en la
 * primera instalación (identidad de marca Spotify-like).
 */
@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val themeKey = stringPreferencesKey("theme_mode")

    val themeMode: StateFlow<ThemeMode> = context.settingsDataStore.data
        .map { prefs ->
            when (prefs[themeKey]) {
                ThemeMode.LIGHT.name  -> ThemeMode.LIGHT
                ThemeMode.SYSTEM.name -> ThemeMode.SYSTEM
                else                  -> ThemeMode.DARK   // default en primera instalación
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, ThemeMode.DARK)

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }
}
