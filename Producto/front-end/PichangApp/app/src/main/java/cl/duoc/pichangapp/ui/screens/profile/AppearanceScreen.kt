package cl.duoc.pichangapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import cl.duoc.pichangapp.ui.theme.ThemeMode

@Composable
fun AppearanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val mode by viewModel.themeMode.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PichangTopBar(title = "Apariencia", onNavigateBack = onNavigateBack)

        Text(
            text = "Elige cómo se ve PichangApp. El cambio se aplica al instante.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(8.dp))

        ThemeOption(Icons.Filled.BrightnessAuto, "Sistema", "Sigue el tema del dispositivo",
            selected = mode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }
        ThemeOption(Icons.Filled.LightMode, "Claro", "Fondo claro",
            selected = mode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
        ThemeOption(Icons.Filled.DarkMode, "Oscuro", "Negro estilo Spotify (predeterminado)",
            selected = mode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    SettingsRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        iconTint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
        onClick = onClick,
        trailing = { RadioButton(selected = selected, onClick = onClick) }
    )
}
