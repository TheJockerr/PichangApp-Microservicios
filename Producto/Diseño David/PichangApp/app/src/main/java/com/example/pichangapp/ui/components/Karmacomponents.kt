package com.pichangapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pichangapp.domain.model.KarmaScore
import com.pichangapp.domain.model.KarmaStatus
import com.pichangapp.ui.theme.KarmaHigh
import com.pichangapp.ui.theme.KarmaLow
import com.pichangapp.ui.theme.KarmaMid

// ─── Color helpers ────────────────────────────────────────────────────────────
fun karmaColor(score: Int): Color = when {
    score >= KarmaStatus.TRUSTED.minScore -> KarmaHigh
    score >= KarmaStatus.REGULAR.minScore -> KarmaMid
    else                                  -> KarmaLow
}

// ─── KarmaChip: pequeño chip inline ──────────────────────────────────────────
/**
 * Chip compacto que muestra el score de karma.
 * Ideal para listas de eventos junto al nombre del organizador.
 */
@Composable
fun KarmaChip(
    karma    : KarmaScore,
    modifier : Modifier = Modifier,
    showLabel: Boolean  = false
) {
    val color = karmaColor(karma.score)

    Surface(
        shape  = RoundedCornerShape(20.dp),
        color  = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text  = "${karma.score}%",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            if (showLabel) {
                Text(
                    text  = "karma",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ─── KarmaBadge: badge grande con barra de progreso ──────────────────────────
/**
 * Badge prominente para la pantalla de perfil.
 * Incluye score, label de estado y barra de progreso animada.
 */
@Composable
fun KarmaBadge(
    karma   : KarmaScore,
    modifier: Modifier = Modifier
) {
    val color = karmaColor(karma.score)
    var triggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (triggered) karma.percentage else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "karma_progress"
    )

    LaunchedEffect(Unit) { triggered = true }

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment  = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text  = "Tu Karma",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "${karma.score}%",
                fontSize   = 40.sp,
                fontWeight = FontWeight.Bold,
                color      = color
            )
            LinearProgressIndicator(
                progress     = { animatedProgress },
                modifier     = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color        = color,
                trackColor   = color.copy(alpha = 0.15f),
                strokeCap    = StrokeCap.Round
            )
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    text     = karma.status.label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style    = MaterialTheme.typography.labelMedium,
                    color    = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarmaStatItem("Jugados", karma.totalGames.toString())
                KarmaStatItem("Asistidos", karma.attended.toString())
                KarmaStatItem("Faltados", karma.missed.toString(),
                    color = if (karma.missed > 0) KarmaLow else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun KarmaStatItem(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall,  color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}