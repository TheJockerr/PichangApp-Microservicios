package cl.duoc.pichangapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cl.duoc.pichangapp.ui.components.Avatar
import cl.duoc.pichangapp.ui.components.PichangButton
import cl.duoc.pichangapp.ui.components.PichangTextField
import cl.duoc.pichangapp.ui.components.PichangTopBar

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var nombre by rememberSaveable { mutableStateOf("") }
    var apellido by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var prefilled by rememberSaveable { mutableStateOf(false) }
    var info by remember { mutableStateOf<String?>(null) }

    // Prefill una sola vez cuando llega el usuario.
    LaunchedEffect(state.user) {
        if (!prefilled && state.user != null) {
            nombre = state.user!!.nombre
            apellido = state.user!!.apellido
            prefilled = true
        }
    }

    val fullName = listOf(nombre, apellido).joinToString(" ").trim().ifBlank { "Jugador" }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PichangTopBar(title = "Editar perfil", onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Selector de foto (UI lista; selección de imagen pendiente de backend).
            Box(contentAlignment = Alignment.BottomEnd) {
                Avatar(name = fullName, size = 110.dp)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            // TODO: abrir selector de imagen y subir foto cuando exista el endpoint.
                            info = "El cambio de foto estará disponible próximamente."
                        }
                ) {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = "Cambiar foto",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            PichangTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre",
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            PichangTextField(value = apellido, onValueChange = { apellido = it }, label = "Apellido",
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            PichangTextField(value = telefono, onValueChange = { telefono = it }, label = "Teléfono",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            Spacer(Modifier.height(24.dp))

            PichangButton(
                onClick = {
                    // TODO: conectar a endpoint de actualización de perfil cuando esté disponible.
                    info = if (nombre.isBlank() || apellido.isBlank())
                        "Nombre y apellido no pueden estar vacíos."
                    else
                        "Cambios guardados localmente. Se sincronizarán con el servidor próximamente."
                },
                text = "Guardar cambios"
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
