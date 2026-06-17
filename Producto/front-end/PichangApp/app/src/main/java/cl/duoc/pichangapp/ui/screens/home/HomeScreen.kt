package cl.duoc.pichangapp.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.data.model.KarmaHistoryDto
import cl.duoc.pichangapp.ui.components.AnimatedCounter
import cl.duoc.pichangapp.ui.components.CategoryChip
import cl.duoc.pichangapp.ui.components.EmptyState
import cl.duoc.pichangapp.ui.components.HorizontalCardRow
import cl.duoc.pichangapp.ui.components.LoadingScreen
import cl.duoc.pichangapp.ui.components.SectionHeader
import cl.duoc.pichangapp.ui.components.Avatar
import cl.duoc.pichangapp.ui.components.karmaColor
import cl.duoc.pichangapp.ui.theme.StatDisplayStyle
import java.time.LocalTime

@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingScreen()
        state.error != null -> EmptyState(
            emoji = "⚠️",
            title = "Algo salió mal",
            message = state.error!!
        )
        else -> HomeContent(state = state, navController = navController)
    }
}

private data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
private fun HomeContent(state: HomeUiState, navController: NavController?) {
    val user = state.user
    val karma = state.karma
    val nombre = user?.nombre?.takeIf { it.isNotBlank() } ?: "Jugador"
    val fullName = listOfNotNull(user?.nombre, user?.apellido)
        .joinToString(" ").ifBlank { "Jugador" }
    val score = karma?.puntaje ?: 0
    val categoria = karma?.categoria ?: "Sin categoría"
    val catColor = karmaColor(categoria)
    val history = karma?.history ?: emptyList()

    val quickActions = listOf(
        QuickAction("Crear\npartido", Icons.Filled.Add, MaterialTheme.colorScheme.primary, "events/create"),
        QuickAction("Buscar\neventos", Icons.Filled.Search, MaterialTheme.colorScheme.tertiary, "events"),
        QuickAction("Mi\nkarma", Icons.Filled.Star, catColor, "karma"),
        QuickAction("Avisos", Icons.Filled.NotificationsNone, Color(0xFF9B6BFF), "notifications")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // ── Header: saludo + avatar ─────────────────────────────────────────
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 8.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = greeting(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Hola, $nombre",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Avatar(
                    name = fullName,
                    size = 52.dp,
                    onClick = { navController?.navigate("profile") }
                )
            }
        }

        // ── Karma hero ──────────────────────────────────────────────────────
        item {
            KarmaHeroCard(
                score = score,
                categoria = categoria,
                color = catColor,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                onClick = { navController?.navigate("karma") }
            )
        }

        // ── Acciones rápidas ────────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Acciones rápidas",
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
            )
        }
        item {
            HorizontalCardRow(items = quickActions, key = { it.title }) { action ->
                QuickActionCard(action) { navController?.navigate(action.route) }
            }
        }

        // ── Descubre eventos ────────────────────────────────────────────────
        item {
            DiscoverEventsCard(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp),
                onClick = { navController?.navigate("events") }
            )
        }

        // ── Actividad reciente ──────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Tu actividad reciente",
                actionLabel = if (history.isNotEmpty()) "Ver todo" else null,
                onActionClick = { navController?.navigate("karma") },
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 8.dp)
            )
        }
        if (history.isEmpty()) {
            item {
                Text(
                    text = "Aún no tienes movimientos de karma. ¡Únete a un evento para empezar!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        } else {
            items(history.take(8)) { item ->
                ActivityRow(item, Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
            }
        }
    }
}

// ─── Karma hero (gradiente tipo carátula de álbum) ──────────────────────────
@Composable
private fun KarmaHeroCard(
    score: Int,
    categoria: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.38f), MaterialTheme.colorScheme.surfaceVariant)
                )
            )
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tu karma",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Ver karma",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                AnimatedCounter(
                    targetValue = score,
                    style = StatDisplayStyle.copy(fontSize = 56.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = " pts",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            CategoryChip(label = categoria, color = color, filled = true)
            Spacer(Modifier.height(16.dp))
            KarmaProgressBar(progress = score / 100f, color = color)
        }
    }
}

@Composable
private fun KarmaProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    val animated by animateFloatAsState(
        targetValue = if (started) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(900),
        label = "karma_progress"
    )
    LaunchedEffect(progress) { started = true }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.White.copy(alpha = 0.15f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction = animated)
                .fillMaxHeight()
                .clip(RoundedCornerShape(percent = 50))
                .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.65f))))
        )
    }
}

// ─── Tile de acción rápida (cuadrado, color sólido, ícono grande) ───────────
@Composable
private fun QuickActionCard(action: QuickAction, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.linearGradient(listOf(action.color, action.color.copy(alpha = 0.78f)))
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .padding(10.dp)
        ) {
            Icon(action.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Text(
            text = action.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

// ─── Card "Descubre eventos" ────────────────────────────────────────────────
@Composable
private fun DiscoverEventsCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.horizontalGradient(listOf(primary.copy(alpha = 0.9f), primary.copy(alpha = 0.55f)))
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Eventos cerca de ti",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Descubre pichangas y únete a la cancha",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
            Text(text = "⚽", fontSize = 44.sp)
        }
    }
}

// ─── Fila de actividad de karma ─────────────────────────────────────────────
@Composable
private fun ActivityRow(item: KarmaHistoryDto, modifier: Modifier = Modifier) {
    val positive = item.amount >= 0
    val tint = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(tint.copy(alpha = 0.16f))
        ) {
            Icon(
                if (positive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.reason.ifBlank { "Movimiento de karma" },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                text = item.createdAt.take(10),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (positive) "+${item.amount}" else "${item.amount}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}

private fun greeting(): String {
    val h = LocalTime.now().hour
    return when {
        h < 12 -> "Buenos días"
        h < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}
