package cl.duoc.pichangapp.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.pichangapp.R
import cl.duoc.pichangapp.data.model.KarmaDto
import cl.duoc.pichangapp.data.model.UserDto
import cl.duoc.pichangapp.ui.components.Avatar
import cl.duoc.pichangapp.ui.components.CategoryChip
import cl.duoc.pichangapp.ui.components.karmaColor

private data class DrawerItem(val label: String, val icon: ImageVector, val route: String)

@Composable
fun PichangDrawerContent(
    user: UserDto?,
    karma: KarmaDto?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val fullName = listOfNotNull(user?.nombre, user?.apellido)
        .joinToString(" ").ifBlank { "Jugador" }
    val categoria = karma?.categoria ?: "Sin categoría"
    val score = karma?.puntaje ?: 0
    val catColor = karmaColor(categoria)

    val items = listOf(
        DrawerItem("Inicio", Icons.Filled.Home, "home"),
        DrawerItem("Mi Karma", Icons.Filled.Star, "karma"),
        DrawerItem("Mis Eventos", Icons.Filled.SportsSoccer, "mis-eventos"),
        DrawerItem("Notificaciones", Icons.Filled.Notifications, "notifications"),
        DrawerItem("Buscar Usuarios", Icons.Filled.Group, "buscar-usuarios"),
        DrawerItem("Configuración", Icons.Filled.Settings, "settings")
    )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(catColor.copy(alpha = 0.30f), MaterialTheme.colorScheme.surface)
                        )
                    )
                    .padding(20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "PichangApp",
                    modifier = Modifier.height(40.dp)
                )
                Spacer(Modifier.height(20.dp))
                Avatar(name = fullName, size = 64.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                CategoryChip(label = "$categoria · $score pts", color = catColor, filled = true)
            }

            Spacer(Modifier.height(8.dp))

            // ── Navegación ──────────────────────────────────────────────────
            items.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.label) },
                    selected = false,
                    onClick = { onNavigate(item.route) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))

            // ── Cerrar sesión ───────────────────────────────────────────────
            NavigationDrawerItem(
                icon = {
                    Icon(
                        Icons.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                label = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) },
                selected = false,
                onClick = onLogout,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}
