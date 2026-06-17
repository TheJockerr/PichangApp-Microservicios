package cl.duoc.pichangapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.duoc.pichangapp.ui.components.PichangTopBar
import cl.duoc.pichangapp.ui.components.SettingsRow

@Composable
fun NotificationPreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationPreferencesViewModel = hiltViewModel()
) {
    val prefs by viewModel.prefs.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PichangTopBar(title = "Notificaciones", onNavigateBack = onNavigateBack)

        Text(
            text = "Controla qué avisos quieres recibir.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(8.dp))

        SwitchRow(Icons.Filled.EventAvailable, "Eventos", "Nuevos eventos y cambios de cupo",
            checked = prefs.events) { viewModel.setEvents(it) }
        SwitchRow(Icons.Filled.Star, "Karma", "Cuando tu karma sube o baja",
            checked = prefs.karma) { viewModel.setKarma(it) }
        SwitchRow(Icons.Filled.NotificationsActive, "Recordatorios", "Mensajes y recordatorios de partidos",
            checked = prefs.reminders) { viewModel.setReminders(it) }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = { onCheckedChange(!checked) },
        trailing = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}
