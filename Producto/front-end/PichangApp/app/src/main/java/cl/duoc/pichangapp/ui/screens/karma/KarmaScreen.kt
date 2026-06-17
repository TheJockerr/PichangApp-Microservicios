package cl.duoc.pichangapp.ui.screens.karma

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.data.model.KarmaHistoryDto
import cl.duoc.pichangapp.ui.components.AnimatedCounter
import cl.duoc.pichangapp.ui.components.CategoryChip
import cl.duoc.pichangapp.ui.components.EmptyState
import cl.duoc.pichangapp.ui.components.LoadingScreen
import cl.duoc.pichangapp.ui.components.PichangCard
import cl.duoc.pichangapp.ui.components.SectionHeader
import cl.duoc.pichangapp.ui.components.karmaColor
import cl.duoc.pichangapp.ui.theme.StatDisplayStyle

@Composable
fun KarmaScreen(
    navController: NavController? = null,
    viewModel: KarmaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingScreen()
        state.error != null -> EmptyState(emoji = "⚠️", title = "Error", message = state.error!!)
        else -> {
            val karma = state.karma
            val score = karma?.puntaje ?: 0
            val categoria = karma?.categoria ?: "Sin categoría"
            val color = karmaColor(categoria)
            val history = karma?.history ?: emptyList()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item { KarmaHeader(score, categoria, color) }
                item {
                    SectionHeader(
                        title = "Historial reciente",
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
                    )
                }
                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "Aún no tienes movimientos de karma.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    itemsIndexed(history, key = { _, it -> it.createdAt + it.reason }) { index, item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300, delayMillis = index * 50)) +
                                slideInHorizontally(tween(300, delayMillis = index * 50)) { it / 4 }
                        ) {
                            TimelineCard(item, Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KarmaHeader(score: Int, categoria: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.45f), MaterialTheme.colorScheme.background)
                )
            )
            .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tu nivel de karma",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            AnimatedCounter(
                targetValue = score,
                style = StatDisplayStyle,
                color = MaterialTheme.colorScheme.onBackground,
                durationMillis = 1200
            )
            Spacer(Modifier.height(8.dp))
            CategoryChip(label = categoria, color = color, filled = true)
            Spacer(Modifier.height(20.dp))
            KarmaBar(progress = score / 100f, color = color)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$score / 100",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KarmaBar(progress: Float, color: Color) {
    var started by remember { mutableStateOf(false) }
    val animated by animateFloatAsState(
        targetValue = if (started) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(1000),
        label = "karma_bar"
    )
    LaunchedEffect(progress) { started = true }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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

@Composable
private fun TimelineCard(item: KarmaHistoryDto, modifier: Modifier = Modifier) {
    val positive = item.amount >= 0
    val tint = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    PichangCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.16f))
            ) {
                Icon(
                    if (positive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.reason.ifBlank { "Movimiento de karma" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.createdAt.substringBefore("T"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (positive) "+${item.amount}" else "${item.amount}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = tint,
                fontSize = 20.sp
            )
        }
    }
}
