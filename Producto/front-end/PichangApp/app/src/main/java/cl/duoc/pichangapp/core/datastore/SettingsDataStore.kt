package cl.duoc.pichangapp.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore separado para preferencias de UI (tema, notificaciones locales).
 * Independiente de `auth_prefs` para no mezclar sesión con configuración visual.
 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")
