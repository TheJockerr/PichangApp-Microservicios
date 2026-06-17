package cl.duoc.pichangapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Métrica estilo Spotify Wrapped: número grande (ExtraBold, animado) + etiqueta.
 * Usado en stats de perfil y como cifra protagonista en Karma.
 */
@Composable
fun StatDisplay(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    animated: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (animated) {
            AnimatedCounter(targetValue = value, style = valueStyle, color = valueColor)
        } else {
            Text(text = value.toString(), style = valueStyle, color = valueColor)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
            textAlign = TextAlign.Center
        )
    }
}
