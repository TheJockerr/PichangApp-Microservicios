package cl.duoc.pichangapp.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.data.model.EventDto
import cl.duoc.pichangapp.ui.components.EmptyState
import cl.duoc.pichangapp.ui.components.LoadingScreen

@Composable
fun MisEventosScreen(
    navController: NavController,
    viewModel: MisEventosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Mis eventos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Me inscribí", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Organizo", fontWeight = FontWeight.Bold) }
            )
        }

        when {
            state.isLoading -> LoadingScreen()
            state.error != null -> EmptyState(emoji = "⚠️", title = "Error", message = state.error!!)
            else -> {
                val lista = if (selectedTab == 0) state.inscrito else state.organizando
                val (emoji, titulo, mensaje) = if (selectedTab == 0)
                    Triple("🎟️", "Sin inscripciones", "Aún no te has inscrito en ningún evento.")
                else
                    Triple("📋", "No organizas eventos", "Crea tu primera pichanga desde el botón +.")

                MisEventosLista(lista, emoji, titulo, mensaje, navController)
            }
        }
    }
}

@Composable
private fun MisEventosLista(
    eventos: List<EventDto>,
    emoji: String,
    tituloVacio: String,
    mensajeVacio: String,
    navController: NavController
) {
    if (eventos.isEmpty()) {
        EmptyState(emoji = emoji, title = tituloVacio, message = mensajeVacio)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(eventos, key = { _, item -> item.id }) { _, event ->
                EventCard(event = event, onClick = { navController.navigate("events/${event.id}") })
            }
        }
    }
}
