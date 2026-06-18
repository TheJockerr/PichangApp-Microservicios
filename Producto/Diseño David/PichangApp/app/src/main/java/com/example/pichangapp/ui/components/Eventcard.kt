package com.pichangapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pichangapp.domain.model.Event
import com.pichangapp.domain.model.SportType
import com.pichangapp.ui.theme.*
import java.time.format.DateTimeFormatter

// ─── Sport color mapping ──────────────────────────────────────────────────────
fun sportColor(sport: SportType): Color = when (sport) {
    SportType.FUTBOL  -> SportFutbol
    SportType.PADEL   -> SportPadel
    SportType.TENIS   -> SportTenis
    SportType.ESPORTS -> SportEsports
    SportType.BASKET  -> Color(0xFFEA580C)
    SportType.VOLLEY  -> Color(0xFFDB2777)
}

// ─── EventCard ────────────────────────────────────────────────────────────────
/**
 * Tarjeta principal de evento para LazyColumn en HomeScreen.
 * Diseño de red social: muestra la info clave de un vistazo.
 */
@Composable
fun EventCard(
    event    : Event,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier
) {
    val sportColor = sportColor(event.sport)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM")

    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // ── Franja de color del deporte ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(sportColor)
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Header: deporte + hora + distancia ────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SportChip(sport = event.sport, color = sportColor)
                        LevelChip(level = event.level.displayName)
                    }
                    event.distanceKm?.let { km ->
                        Text(
                            text  = "%.1f km".format(km),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Título + descripción ──────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text     = event.title,
                        style    = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    event.description?.let { desc ->
                        Text(
                            text     = desc,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )

                // ── Footer: fecha, ubicación, cupos ───────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Fecha + hora
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector       = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint   = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text  = "${event.dateTime.format(dateFormatter)} · ${event.dateTime.format(timeFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Cupos
                    SlotIndicator(
                        filled = event.filledSlots,
                        total  = event.totalSlots,
                        isFull = event.isFull,
                        color  = sportColor
                    )
                }

                // ── Organizador + karma ───────────────────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AvatarCircle(
                        name     = event.organizer.name,
                        imageUrl = event.organizer.avatarUrl,
                        size     = 22.dp
                    )
                    Text(
                        text  = event.organizer.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    KarmaChip(karma = event.organizer.karma)
                    if (!event.isFree) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            text  = "$${event.price.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ─── SportChip ────────────────────────────────────────────────────────────────
@Composable
fun SportChip(
    sport   : SportType,
    color   : Color     = sportColor(sport),
    modifier: Modifier  = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Text(
            text     = "${sport.emoji} ${sport.displayName}",
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─── LevelChip ───────────────────────────────────────────────────────────────
@Composable
fun LevelChip(
    level   : String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Text(
            text     = level,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─── SlotIndicator ───────────────────────────────────────────────────────────
@Composable
fun SlotIndicator(
    filled  : Int,
    total   : Int,
    isFull  : Boolean,
    color   : Color,
    modifier: Modifier = Modifier
) {
    val tint = if (isFull) MaterialTheme.colorScheme.error else color

    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = tint.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector       = Icons.Rounded.Group,
                contentDescription = null,
                tint     = tint,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text  = if (isFull) "Completo" else "$filled/$total",
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── AvatarCircle ─────────────────────────────────────────────────────────────
/**
 * Avatar circular con inicial del nombre como fallback.
 */
@Composable
fun AvatarCircle(
    name    : String,
    imageUrl: String?  = null,
    size    : androidx.compose.ui.unit.Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    // Aquí puedes integrar Coil para carga de imágenes remotas:
    // AsyncImage(model = imageUrl, ...) cuando imageUrl != null
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text  = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}