package cl.duoc.pichangapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PichangSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    val message = snackbarData.visuals.message
    val isError = message.contains("Error", ignoreCase = true) || message.startsWith("✗") || message.contains("No puedes", ignoreCase = true)
    val isSuccess = message.startsWith("✓") || message.contains("¡") || message.contains("correctamente") || message.contains("exitosamente") || message.contains("cancelada")

    val containerColor = when {
        isError -> MaterialTheme.colorScheme.errorContainer
        isSuccess -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isError -> MaterialTheme.colorScheme.onErrorContainer
        isSuccess -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val icon = when {
        isError -> Icons.Filled.Error
        isSuccess -> Icons.Filled.CheckCircle
        else -> Icons.Filled.Info
    }

    Snackbar(
        modifier = modifier.padding(12.dp),
        containerColor = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}
