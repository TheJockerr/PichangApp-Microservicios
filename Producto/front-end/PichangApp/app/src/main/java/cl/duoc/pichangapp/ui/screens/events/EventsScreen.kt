package cl.duoc.pichangapp.ui.screens.events

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.ui.components.PichangCard
import cl.duoc.pichangapp.ui.components.EmptyState
import cl.duoc.pichangapp.ui.components.LoadingScreen
import cl.duoc.pichangapp.ui.components.SportChip
import cl.duoc.pichangapp.ui.components.EventStatusChip
import cl.duoc.pichangapp.ui.components.SlotIndicator
import cl.duoc.pichangapp.ui.components.sportColor
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    navController: NavController,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    val myEvents by viewModel.myEvents.collectAsState()
    val organizingEvents by viewModel.organizingEvents.collectAsState()
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var selectedTab by remember { mutableStateOf(0) }
    var userLat by remember { mutableStateOf<Double?>(null) }
    var userLng by remember { mutableStateOf<Double?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                              permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        userLat = location.latitude
                        userLng = location.longitude
                        viewModel.loadEvents(location.latitude, location.longitude)
                    } else {
                        viewModel.loadEvents(-33.4489, -70.6693) // Santiago default
                    }
                }
            } catch (e: SecurityException) { }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Eventos", fontWeight = FontWeight.Bold) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Buscar", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mis Eventos", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                if (!hasLocationPermission) {
                    EmptyState(emoji = "📍", title = "Permiso requerido", message = "Se requiere ubicación para buscar eventos cercanos.")
                } else if (userLat != null && userLng != null) {
                    BuscarEventosTab(events, userLat!!, userLng!!, navController)
                } else {
                    LoadingScreen()
                }
            } else {
                MisEventosTab(myEvents, organizingEvents, navController)
            }
        }
    }
}

@Composable
fun BuscarEventosTab(events: List<cl.duoc.pichangapp.data.model.EventDto>, lat: Double, lng: Double, navController: NavController) {
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(com.google.android.gms.maps.model.LatLng(lat, lng), 12f)
    }
    
    var mapVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { mapVisible = true }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = mapVisible,
            enter = fadeIn(tween(1000)),
            modifier = Modifier.weight(0.8f).fillMaxWidth()
        ) {
            Box {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        tiltGesturesEnabled = true,
                        rotationGesturesEnabled = true
                    )
                ) {
                    events.forEach { event ->
                        Marker(
                            state = MarkerState(position = com.google.android.gms.maps.model.LatLng(event.latitude, event.longitude)),
                            title = event.name,
                            snippet = event.sport
                        )
                    }
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1.2f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(events.sortedBy { it.distanceKm ?: 0.0 }, key = { _, item -> item.id }) { index, event ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300, delayMillis = index * 50)) + slideInHorizontally(tween(300, delayMillis = index * 50))
                ) {
                    EventCard(event = event, onClick = { navController.navigate("events/${event.id}") })
                }
            }
        }
    }
}

@Composable
fun MisEventosTab(myEvents: List<cl.duoc.pichangapp.data.model.EventDto>, organizingEvents: List<cl.duoc.pichangapp.data.model.EventDto>, navController: NavController) {
    var subTab by remember { mutableStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Inscrito") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Organizando") })
        }
        
        val list = if (subTab == 0) myEvents else organizingEvents
        
        if (list.isEmpty()) {
            EmptyState(emoji = "🏟️", title = "Nada por aquí", message = "No tienes eventos en esta categoría.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(list, key = { _, item -> item.id }) { index, event ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300, delayMillis = index * 50)) + slideInHorizontally(tween(300, delayMillis = index * 50))
                    ) {
                        EventCard(event = event, onClick = { navController.navigate("events/${event.id}") })
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: cl.duoc.pichangapp.data.model.EventDto, onClick: () -> Unit) {
    val accent = sportColor(event.sport)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // ── Franja de color del deporte ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accent)
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Header: deporte + distancia ───────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SportChip(sport = event.sport, color = accent)
                    event.distanceKm?.let { km ->
                        Text(
                            text = String.format("%.1f km", km),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Título ────────────────────────────────────────────────
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                // ── Fecha ─────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = event.eventDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── Ubicación ─────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = event.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )

                // ── Footer: estado + cupos ────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EventStatusChip(status = event.status)
                    SlotIndicator(
                        filled = event.currentPlayers,
                        total = event.maxPlayers,
                        color = accent
                    )
                }
            }
        }
    }
}
