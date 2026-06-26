package cl.duoc.pichangapp.ui.screens.events

import android.location.Geocoder
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.data.model.CreateEventRequest
import cl.duoc.pichangapp.ui.components.PichangButton
import cl.duoc.pichangapp.ui.components.PichangSnackbar
import cl.duoc.pichangapp.ui.components.PichangTextField
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isCreating by viewModel.isCreating.collectAsState()

    var step by remember { mutableIntStateOf(1) }

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    
    var sport by remember { mutableStateOf("Fútbol") }
    var expandedSport by remember { mutableStateOf(false) }
    
    var maxPlayers by remember { mutableStateOf("10") }
    var expandedPlayers by remember { mutableStateOf(false) }

    var selectedLocalDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var dateError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )
    val timePickerState = rememberTimePickerState(is24Hour = true)

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf("Ubicación en el mapa") }
    var locationError by remember { mutableStateOf(false) }

    // Búsqueda de dirección (Places)
    var addressQuery by remember { mutableStateOf("") }
    val addressSuggestions by viewModel.addressSuggestions.collectAsState()
    val addressError by viewModel.addressError.collectAsState()
    val searchedLocation by viewModel.searchedLocation.collectAsState()
    val searchedAddress by viewModel.searchedAddress.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-33.4489, -70.6693), 12f)
    }

    // Cuando el usuario toca el mapa, se deshabilita el verticalScroll del padre
    // para que el mapa pueda recibir los gestos de un dedo sin interferencia.
    var isMapBeingTouched by remember { mutableStateOf(false) }

    // Reinicia el estado de búsqueda al abrir la pantalla.
    LaunchedEffect(Unit) { viewModel.resetAddressSearch() }

    // Cuando la búsqueda resuelve una ubicación, mueve la cámara y coloca el marcador.
    LaunchedEffect(searchedLocation) {
        searchedLocation?.let { loc ->
            selectedLocation = loc
            locationName = searchedAddress ?: locationName
            locationError = false
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(loc, 16f))
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Crear Partido - Paso $step/3") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) { PichangSnackbar(it) } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = step / 3f,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(tween(300)) { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally(tween(300)) { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally(tween(300)) { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally(tween(300)) { width -> width } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { targetStep ->
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState(), enabled = !isMapBeingTouched),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (targetStep) {
                        1 -> {
                            Text("Detalles Generales", style = MaterialTheme.typography.titleLarge)
                            PichangTextField(
                                value = name,
                                onValueChange = { name = it; nameError = false },
                                label = "Nombre del partido",
                                isError = nameError,
                                errorMessage = "Debe tener al menos 3 caracteres",
                                leadingIcon = { Icon(Icons.Filled.Title, contentDescription = null) }
                            )

                            ExposedDropdownMenuBox(
                                expanded = expandedSport,
                                onExpandedChange = { expandedSport = it }
                            ) {
                                PichangTextField(
                                    value = sport,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = "Deporte",
                                    leadingIcon = { Icon(Icons.Filled.SportsScore, contentDescription = null) },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedSport,
                                    onDismissRequest = { expandedSport = false }
                                ) {
                                    listOf("Fútbol", "Básquetbol", "Tenis", "Vóleibol", "Otro").forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = { sport = option; expandedSport = false }
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            Text("Fecha y Hora", style = MaterialTheme.typography.titleLarge)
                            
                            val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM, HH:mm", Locale("es", "ES"))
                            val displayDateTime = selectedLocalDateTime?.format(formatter)?.replaceFirstChar { it.uppercase() } ?: "Seleccionar Fecha y Hora"
                            
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = MaterialTheme.shapes.medium,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, 
                                    if (dateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text(displayDateTime, color = if (dateError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            }
                            if (dateError) {
                                Text("Debes seleccionar una fecha y hora futura válida", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }

                            if (showDatePicker) {
                                DatePickerDialog(
                                    onDismissRequest = { showDatePicker = false },
                                    confirmButton = {
                                        TextButton(onClick = { showDatePicker = false; showTimePicker = true }) { Text("Siguiente") }
                                    }
                                ) { DatePicker(state = datePickerState) }
                            }

                            if (showTimePicker) {
                                AlertDialog(
                                    onDismissRequest = { showTimePicker = false },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                                            val date = java.time.Instant.ofEpochMilli(dateMillis).atZone(ZoneId.of("UTC")).toLocalDate()
                                            val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                            val dateTime = LocalDateTime.of(date, time)
                                            
                                            val minAllowed = LocalDateTime.now().plusHours(2)
                                            if (dateTime.isBefore(minAllowed)) {
                                                scope.launch { snackbarHostState.showSnackbar("La hora debe ser al menos 2 horas en el futuro") }
                                            } else {
                                                selectedLocalDateTime = dateTime
                                                dateError = false
                                                showTimePicker = false
                                            }
                                        }) { Text("Confirmar") }
                                    },
                                    text = { TimePicker(state = timePickerState) }
                                )
                            }
                        }
                        3 -> {
                            Text("Ubicación y Jugadores", style = MaterialTheme.typography.titleLarge)
                            
                            ExposedDropdownMenuBox(
                                expanded = expandedPlayers,
                                onExpandedChange = { expandedPlayers = it }
                            ) {
                                PichangTextField(
                                    value = maxPlayers,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = "Máximo de jugadores",
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedPlayers,
                                    onDismissRequest = { expandedPlayers = false }
                                ) {
                                    (1..50).map { it.toString() }.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = { maxPlayers = option; expandedPlayers = false }
                                        )
                                    }
                                }
                            }

                            Text("Busca una dirección o selecciónala en el mapa", fontWeight = FontWeight.Bold, color = if (locationError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)

                            // Campo de búsqueda de dirección
                            OutlinedTextField(
                                value = addressQuery,
                                onValueChange = {
                                    addressQuery = it
                                    viewModel.searchAddress(it)
                                },
                                label = { Text("Buscar dirección") },
                                singleLine = true,
                                isError = addressError != null,
                                supportingText = {
                                    if (addressError != null) {
                                        Text(addressError!!, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.geocodeAddress(addressQuery) }) {
                                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Lista de sugerencias (máximo 4)
                            if (addressSuggestions.isNotEmpty()) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                                        items(addressSuggestions.take(4)) { prediction ->
                                            ListItem(
                                                headlineContent = { Text(prediction.getPrimaryText(null).toString()) },
                                                supportingContent = { Text(prediction.getSecondaryText(null).toString()) },
                                                modifier = Modifier.clickable {
                                                    addressQuery = prediction.getPrimaryText(null).toString()
                                                    viewModel.selectPlace(prediction.placeId)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                isMapBeingTouched = event.changes.any { it.pressed }
                                            }
                                        }
                                    }
                            ) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    onMapClick = { latLng ->
                                        selectedLocation = latLng
                                        locationError = false
                                        scope.launch {
                                            try {
                                                val geocoder = Geocoder(context, Locale.getDefault())
                                                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                                                if (!addresses.isNullOrEmpty()) {
                                                    locationName = addresses[0].getAddressLine(0) ?: "Ubicación en el mapa"
                                                }
                                            } catch (e: Exception) { }
                                        }
                                    }
                                ) {
                                    selectedLocation?.let { Marker(state = MarkerState(position = it)) }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(locationName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (step > 1) {
                    OutlinedButton(onClick = { step-- }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Atrás")
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
                
                if (step < 3) {
                    Button(onClick = {
                        if (step == 1 && name.length < 3) {
                            nameError = true
                        } else if (step == 2 && selectedLocalDateTime == null) {
                            dateError = true
                        } else {
                            step++
                        }
                    }) {
                        Text("Siguiente")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        enabled = !isCreating,   // bloquea doble-tap mientras se crea
                        onClick = {
                            if (selectedLocation == null) {
                                locationError = true
                                scope.launch { snackbarHostState.showSnackbar("Debes seleccionar una ubicación") }
                                return@Button
                            }

                            val eventDateStr = selectedLocalDateTime!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            scope.launch {
                                val result = viewModel.createEvent(
                                    CreateEventRequest(
                                        name = name,
                                        sport = sport,
                                        eventDate = eventDateStr,
                                        latitude = selectedLocation!!.latitude,
                                        longitude = selectedLocation!!.longitude,
                                        locationName = locationName,
                                        maxPlayers = maxPlayers.toIntOrNull() ?: 10
                                    )
                                )
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar("¡Partido creado exitosamente!")
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Error al crear")
                                }
                            }
                        }
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Crear Partido")
                        }
                    }
                }
            }
        }
    }
}
