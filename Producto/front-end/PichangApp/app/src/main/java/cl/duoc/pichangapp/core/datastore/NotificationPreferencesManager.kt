package cl.duoc.pichangapp.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Preferencias locales de notificaciones (sin backend por ahora). */
data class NotificationPrefs(
    val events: Boolean = true,
    val karma: Boolean = true,
    val reminders: Boolean = true
)

@Singleton
class NotificationPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val eventsKey    = booleanPreferencesKey("notif_events")
    private val karmaKey     = booleanPreferencesKey("notif_karma")
    private val remindersKey = booleanPreferencesKey("notif_reminders")

    val prefs: Flow<NotificationPrefs> = context.settingsDataStore.data.map { p ->
        NotificationPrefs(
            events    = p[eventsKey] ?: true,
            karma     = p[karmaKey] ?: true,
            reminders = p[remindersKey] ?: true
        )
    }

    suspend fun setEvents(v: Boolean)    = set(eventsKey, v)
    suspend fun setKarma(v: Boolean)     = set(karmaKey, v)
    suspend fun setReminders(v: Boolean) = set(remindersKey, v)

    private suspend fun set(key: Preferences.Key<Boolean>, value: Boolean) {
        context.settingsDataStore.edit { it[key] = value }
    }
}
