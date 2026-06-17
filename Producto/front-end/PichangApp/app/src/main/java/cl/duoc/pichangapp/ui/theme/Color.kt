package cl.duoc.pichangapp.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
//  PALETA DE MARCA — PichangApp (inspiración dominante: Spotify)
//  Dark mode es la identidad principal: neutros oscuros + verde vibrante de acento.
//  Ver DESIGN_SYSTEM.md para el detalle de tokens.
// ═══════════════════════════════════════════════════════════════════════════════

// ─── Dark (dominante) ──────────────────────────────────────────────────────────
val PichangBlack         = Color(0xFF121212)  // Background  (negro Spotify)
val PichangSurface       = Color(0xFF1E1E1E)  // Surface
val PichangSurfaceElev    = Color(0xFF282828)  // SurfaceElevated (cards / inputs)
val PichangGreen         = Color(0xFF2ECC71)  // Primary     (verde vibrante)
val PichangGreenVariant  = Color(0xFF1DB954)  // PrimaryVariant (verde Spotify)
val PichangOrange        = Color(0xFFFF6F00)  // Accent      (naranja energía)
val PichangMuted         = Color(0xFFB3B3B3)  // OnSurfaceMuted (gris Spotify)
val PichangDivider       = Color(0xFF2A2A2A)
val PichangError         = Color(0xFFFF5252)

// ─── Light ───────────────────────────────────────────────────────────────────
val PichangBgLight       = Color(0xFFFAFAFA)
val PichangSurfaceLight  = Color(0xFFFFFFFF)
val PichangSurfaceElevLight = Color(0xFFF2F2F2)
val PichangGreenLight    = Color(0xFF1DB954)
val PichangMutedLight    = Color(0xFF6B6B6B)
val PichangDividerLight  = Color(0xFFE0E0E0)

// ─── Colores semánticos de Karma (independientes del tema) ─────────────────────
// Mantienen los nombres usados por Home / Karma / Notifications.
val KarmaExcellent = Color(0xFF2ECC71)  // excelente / oro
val KarmaGood      = Color(0xFF1DB954)  // bueno / plata
val KarmaRegular   = Color(0xFFFF6F00)  // regular / bronce
val KarmaLow       = Color(0xFFFF5252)  // bajo

// ─── Acentos por deporte (para gradientes tipo "carátula de álbum") ────────────
// Tonos vibrantes y distintos entre sí; se ven bien sobre fondos oscuros.
val SportFutbol    = Color(0xFF21C063)
val SportPadel     = Color(0xFFFF8A3D)
val SportTenis     = Color(0xFF9B6BFF)
val SportEsports   = Color(0xFF22C7E0)
val SportBasket    = Color(0xFFFF7A33)
val SportVolley    = Color(0xFFF65BA8)

// ═══════════════════════════════════════════════════════════════════════════════
//  Mapeo a Material 3 ColorScheme
// ═══════════════════════════════════════════════════════════════════════════════

// ─── Dark scheme ───────────────────────────────────────────────────────────────
val md_theme_dark_primary             = PichangGreen
val md_theme_dark_onPrimary           = Color(0xFF00210E)   // texto oscuro sobre verde (estilo Spotify)
val md_theme_dark_primaryContainer    = Color(0xFF14512E)
val md_theme_dark_onPrimaryContainer  = Color(0xFF8FF0BC)
val md_theme_dark_secondary           = PichangGreenVariant
val md_theme_dark_onSecondary         = Color(0xFF00210E)
val md_theme_dark_secondaryContainer  = Color(0xFF1F3D2C)
val md_theme_dark_onSecondaryContainer = Color(0xFFB6F0CF)
val md_theme_dark_tertiary            = PichangOrange
val md_theme_dark_onTertiary          = Color(0xFF3A1A00)
val md_theme_dark_tertiaryContainer   = Color(0xFF5C2E00)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFD9B3)
val md_theme_dark_error               = PichangError
val md_theme_dark_onError             = Color(0xFF3A0000)
val md_theme_dark_errorContainer      = Color(0xFF7A1212)
val md_theme_dark_onErrorContainer    = Color(0xFFFFD9D6)
val md_theme_dark_background          = PichangBlack
val md_theme_dark_onBackground        = Color(0xFFFFFFFF)
val md_theme_dark_surface             = PichangSurface
val md_theme_dark_onSurface           = Color(0xFFFFFFFF)
val md_theme_dark_surfaceVariant      = PichangSurfaceElev    // cards / inputs
val md_theme_dark_onSurfaceVariant    = PichangMuted          // texto secundario
val md_theme_dark_outline             = Color(0xFF3A3A3A)
val md_theme_dark_outlineVariant      = PichangDivider
val md_theme_dark_surfaceTint         = PichangGreen

// ─── Light scheme ──────────────────────────────────────────────────────────────
val md_theme_light_primary            = PichangGreenLight
val md_theme_light_onPrimary          = Color(0xFF052E16)
val md_theme_light_primaryContainer   = Color(0xFFC8F5D9)
val md_theme_light_onPrimaryContainer = Color(0xFF00391B)
val md_theme_light_secondary          = Color(0xFF146C34)
val md_theme_light_onSecondary        = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFCDEFD8)
val md_theme_light_onSecondaryContainer = Color(0xFF05291A)
val md_theme_light_tertiary           = PichangOrange
val md_theme_light_onTertiary         = Color(0xFF3A1A00)
val md_theme_light_tertiaryContainer  = Color(0xFFFFDDB8)
val md_theme_light_onTertiaryContainer = Color(0xFF2A1700)
val md_theme_light_error              = Color(0xFFD32F2F)
val md_theme_light_onError            = Color(0xFFFFFFFF)
val md_theme_light_errorContainer     = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer   = Color(0xFF410002)
val md_theme_light_background         = PichangBgLight
val md_theme_light_onBackground       = Color(0xFF121212)
val md_theme_light_surface            = PichangSurfaceLight
val md_theme_light_onSurface          = Color(0xFF121212)
val md_theme_light_surfaceVariant     = PichangSurfaceElevLight
val md_theme_light_onSurfaceVariant   = PichangMutedLight
val md_theme_light_outline            = Color(0xFFC4C4C4)
val md_theme_light_outlineVariant     = PichangDividerLight
val md_theme_light_surfaceTint        = PichangGreenLight
