package cl.duoc.pichangapp.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Número que anima desde 0 hasta [targetValue] la primera vez que aparece
 * (sensación "Spotify Wrapped"). Reanima si [targetValue] cambia.
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    durationMillis: Int = 900,
    format: (Int) -> String = { it.toString() }
) {
    var started by remember { mutableStateOf(false) }
    val animated by animateIntAsState(
        targetValue = if (started) targetValue else 0,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "animated_counter"
    )
    LaunchedEffect(targetValue) { started = true }

    Text(text = format(animated), style = style, color = color, modifier = modifier)
}
