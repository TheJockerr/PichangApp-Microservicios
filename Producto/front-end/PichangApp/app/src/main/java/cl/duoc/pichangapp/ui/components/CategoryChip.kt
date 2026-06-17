package cl.duoc.pichangapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Chip de categoría / deporte: color de acento + ícono o emoji opcional.
 * `filled = true` lo pinta sólido (seleccionado); por defecto es suave (tinte 15%).
 */
@Composable
fun CategoryChip(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    leadingEmoji: String? = null,
    leadingIcon: ImageVector? = null,
    filled: Boolean = false
) {
    val bg = if (filled) color else color.copy(alpha = 0.15f)
    val fg = if (filled) MaterialTheme.colorScheme.onPrimary else color

    Surface(shape = RoundedCornerShape(percent = 50), color = bg, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            leadingEmoji?.let { Text(text = it, style = MaterialTheme.typography.labelMedium) }
            leadingIcon?.let {
                Icon(it, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
