package cl.duoc.pichangapp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.pichangapp.ui.screens.auth.LoginScreen
import cl.duoc.pichangapp.ui.screens.auth.RegisterScreen
import cl.duoc.pichangapp.ui.screens.home.HomeScreen
import cl.duoc.pichangapp.ui.screens.karma.KarmaScreen
import cl.duoc.pichangapp.ui.screens.notifications.NotificationsScreen
import cl.duoc.pichangapp.ui.screens.profile.ProfileScreen
import cl.duoc.pichangapp.ui.screens.profile.EditProfileScreen
import cl.duoc.pichangapp.ui.screens.profile.ChangePasswordScreen
import cl.duoc.pichangapp.ui.screens.profile.NotificationPreferencesScreen
import cl.duoc.pichangapp.ui.screens.profile.AppearanceScreen
import cl.duoc.pichangapp.ui.screens.splash.SplashScreen
import cl.duoc.pichangapp.ui.screens.events.EventsScreen
import cl.duoc.pichangapp.ui.screens.events.CreateEventScreen
import cl.duoc.pichangapp.ui.screens.events.EventDetailScreen
import cl.duoc.pichangapp.ui.screens.events.AttendanceScreen

sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null,          // ícono relleno (seleccionado)
    val iconOutlined: ImageVector? = null   // ícono outline (no seleccionado)
) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home", "Inicio", Icons.Filled.Home, Icons.Outlined.Home)
    object Karma : Screen("karma", "Karma", Icons.Filled.Star, Icons.Outlined.StarBorder)
    object Notifications : Screen("notifications", "Avisos", Icons.Filled.Notifications, Icons.Outlined.Notifications)
    object Profile : Screen("profile", "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
    object Events : Screen("events", "Eventos", Icons.Filled.Map, Icons.Outlined.Map)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Karma,
    Screen.Events,
    Screen.Notifications,
    Screen.Profile
)

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val img = if (selected) screen.icon else (screen.iconOutlined ?: screen.icon)
                                img?.let { Icon(it, contentDescription = screen.title) }
                            },
                            label = { screen.title?.let { Text(it, style = MaterialTheme.typography.labelSmall) } },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            // Transiciones sutiles: fade + slide ligero entre pantallas.
            enterTransition = { fadeIn(tween(300)) + slideIntoContainer(SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(tween(240)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(240)) + slideOutOfContainer(SlideDirection.End, tween(300)) }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Login.route + "?mensaje={mensaje}") { backStackEntry ->
                val mensaje = backStackEntry.arguments?.getString("mensaje")
                LoginScreen(
                    mensaje = mensaje,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = { correo ->
                        navController.navigate("verify-code/$correo") {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }
            composable("verify-code/{email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                cl.duoc.pichangapp.ui.screens.auth.VerifyCodeScreen(
                    email = email,
                    onVerifySuccess = {
                        navController.navigate(Screen.Login.route + "?mensaje=Cuenta verificada, inicia sesión") {
                            popUpTo("verify-code/{email}") { inclusive = true }
                        }
                    }
                )
            }
            composable("events") {
                EventsScreen(navController = navController)
            }
            composable("events/create") {
                CreateEventScreen(navController = navController)
            }
            composable("events/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                EventDetailScreen(navController = navController, eventId = id)
            }
            composable("events/{id}/attendance") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                AttendanceScreen(navController = navController, eventId = id)
            }
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Karma.route) {
                KarmaScreen(navController = navController)
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(navController = navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            // ── Sub-pantallas de perfil ─────────────────────────────────────
            composable("edit-profile") {
                EditProfileScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("change-password") {
                ChangePasswordScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("notification-preferences") {
                NotificationPreferencesScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("appearance") {
                AppearanceScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
