package cl.duoc.pichangapp.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.R
import cl.duoc.pichangapp.ui.screens.home.HomeViewModel
import kotlinx.coroutines.launch

/**
 * Contenedor principal de la app autenticada: Drawer lateral + TopAppBar con logo
 * y FAB opcional (solo en Inicio y Eventos). Reemplaza la barra inferior.
 *
 * [homeViewModel] alimenta el header del Drawer (usuario + karma). Por defecto se
 * obtiene con el scope de la ruta actual: en "home" es la MISMA instancia que usa
 * HomeScreen (sin doble carga).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavController,
    showFab: Boolean = false,
    homeViewModel: HomeViewModel = hiltViewModel(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
    content: @Composable (openDrawer: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }
    val state by homeViewModel.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PichangDrawerContent(
                user = state.user,
                karma = state.karma,
                onNavigate = { ruta ->
                    scope.launch { drawerState.close() }
                    navController.navigate(ruta) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    showLogoutDialog = true
                }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "PichangApp",
                            modifier = Modifier.height(32.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menú")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            floatingActionButton = {
                if (showFab) {
                    FloatingActionButton(
                        onClick = { navController.navigate("events/create") },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Crear evento", tint = Color.White)
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) { content(openDrawer) }
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
                    sessionViewModel.logout()
                }) { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
