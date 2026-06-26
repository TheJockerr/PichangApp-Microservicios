package cl.duoc.pichangapp.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste localmente la preferencia de visibilidad del historial de karma.
 * El backend no expone un GET para el valor actual, así que el espejo local
 * (por defecto visible = true) gobierna lo que muestra el switch.
 */
@Singleton
class HistorialPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val visibleKey = booleanPreferencesKey("historial_visible")

    val visible: Flow<Boolean> = context.settingsDataStore.data.map { it[visibleKey] ?: true }

    suspend fun setVisible(value: Boolean) {
        context.settingsDataStore.edit { it[visibleKey] = value }
    }
}
