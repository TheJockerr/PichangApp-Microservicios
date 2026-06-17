package cl.duoc.pichangapp.ui.screens.events

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.ui.components.PichangButton
import cl.duoc.pichangapp.ui.components.PichangCard
import cl.duoc.pichangapp.ui.components.PichangSnackbar
import cl.duoc.pichangapp.ui.components.LoadingScreen
import cl.duoc.pichangapp.ui.components.EmptyState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavController,
    eventId: Int,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val registrations by viewModel.registrations.collectAsState()
    val eventDetail by viewModel.eventDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFinishDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        viewModel.loadRegistrations(eventId)
        viewModel.loadEventDetail(eventId)
    }

    // Dialogo de confirmación finalizar evento
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
                            snackbarHostState.showSnackbar(
                                result.exceptionOrNull()?.message ?: "Error al finalizar"
                            )
                        }
                    }
                }) { Text("Finalizar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistencia", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { PichangSnackbar(it) } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading && registrations.isEmpty()) {
                LoadingScreen()
            } else if (registrations.isEmpty()) {
                EmptyState(
                    emoji = "✅",
                    title = "Todos procesados",
                    message = "Todos los participantes han sido procesados."
                )
            } else {
                Text(
                    text = "${registrations.size} participante(s) pendiente(s)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                val finishTime = eventDetail?.let { e ->
                    try {
                        java.time.LocalDateTime.parse(e.eventDate).plusMinutes(5)
                    } catch (ex: Exception) {
                        java.time.LocalDateTime.now()
                    }
                }
                
                val isActionEnabled = finishTime?.let {
                    !java.time.LocalDateTime.now().isBefore(it)
                } ?: true

                if (!isActionEnabled && finishTime != null) {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                    Text(
                        text = "Podrás registrar asistencia a partir de las ${finishTime.format(formatter)}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(registrations, key = { _, item -> item.id }) { index, reg ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300, delayMillis = index * 50)) + slideInHorizontally(tween(300, delayMillis = index * 50))
                        ) {
                            AttendanceRow(
                                reg = reg,
                                eventId = eventId,
                                viewModel = viewModel,
                                snackbarHostState = snackbarHostState,
                                isActionEnabled = isActionEnabled
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            eventDetail?.let { e ->
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
                        text = "Finalizar Evento",
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceRow(
    reg: cl.duoc.pichangapp.data.model.EventRegistrationDto,
    eventId: Int,
    viewModel: EventsViewModel,
    snackbarHostState: SnackbarHostState,
    isActionEnabled: Boolean = true
) {
    var userName by remember { mutableStateOf("Cargando…") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reg.userId) {
        userName = viewModel.getUserName(reg.userId)
    }

    PichangCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Inscrito — pendiente validación",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón ASISTIÓ (verde)
            IconButton(
                onClick = {
                    scope.launch {
                        val result = viewModel.markAttendance(eventId, reg.userId, true)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("✓ $userName validado como asistente")
                        } else {
                            snackbarHostState.showSnackbar(
                                result.exceptionOrNull()?.message ?: "Error al validar"
                            )
                        }
                    }
                },
                enabled = isActionEnabled,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Asistió")
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Botón NO ASISTIÓ (rojo)
            IconButton(
                onClick = {
                    scope.launch {
                        val result = viewModel.markAttendance(eventId, reg.userId, false)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("✗ $userName marcado como ausente")
                        } else {
                            snackbarHostState.showSnackbar(
                                result.exceptionOrNull()?.message ?: "Error al registrar ausencia"
                            )
                        }
                    }
                },
                enabled = isActionEnabled,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Icon(Icons.Filled.Close, contentDescription = "No asistió")
            }
        }
    }
}
