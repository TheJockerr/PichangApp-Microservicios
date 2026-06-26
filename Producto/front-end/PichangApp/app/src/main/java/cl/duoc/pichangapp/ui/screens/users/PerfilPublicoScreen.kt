package cl.duoc.pichangapp.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.ui.components.Avatar
import cl.duoc.pichangapp.ui.components.CategoryChip
import cl.duoc.pichangapp.ui.components.EmptyState
import cl.duoc.pichangapp.ui.components.LoadingScreen
import cl.duoc.pichangapp.ui.components.PichangTopBar
import cl.duoc.pichangapp.ui.components.karmaColor

@Composable
fun PerfilPublicoScreen(
    navController: NavController,
    nombre: String,
    apellido: String,
    viewModel: PerfilPublicoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(nombre, apellido) { viewModel.load(nombre, apellido) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PichangTopBar(title = "Perfil", onNavigateBack = { navController.popBackStack() })

        when {
            state.isLoading -> LoadingScreen()
            state.error != null || state.perfil == null ->
                EmptyState(emoji = "🤷", title = "Perfil no disponible", message = state.error ?: "No se encontró el perfil")
            else -> {
                val perfil = state.perfil!!
                val fullName = "${perfil.nombre} ${perfil.apellido}".trim()
                val color = karmaColor(perfil.categoriaKarma)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))
                    Avatar(name = fullName, size = 120.dp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))

                    // ── Tarjeta de karma ──────────────────────────────────────
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Column(
                            Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${perfil.karmaScore}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = color
                            )
                            Text(
                                text = "puntos de karma",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            CategoryChip(label = perfil.categoriaKarma, color = color, filled = true)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Historial (respeta la visibilidad del usuario) ────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (perfil.historialVisible) Icons.Filled.Schedule else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Historial de eventos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (!perfil.historialVisible)
                            "Este usuario mantiene su historial privado."
                        else
                            "El historial detallado de este jugador aún no está disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}
