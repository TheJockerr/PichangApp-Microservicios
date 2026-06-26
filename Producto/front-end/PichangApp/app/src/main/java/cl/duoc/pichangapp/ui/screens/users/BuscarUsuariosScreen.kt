package cl.duoc.pichangapp.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cl.duoc.pichangapp.data.model.PerfilPublicoDto
import cl.duoc.pichangapp.ui.components.Avatar
import cl.duoc.pichangapp.ui.components.CategoryChip
import cl.duoc.pichangapp.ui.components.EmptyState
import cl.duoc.pichangapp.ui.components.PichangTextField
import cl.duoc.pichangapp.ui.components.karmaColor

@Composable
fun BuscarUsuariosScreen(
    navController: NavController,
    viewModel: BuscarUsuariosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Buscar usuarios",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        PichangTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            label = "Nombre o apellido",
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
        )

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            state.error != null -> EmptyState(emoji = "⚠️", title = "Error", message = state.error!!)
            state.query.trim().length < 2 -> EmptyState(
                emoji = "🔎",
                title = "Encuentra jugadores",
                message = "Escribe al menos 2 letras del nombre o apellido."
            )
            state.searched && state.results.isEmpty() -> EmptyState(
                emoji = "🤷",
                title = "Sin resultados",
                message = "No encontramos usuarios que coincidan con tu búsqueda."
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.results, key = { it.nombre.orEmpty() + "|" + it.apellido.orEmpty() }) { perfil ->
                    UsuarioResultRow(perfil = perfil) {
                        val nombre = android.net.Uri.encode(perfil.nombre)
                        val apellido = android.net.Uri.encode(perfil.apellido)
                        navController.navigate("perfil-publico?nombre=$nombre&apellido=$apellido")
                    }
                }
            }
        }
    }
}

@Composable
private fun UsuarioResultRow(perfil: PerfilPublicoDto, onClick: () -> Unit) {
    val fullName = "${perfil.nombre.orEmpty()} ${perfil.apellido.orEmpty()}".trim()
    val categoria = perfil.categoriaKarma.orEmpty().ifBlank { "Sin categoría" }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(name = fullName.ifBlank { "?" }, size = 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = fullName.ifBlank { "Usuario" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                CategoryChip(
                    label = "$categoria · ${perfil.karmaScore} pts",
                    color = karmaColor(categoria),
                    filled = false
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
