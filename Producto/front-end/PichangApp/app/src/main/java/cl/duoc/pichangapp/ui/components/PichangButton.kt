package cl.duoc.pichangapp.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

// Pill totalmente redondeado para CTAs (estilo Spotify).
private val PillShape = RoundedCornerShape(percent = 50)

/**
 * Botón principal pill-shaped, full-width.
 * Variantes: relleno (Primary, default) u `isOutlined = true` (Secondary).
 * El press anima una escala sutil con rebote.
 */
@Composable
fun PichangButton(
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier,
    enabled   : Boolean = true,
    isLoading : Boolean = false,
    text      : String,
    colors    : ButtonColors = ButtonDefaults.buttonColors(),
    isOutlined: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    val buttonModifier = modifier
        .fillMaxWidth()
        .height(54.dp)
        .graphicsLayer(scaleX = scale, scaleY = scale)

    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            interactionSource = interactionSource,
            shape = PillShape
        ) {
            ButtonContent(isLoading, text, MaterialTheme.colorScheme.primary)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            interactionSource = interactionSource,
            shape = PillShape,
            colors = colors
        ) {
            ButtonContent(isLoading, text, MaterialTheme.colorScheme.onPrimary)
        }
    }
}

/** Variante de texto (terciaria): sin relleno, para enlaces / acciones suaves. */
@Composable
fun PichangTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(onClick = onClick, modifier = modifier, enabled = enabled, shape = PillShape) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ButtonContent(isLoading: Boolean, text: String, spinnerColor: androidx.compose.ui.graphics.Color) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp,
            color = spinnerColor
        )
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
