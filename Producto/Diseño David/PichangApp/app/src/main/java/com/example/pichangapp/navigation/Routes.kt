package com.pichangapp.navigation

/**
 * Rutas de navegación centralizadas.
 * Usar sealed class + object evita typos de strings en el NavGraph.
 */
sealed class Routes(val route: String) {

    // ── Auth ──────────────────────────────────────────────────────────────────
    data object Login    : Routes("login")
    data object Register : Routes("register")

    // ── Main (bottom nav) ─────────────────────────────────────────────────────
    data object Home     : Routes("home")
    data object Map      : Routes("map")
    data object Friends  : Routes("friends")
    data object Profile  : Routes("profile")

    // ── Detail screens ────────────────────────────────────────────────────────
    /** Detalle de evento. Requiere [eventId]. */
    data object EventDetail : Routes("event/{eventId}") {
        fun createRoute(eventId: String) = "event/$eventId"
        const val ARG_EVENT_ID = "eventId"
    }

    /** Chat de un partido. Requiere [eventId]. */
    data object Chat : Routes("chat/{eventId}") {
        fun createRoute(eventId: String) = "chat/$eventId"
        const val ARG_EVENT_ID = "eventId"
    }

    /** Perfil público de otro usuario. */
    data object PublicProfile : Routes("user/{userId}") {
        fun createRoute(userId: String) = "user/$userId"
        const val ARG_USER_ID = "userId"
    }

    // ── Onboarding ────────────────────────────────────────────────────────────
    data object Splash      : Routes("splash")
    data object Onboarding  : Routes("onboarding")

    // ── Settings ──────────────────────────────────────────────────────────────
    data object Settings    : Routes("settings")
}

/** Rutas que pertenecen al bottom navigation bar */
val bottomNavRoutes = listOf(
    Routes.Home,
    Routes.Map,
    Routes.Friends,
    Routes.Profile
)