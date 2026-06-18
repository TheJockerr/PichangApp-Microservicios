package com.pichangapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pichangapp.ui.screens.auth.LoginScreen
import com.pichangapp.ui.screens.auth.RegisterScreen
import com.pichangapp.ui.screens.chat.ChatScreen
import com.pichangapp.ui.screens.event.EventDetailScreen
import com.pichangapp.ui.screens.friends.FriendsScreen
import com.pichangapp.ui.screens.home.HomeScreen
import com.pichangapp.ui.screens.map.MapScreen
import com.pichangapp.ui.screens.profile.ProfileScreen
import com.pichangapp.ui.screens.profile.PublicProfileScreen

/**
 * Grafo de navegación principal de PichangApp.
 *
 * @param navController Controller de navegación compartido
 * @param startDestination Ruta inicial (login o home según sesión activa)
 * @param modifier Modifier opcional para el NavHost
 */
@Composable
fun PichangNavGraph(
    navController    : NavHostController,
    startDestination : String = Routes.Login.route,
    modifier         : Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier
    ) {

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess   = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.Register.route)
                }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Routes.Home.route) {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(Routes.EventDetail.createRoute(eventId))
                },
                onNavigateToMap = { navController.navigate(Routes.Map.route) }
            )
        }

        // ── Map ───────────────────────────────────────────────────────────────
        composable(Routes.Map.route) {
            MapScreen(
                onEventClick = { eventId ->
                    navController.navigate(Routes.EventDetail.createRoute(eventId))
                }
            )
        }

        // ── Friends ───────────────────────────────────────────────────────────
        composable(Routes.Friends.route) {
            FriendsScreen(
                onUserClick = { userId ->
                    navController.navigate(Routes.PublicProfile.createRoute(userId))
                }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(Routes.Profile.route) {
            ProfileScreen(
                onNavigateToSettings = { navController.navigate(Routes.Settings.route) },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Event detail ──────────────────────────────────────────────────────
        composable(
            route     = Routes.EventDetail.route,
            arguments = listOf(
                navArgument(Routes.EventDetail.ARG_EVENT_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments
                ?.getString(Routes.EventDetail.ARG_EVENT_ID) ?: return@composable
            EventDetailScreen(
                eventId       = eventId,
                onNavigateBack = { navController.popBackStack() },
                onOpenChat     = { navController.navigate(Routes.Chat.createRoute(eventId)) }
            )
        }

        // ── Chat ──────────────────────────────────────────────────────────────
        composable(
            route     = Routes.Chat.route,
            arguments = listOf(
                navArgument(Routes.Chat.ARG_EVENT_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments
                ?.getString(Routes.Chat.ARG_EVENT_ID) ?: return@composable
            ChatScreen(
                eventId        = eventId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Public profile ────────────────────────────────────────────────────
        composable(
            route     = Routes.PublicProfile.route,
            arguments = listOf(
                navArgument(Routes.PublicProfile.ARG_USER_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments
                ?.getString(Routes.PublicProfile.ARG_USER_ID) ?: return@composable
            PublicProfileScreen(
                userId         = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}