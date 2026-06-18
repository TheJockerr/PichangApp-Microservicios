package com.pichangapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

// ─── PichangTextField ─────────────────────────────────────────────────────────
/**
 * TextField estilizado para formularios de la app.
 */
@Composable
fun PichangTextField(
    value          : String,
    onValueChange  : (String) -> Unit,
    label          : String,
    modifier       : Modifier = Modifier,
    placeholder    : String?  = null,
    leadingIcon    : ImageVector? = null,
    isPassword     : Boolean  = false,
    isError        : Boolean  = false,
    errorMessage   : String?  = null,
    enabled        : Boolean  = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine     : Boolean  = true
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier            = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            label         = { Text(label) },
            placeholder   = placeholder?.let { { Text(it) } },
            leadingIcon   = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(20.dp)) }
            },
            trailingIcon  = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff
                            else Icons.Rounded.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            isError        = isError,
            enabled        = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine     = singleLine,
            shape          = RoundedCornerShape(12.dp),
            modifier       = Modifier.fillMaxWidth()
        )
        if (isError && errorMessage != null) {
            Text(
                text  = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ─── PichangButton ────────────────────────────────────────────────────────────
@Composable
fun PichangButton(
    text      : String,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier,
    enabled   : Boolean  = true,
    isLoading : Boolean  = false,
    icon      : ImageVector? = null
) {
    Button(
        onClick  = onClick,
        enabled  = enabled && !isLoading,
        shape    = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier  = Modifier.size(20.dp),
                color     = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector       = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text       = text,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── LoadingScreen ────────────────────────────────────────────────────────────
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message : String   = "Cargando..."
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── ErrorScreen ─────────────────────────────────────────────────────────────
@Composable
fun ErrorScreen(
    message : String,
    onRetry : (() -> Unit)? = null,
    modifier: Modifier      = Modifier
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
                imageVector       = Icons.Rounded.ErrorOutline,
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
                OutlinedButton(
                    onClick = it,
                    shape   = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reintentar")
                }
            }
        }
    }
}

// ─── EmptyState ───────────────────────────────────────────────────────────────
@Composable
fun EmptyState(
    icon    : ImageVector,
    title   : String,
    subtitle: String,
    action  : (@Composable () -> Unit)? = null,
    modifier: Modifier                  = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector       = icon,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            action?.invoke()
        }
    }
}

// ─── PichangTopBar ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PichangTopBar(
    title          : String,
    onNavigateBack : (() -> Unit)? = null,
    actions        : @Composable RowScope.() -> Unit = {}
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
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}