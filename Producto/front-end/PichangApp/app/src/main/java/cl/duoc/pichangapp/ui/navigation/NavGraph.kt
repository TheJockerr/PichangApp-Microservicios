package cl.duoc.pichangapp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import cl.duoc.pichangapp.ui.screens.profile.SettingsScreen
import cl.duoc.pichangapp.ui.screens.splash.SplashScreen
import cl.duoc.pichangapp.ui.screens.events.EventsScreen
import cl.duoc.pichangapp.ui.screens.events.CreateEventScreen
import cl.duoc.pichangapp.ui.screens.events.EventDetailScreen
import cl.duoc.pichangapp.ui.screens.events.AttendanceScreen
import cl.duoc.pichangapp.ui.screens.events.MisEventosScreen
import cl.duoc.pichangapp.ui.screens.users.BuscarUsuariosScreen
import cl.duoc.pichangapp.ui.screens.users.PerfilPublicoScreen

/** Rutas con nombre (constantes) usadas por la app. */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Karma : Screen("karma")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object Events : Screen("events")
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        // Transiciones sutiles: fade + slide ligero entre pantallas.
        enterTransition = { fadeIn(tween(300)) + slideIntoContainer(SlideDirection.Start, tween(300)) },
        exitTransition = { fadeOut(tween(240)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(240)) + slideOutOfContainer(SlideDirection.End, tween(300)) }
    ) {
        // ── Pantallas sin sesión (sin Drawer) ──────────────────────────────────
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
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
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

        // ── Destinos principales (con Drawer vía MainScaffold) ──────────────────
        composable(Screen.Home.route) {
            MainScaffold(navController = navController, showFab = true) { openDrawer ->
                HomeScreen(navController = navController, onOpenDrawer = openDrawer)
            }
        }
        composable(Screen.Karma.route) {
            MainScaffold(navController = navController, showFab = false) { _ ->
                KarmaScreen(navController = navController)
            }
        }
        composable(Screen.Events.route) {
            MainScaffold(navController = navController, showFab = true) { _ ->
                EventsScreen(navController = navController)
            }
        }
        composable("mis-eventos") {
            MainScaffold(navController = navController, showFab = false) { _ ->
                MisEventosScreen(navController = navController)
            }
        }
        composable(Screen.Notifications.route) {
            MainScaffold(navController = navController, showFab = false) { _ ->
                NotificationsScreen(navController = navController)
            }
        }
        composable("buscar-usuarios") {
            MainScaffold(navController = navController, showFab = false) { _ ->
                BuscarUsuariosScreen(navController = navController)
            }
        }


        // ── Sub-pantallas de eventos (push, con su propia navegación) ───────────
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

        // ── Perfil público de otro usuario (query params: apellido puede ir vacío) ─
        composable("perfil-publico?nombre={nombre}&apellido={apellido}") { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre").orEmpty()
            val apellido = backStackEntry.arguments?.getString("apellido").orEmpty()
            PerfilPublicoScreen(navController = navController, nombre = nombre, apellido = apellido)
        }

        // ── Perfil propio + configuración (push) ────────────────────────────────
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
        composable("settings") {
            SettingsScreen(navController = navController)
        }
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
