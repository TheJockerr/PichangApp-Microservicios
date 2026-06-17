package cl.duoc.pichangapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.ui.components.Avatar
import cl.duoc.pichangapp.ui.components.EmptyState
import cl.duoc.pichangapp.ui.components.LoadingScreen
import cl.duoc.pichangapp.ui.components.SettingsRow

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    navController: NavController? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    when {
        state.isLoading -> LoadingScreen()
        state.error != null -> EmptyState(emoji = "⚠️", title = "Error", message = state.error!!)
        else -> {
            val user = state.user
            val fullName = listOfNotNull(user?.nombre, user?.apellido)
                .joinToString(" ").ifBlank { "Jugador" }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                Avatar(name = fullName, size = 110.dp)

                Spacer(Modifier.height(16.dp))
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                user?.correo?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Stats rápidas (visual por ahora; sin endpoint dedicado).
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat("—", "Karma")
                    ProfileStat("—", "Creados")
                    ProfileStat("—", "Asistidos")
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))

                SettingsRow(Icons.Filled.Edit, "Editar perfil",
                    onClick = { navController?.navigate("edit-profile") })
                SettingsRow(Icons.Filled.Lock, "Cambiar contraseña",
                    onClick = { navController?.navigate("change-password") })
                SettingsRow(Icons.Filled.NotificationsNone, "Preferencias de notificaciones",
                    onClick = { navController?.navigate("notification-preferences") })
                SettingsRow(Icons.Filled.DarkMode, "Apariencia",
                    onClick = { navController?.navigate("appearance") })

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))

                SettingsRow(
                    icon = Icons.Filled.Logout,
                    title = "Cerrar sesión",
                    iconTint = MaterialTheme.colorScheme.error,
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showLogoutDialog = true },
                    trailing = null
                )
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Cerrar sesión") },
                    text = { Text("¿Estás seguro que deseas cerrar sesión?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutDialog = false
                            viewModel.logout(onLogoutSuccess = onLogout)
                        }) { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
