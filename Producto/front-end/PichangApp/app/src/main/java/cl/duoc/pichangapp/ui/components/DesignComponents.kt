package cl.duoc.pichangapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.pichangapp.ui.theme.SportBasket
import cl.duoc.pichangapp.ui.theme.SportEsports
import cl.duoc.pichangapp.ui.theme.SportFutbol
import cl.duoc.pichangapp.ui.theme.SportPadel
import cl.duoc.pichangapp.ui.theme.SportTenis
import cl.duoc.pichangapp.ui.theme.SportVolley

// ─── Mapeo de color por deporte (sobre el String real del backend) ────────────
/**
 * Devuelve el color de acento para un deporte dado como texto libre.
 * Tolerante a tildes, mayúsculas y nombres en inglés/español.
 */
@Composable
fun sportColor(sport: String): Color {
    val s = sport.lowercase().trim()
    return when {
        s.contains("futbol") || s.contains("fútbol") || s.contains("football") || s.contains("soccer") -> SportFutbol
        s.contains("padel")  || s.contains("pádel")  -> SportPadel
        s.contains("tenis")  || s.contains("tennis") -> SportTenis
        s.contains("esport") || s.contains("gaming") -> SportEsports
        s.contains("basket") || s.contains("básquet") || s.contains("basquet") -> SportBasket
        s.contains("voley")  || s.contains("vóley")  || s.contains("volley")  || s.contains("voleibol") -> SportVolley
        else -> MaterialTheme.colorScheme.primary
    }
}

/** Emoji decorativo según el deporte. Solo cosmético. */
fun sportEmoji(sport: String): String {
    val s = sport.lowercase().trim()
    return when {
        s.contains("futbol") || s.contains("fútbol") || s.contains("football") || s.contains("soccer") -> "⚽"
        s.contains("padel")  || s.contains("pádel")  -> "🎾"
        s.contains("tenis")  || s.contains("tennis") -> "🎾"
        s.contains("esport") || s.contains("gaming") -> "🎮"
        s.contains("basket") || s.contains("básquet") || s.contains("basquet") -> "🏀"
        s.contains("voley")  || s.contains("vóley")  || s.contains("volley")  || s.contains("voleibol") -> "🏐"
        else -> "🏆"
    }
}

/** Color de acento según la categoría de Karma (excelente/bueno/regular/bajo). */
fun karmaColor(categoria: String): Color = when (categoria.lowercase().trim()) {
    "excelente", "oro"   -> cl.duoc.pichangapp.ui.theme.KarmaExcellent
    "bueno", "plata"     -> cl.duoc.pichangapp.ui.theme.KarmaGood
    "regular", "bronce"  -> cl.duoc.pichangapp.ui.theme.KarmaRegular
    else                 -> cl.duoc.pichangapp.ui.theme.KarmaLow
}

// ─── SportChip ────────────────────────────────────────────────────────────────
@Composable
fun SportChip(
    sport   : String,
    modifier: Modifier = Modifier,
    color   : Color    = sportColor(sport)
) {
    // Unificado sobre CategoryChip para consistencia con el nuevo sistema.
    CategoryChip(
        label        = sport,
        modifier     = modifier,
        color        = color,
        leadingEmoji = sportEmoji(sport)
    )
}

// ─── StatusChip (texto libre: estado del evento, etc.) ────────────────────────
@Composable
fun StatusChip(
    text    : String,
    modifier: Modifier = Modifier,
    color   : Color    = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Text(
            text     = text,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─── Estado de evento en español (color por estado) ───────────────────────────
/** Color del badge según el estado del evento (valores en español del backend). */
fun colorParaStatus(status: String): Color = when (status.uppercase().trim()) {
    "ACTIVO"     -> Color(0xFF2E7D32) // verde
    "FINALIZADO" -> Color(0xFF1565C0) // azul
    "CANCELADO"  -> Color(0xFFC62828) // rojo
    else         -> Color(0xFF546E7A) // gris
}

/** Etiqueta legible del estado (capitaliza; tolera valores en inglés heredados). */
fun statusLabel(status: String): String = when (status.uppercase().trim()) {
    "ACTIVO", "ACTIVE"        -> "Activo"
    "FINALIZADO", "FINISHED"  -> "Finalizado"
    "CANCELADO", "CANCELLED"  -> "Cancelado"
    else -> status.lowercase().replaceFirstChar { it.uppercase() }
}

/** Chip de estado de evento con color e ícono de punto. */
@Composable
fun EventStatusChip(status: String, modifier: Modifier = Modifier) {
    val color = colorParaStatus(status)
    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = color.copy(alpha = 0.14f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text       = statusLabel(status),
                style      = MaterialTheme.typography.labelSmall,
                color      = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── SlotIndicator (cupos: ocupados / total) ──────────────────────────────────
@Composable
fun SlotIndicator(
    filled  : Int,
    total   : Int,
    color   : Color,
    modifier: Modifier = Modifier
) {
    val isFull = total > 0 && filled >= total
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
                imageVector        = Icons.Rounded.Group,
                contentDescription = null,
                tint     = tint,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text       = if (isFull) "Completo" else "$filled/$total",
                style      = MaterialTheme.typography.labelSmall,
                color      = tint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── AvatarCircle ─────────────────────────────────────────────────────────────
/** Avatar circular con la inicial del nombre como fallback. */
@Composable
fun AvatarCircle(
    name    : String,
    modifier: Modifier = Modifier,
    size    : Dp       = 40.dp
) {
    // Delegado al nuevo Avatar (iniciales con color por hash, soporte de foto).
    Avatar(name = name, modifier = modifier, size = size)
}

// ─── ErrorScreen ──────────────────────────────────────────────────────────────
@Composable
fun ErrorScreen(
    message : String,
    modifier: Modifier      = Modifier,
    onRetry : (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector        = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            onRetry?.let {
                OutlinedButton(onClick = it, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reintentar")
                }
            }
        }
    }
}

// ─── PichangTopBar ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PichangTopBar(
    title         : String,
    onNavigateBack: (() -> Unit)? = null,
    actions       : @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            onNavigateBack?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Volver")
                }
            }
        },
        actions = actions,
        colors  = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
