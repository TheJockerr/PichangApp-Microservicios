package cl.duoc.pichangapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Paleta de fondos vibrantes para avatares sin foto (se elige por hash del nombre).
private val avatarColors = listOf(
    Color(0xFF1DB954), Color(0xFF2ECC71), Color(0xFFFF6F00), Color(0xFF9B6BFF),
    Color(0xFF22C7E0), Color(0xFFF65BA8), Color(0xFFFF7A33), Color(0xFF21C063)
)

/** Color de fondo estable derivado del nombre. */
fun avatarColorFor(name: String): Color {
    if (name.isBlank()) return avatarColors.first()
    val idx = (name.hashCode() % avatarColors.size + avatarColors.size) % avatarColors.size
    return avatarColors[idx]
}

/** Iniciales: primera letra del primer y último token del nombre (máx. 2). */
private fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

/**
 * Avatar circular. Muestra la foto ([photoUrl]) si existe; si no, iniciales sobre
 * un fondo con gradiente del color generado por hash del nombre.
 */
@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    photoUrl: String? = null,
    onClick: (() -> Unit)? = null
) {
    val base = modifier
        .size(size)
        .clip(CircleShape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = base
        )
    } else {
        val c = avatarColorFor(name)
        Box(
            contentAlignment = Alignment.Center,
            modifier = base.background(
                Brush.linearGradient(listOf(c, c.copy(alpha = 0.75f)))
            )
        ) {
            Text(
                text = initialsOf(name),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value / 2.6f).sp,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
