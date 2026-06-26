package cl.duoc.pichangapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.ui.components.PichangSnackbar
import cl.duoc.pichangapp.ui.components.PichangTopBar
import cl.duoc.pichangapp.ui.components.SettingsRow
import cl.duoc.pichangapp.ui.navigation.Screen

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val historialVisible by viewModel.historialVisible.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Navegación tras eliminar cuenta o cerrar sesión.
    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            navController.navigate(Screen.Login.route + "?mensaje=Cuenta eliminada") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumedError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { PichangSnackbar(it) } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            PichangTopBar(title = "Configuración", onNavigateBack = { navController.popBackStack() })

            Spacer(Modifier.height(8.dp))

            // ── Cuenta ──────────────────────────────────────────────────────
            SettingsRow(Icons.Filled.Edit, "Editar perfil",
                onClick = { navController.navigate("edit-profile") })
            SettingsRow(Icons.Filled.Lock, "Cambiar contraseña",
                onClick = { navController.navigate("change-password") })

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

            // ── Privacidad: visibilidad del historial (BLOQUE 11) ───────────
            SettingsRow(
                icon = Icons.Filled.History,
                title = "Historial visible",
                subtitle = "Permite que otros vean tu historial de karma",
                onClick = { viewModel.setHistorialVisible(!historialVisible) },
                trailing = {
                    Switch(
                        checked = historialVisible,
                        onCheckedChange = { viewModel.setHistorialVisible(it) }
                    )
                }
            )

            // ── Preferencias ────────────────────────────────────────────────
            SettingsRow(Icons.Filled.NotificationsNone, "Preferencias de notificaciones",
                onClick = { navController.navigate("notification-preferences") })
            SettingsRow(Icons.Filled.DarkMode, "Apariencia",
                onClick = { navController.navigate("appearance") })

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

            // ── Cerrar sesión ───────────────────────────────────────────────
            SettingsRow(
                icon = Icons.Filled.Logout,
                title = "Cerrar sesión",
                onClick = { showLogoutDialog = true },
                trailing = null
            )

            // ── Eliminar cuenta (BLOQUE 8) ──────────────────────────────────
            SettingsRow(
                icon = Icons.Filled.DeleteForever,
                title = "Eliminar cuenta",
                iconTint = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showDeleteDialog = true },
                trailing = null
            )

            Spacer(Modifier.height(28.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.cerrarSesion()
                }) { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!state.isDeleting) showDeleteDialog = false },
            title = { Text("Eliminar cuenta") },
            text = {
                Text(
                    "¿Estás seguro que deseas eliminar tu cuenta?\n\n" +
                        "Esta acción es irreversible. Todos tus eventos activos serán cancelados " +
                        "y los participantes serán compensados."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !state.isDeleting,
                    onClick = {
                        showDeleteDialog = false
                        viewModel.eliminarCuenta()
                    }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !state.isDeleting) {
                    Text("Cancelar")
                }
            }
        )
    }
}
