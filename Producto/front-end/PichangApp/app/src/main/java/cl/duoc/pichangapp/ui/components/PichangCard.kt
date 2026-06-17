package cl.duoc.pichangapp.ui.components

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Card base de PichangApp: esquinas 20 dp, sin borde, elevación sutil.
 * Color de superficie elevada (estilo card de Spotify, ~#282828 en dark).
 */
@Composable
fun PichangCard(
    modifier      : Modifier = Modifier,
    onClick       : (() -> Unit)? = null,
    shape         : Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    elevation     : Dp = 2.dp,
    content       : @Composable () -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val cardElevation = CardDefaults.cardElevation(defaultElevation = elevation)
    if (onClick != null) {
        Card(
            onClick   = onClick,
            modifier  = modifier,
            shape     = shape,
            elevation = cardElevation,
            colors    = colors
        ) { content() }
    } else {
        Card(
            modifier  = modifier,
            shape     = shape,
            elevation = cardElevation,
            colors    = colors
        ) { content() }
    }
}
