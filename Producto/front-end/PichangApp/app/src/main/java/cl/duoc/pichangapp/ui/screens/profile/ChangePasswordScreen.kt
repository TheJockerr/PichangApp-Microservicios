package cl.duoc.pichangapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.pichangapp.ui.components.PichangButton
import cl.duoc.pichangapp.ui.components.PichangTextField
import cl.duoc.pichangapp.ui.components.PichangTopBar

@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<String?>(null) }

    val mismatch = confirmar.isNotEmpty() && nueva != confirmar
    val tooShort = nueva.isNotEmpty() && nueva.length < 6

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PichangTopBar(title = "Cambiar contraseña", onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            val transform: VisualTransformation =
                if (visible) VisualTransformation.None else PasswordVisualTransformation()
            val eye: @Composable () -> Unit = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (visible) "Ocultar" else "Mostrar"
                    )
                }
            }

            PichangTextField(
                value = current, onValueChange = { current = it },
                label = "Contraseña actual",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = transform, trailingIcon = eye
            )
            Spacer(Modifier.height(12.dp))
            PichangTextField(
                value = nueva, onValueChange = { nueva = it },
                label = "Nueva contraseña",
                modifier = Modifier.fillMaxWidth(),
                isError = tooShort,
                errorMessage = if (tooShort) "Mínimo 6 caracteres" else null,
                visualTransformation = transform, trailingIcon = eye
            )
            Spacer(Modifier.height(12.dp))
            PichangTextField(
                value = confirmar, onValueChange = { confirmar = it },
                label = "Confirmar nueva contraseña",
                modifier = Modifier.fillMaxWidth(),
                isError = mismatch,
                errorMessage = if (mismatch) "Las contraseñas no coinciden" else null,
                visualTransformation = transform, trailingIcon = eye
            )

            Spacer(Modifier.height(24.dp))

            PichangButton(
                onClick = {
                    // TODO: conectar a endpoint de cambio de contraseña cuando esté disponible.
                    info = when {
                        current.isBlank() || nueva.isBlank() || confirmar.isBlank() ->
                            "Completa todos los campos."
                        tooShort -> "La nueva contraseña debe tener al menos 6 caracteres."
                        nueva != confirmar -> "Las contraseñas no coinciden."
                        else -> "Contraseña validada. Se actualizará en el servidor próximamente."
                    }
                },
                text = "Actualizar contraseña",
                enabled = current.isNotBlank() && nueva.isNotBlank() && confirmar.isNotBlank() && !mismatch && !tooShort
            )

            info?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
