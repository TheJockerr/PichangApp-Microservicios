package cl.duoc.pichangapp.ui.screens.events

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.ui.components.PichangButton
import cl.duoc.pichangapp.ui.components.PichangSnackbar
import cl.duoc.pichangapp.ui.components.LoadingScreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: Int,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val event by viewModel.eventDetail.collectAsState()
    val myEvents by viewModel.myEvents.collectAsState()
    val userIdStr by viewModel.currentUserId.collectAsState(initial = null)
    val userId = userIdStr?.toIntOrNull()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Pulse animation for the join button
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(eventId) {
        viewModel.loadEventDetail(eventId)
        viewModel.loadMyEvents()
    }

    // Dialogo de cancelar participación
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar participación") },
            text = { Text("¿Estás seguro que deseas cancelar tu participación? Si el partido comienza en menos de 2 horas no podrás hacerlo.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    scope.launch {
                        val result = viewModel.leaveEvent(eventId)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Participación cancelada correctamente")
                        } else {
                            snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Error al cancelar")
                        }
                    }
                }) { Text("Confirmar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Volver") }
            }
        )
    }

    // Dialogo de finalizar evento
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finalizar Evento") },
            text = { Text("¿Estás seguro? Los participantes que no fueron validados como asistentes recibirán una penalización de karma.") },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    scope.launch {
                        val result = viewModel.finishEvent(eventId)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Evento finalizado correctamente")
                            delay(1500)
                            navController.navigate("events") {
                                popUpTo("events") { inclusive = false }
                            }
                        } else {
                            snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Error al finalizar")
                        }
                    }
                }) { Text("Finalizar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Dialogo de eliminar evento
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar evento") },
            text = { Text("¿Estás seguro? Todos los participantes recibirán sus puntos de karma y una notificación.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        val result = viewModel.deleteEvent(eventId)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Evento eliminado. Los participantes han sido compensados")
                            delay(1500)
                            navController.navigate("events") {
                                popUpTo("events") { inclusive = false }
                            }
                        } else {
                            snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Error al eliminar")
                        }
                    }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { PichangSnackbar(it) } }
    ) { paddingValues ->
        if (event == null) {
            LoadingScreen()
        } else {
            val e = event!!
            val isOrganizer = e.organizerId == userId
            val isRegistered = myEvents.any { it.id == eventId }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header with Map + Gradient overlay
                Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                    val latLng = LatLng(e.latitude, e.longitude)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(latLng, 14f)
                    }
                    // scrollGesturesEnabled=true permite mover el mapa con un dedo.
                    // La Column exterior tiene verticalScroll, pero GoogleMap consume
                    // los eventos de toque internamente cuando está activo.
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            scrollGesturesEnabled = true
                        ),
                        properties = MapProperties(),
                        onMapLongClick = {
                            // Long press → abrir Google Maps con la ubicación del evento
                            val uri = Uri.parse(
                                "geo:${e.latitude},${e.longitude}?q=${e.latitude},${e.longitude}(${Uri.encode(e.name)})"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                // Fallback: abrir en cualquier app de mapas disponible
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }
                    ) {
                        Marker(state = MarkerState(position = latLng), title = e.name)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                    startY = 200f
                                )
                            )
                    )
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        e.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Chips de info
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EventChip(icon = Icons.Filled.SportsScore, text = e.sport, color = MaterialTheme.colorScheme.tertiary)
                        if (e.distanceKm != null) {
                            EventChip(icon = Icons.Filled.LocationOn, text = String.format("%.1f km", e.distanceKm), color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(e.eventDate, style = MaterialTheme.typography.bodyLarge)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(e.locationName, style = MaterialTheme.typography.bodyLarge)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Botón "Cómo llegar" → abre Google Maps con la ubicación ──
                    FilledTonalButton(
                        onClick = {
                            val uri = Uri.parse(
                                "geo:${e.latitude},${e.longitude}?q=${e.latitude},${e.longitude}(${Uri.encode(e.name)})"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cómo llegar")
                    }

                    // ── Organizado por (nombre del creador, tappable hacia su perfil) ──
                    e.nombreCreador?.takeIf { it.isNotBlank() }?.let { creador ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = if (isOrganizer) Modifier else Modifier.clickable {
                                val partes = creador.trim().split(" ")
                                val nombre = android.net.Uri.encode(partes.firstOrNull().orEmpty())
                                val apellido = android.net.Uri.encode(partes.drop(1).joinToString(" "))
                                navController.navigate("perfil-publico?nombre=$nombre&apellido=$apellido")
                            }
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Organizado por: $creador",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isOrganizer) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Jugadores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("${e.currentPlayers} / ${e.maxPlayers}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = if (e.maxPlayers > 0) e.currentPlayers.toFloat() / e.maxPlayers else 0f,
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // ========= ACCIONES =========
                    if (isOrganizer) {
                        PichangButton(
                            onClick = { navController.navigate("events/${e.id}/attendance") },
                            text = "Ver inscritos",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val finishTime = try {
                            java.time.LocalDateTime.parse(e.eventDate).plusMinutes(5)
                        } catch (ex: Exception) {
                            java.time.LocalDateTime.now() // Fallback if parsing fails
                        }
                        
                        if (java.time.LocalDateTime.now().isBefore(finishTime)) {
                            val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                            Text(
                                text = "Podrás finalizar el evento a partir de las ${finishTime.format(formatter)}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            PichangButton(
                                onClick = { showFinishDialog = true },
                                text = "Finalizar evento",
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        PichangButton(
                            onClick = { showDeleteDialog = true },
                            text = "Eliminar evento",
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        )
                    } else {
                        if (isRegistered) {
                            // Cancelar participación
                            PichangButton(
                                onClick = { showCancelDialog = true },
                                text = "Cancelar participación",
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            )

                        } else {
                            val isFull = e.currentPlayers >= e.maxPlayers
                            PichangButton(
                                onClick = {
                                    scope.launch {
                                        val result = viewModel.joinEvent(e.id)
                                        if (result.isSuccess) {
                                            snackbarHostState.showSnackbar("¡Te uniste al evento!")
                                            delay(1500)
                                            navController.navigate("events") {
                                                popUpTo("events") { inclusive = false }
                                            }
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                result.exceptionOrNull()?.message ?: "Error al unirse al evento"
                                            )
                                        }
                                    }
                                },
                                text = if (isFull) "Cupos llenos" else "Unirse al partido",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(if (!isFull) pulseScale else 1f),
                                enabled = !isFull
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}
