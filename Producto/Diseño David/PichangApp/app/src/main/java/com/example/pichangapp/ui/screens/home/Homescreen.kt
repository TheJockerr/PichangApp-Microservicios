package com.pichangapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pichangapp.core.UiState
import com.pichangapp.domain.model.SportType
import com.pichangapp.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick    : (String) -> Unit,
    onNavigateToMap : () -> Unit,
    viewModel       : HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                // ── App bar ───────────────────────────────────────────────
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text       = "PichangApp",
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text  = "Santiago · ${uiState.radiusKm.toInt()} km",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToMap) {
                            Icon(Icons.Rounded.Map, contentDescription = "Ver mapa")
                        }
                        IconButton(onClick = { /* TODO: notifications */ }) {
                            BadgedBox(badge = { Badge { Text("3") } }) {
                                Icon(Icons.Rounded.Notifications, contentDescription = "Notificaciones")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // ── Buscador ──────────────────────────────────────────────
                SearchBar(uiState.searchQuery, viewModel::onSearchQueryChange)

                // ── Filtros de deporte ────────────────────────────────────
                SportFilters(
                    selected = uiState.selectedSport,
                    onSelect = viewModel::onSportFilterChange
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: CreateEvent screen */ },
                shape   = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Crear partido",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = viewModel::refresh,
            modifier     = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState.eventsState) {
                is UiState.Loading, UiState.Idle -> LoadingScreen()

                is UiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = viewModel::loadEvents
                )

                is UiState.Success -> {
                    val events = uiState.filteredEvents
                    if (events.isEmpty()) {
                        EmptyState(
                            icon     = Icons.Rounded.SportsScore,
                            title    = "No hay partidos cerca",
                            subtitle = "Sé el primero en crear uno en tu zona",
                            action   = {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { /* TODO: create event */ },
                                    shape   = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Crear partido")
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text  = "${events.size} partidos disponibles",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            items(
                                items = events,
                                key   = { it.id }
                            ) { event ->
                                EventCard(
                                    event   = event,
                                    onClick = { onEventClick(event.id) }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) } // FAB clearance
                        }
                    }
                }
            }
        }
    }
}

// ─── SearchBar ────────────────────────────────────────────────────────────────
@Composable
private fun SearchBar(
    query    : String,
    onChange : (String) -> Unit,
    modifier : Modifier = Modifier
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onChange,
        placeholder   = { Text("Buscar partidos o canchas...") },
        leadingIcon   = { Icon(Icons.Rounded.Search, null, Modifier.size(20.dp)) },
        trailingIcon  = if (query.isNotEmpty()) {
            { IconButton(onClick = { onChange("") }) {
                Icon(Icons.Rounded.Clear, null, Modifier.size(18.dp))
            }}
        } else null,
        shape    = RoundedCornerShape(12.dp),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

// ─── SportFilters ─────────────────────────────────────────────────────────────
@Composable
private fun SportFilters(
    selected: SportType?,
    onSelect: (SportType?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding       = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier             = modifier
    ) {
        // "Todos"
        item {
            FilterChip(
                selected = selected == null,
                onClick  = { onSelect(null) },
                label    = { Text("Todos") }
            )
        }
        items(SportType.entries) { sport ->
            FilterChip(
                selected = selected == sport,
                onClick  = { onSelect(if (selected == sport) null else sport) },
                label    = { Text("${sport.emoji} ${sport.displayName}") }
            )
        }
    }
}