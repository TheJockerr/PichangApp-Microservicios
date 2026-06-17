package com.pichangapp.domain.model

import java.time.LocalDateTime

// ─── Sport type ───────────────────────────────────────────────────────────────
enum class SportType(val displayName: String, val emoji: String) {
    FUTBOL  ("Fútbol",  "⚽"),
    PADEL   ("Pádel",   "🏸"),
    TENIS   ("Tenis",   "🎾"),
    ESPORTS ("E-Sports","🎮"),
    BASKET  ("Básquet", "🏀"),
    VOLLEY  ("Vóleibol","🏐")
}

// ─── Skill level ──────────────────────────────────────────────────────────────
enum class SkillLevel(val displayName: String) {
    BEGINNER    ("Principiante"),
    INTERMEDIATE("Intermedio"),
    ADVANCED    ("Avanzado"),
    ALL         ("Todos los niveles")
}

// ─── Karma status ─────────────────────────────────────────────────────────────
enum class KarmaStatus(val label: String, val minScore: Int) {
    TRUSTED  ("Usuario Confiable", 80),
    REGULAR  ("Regular",           50),
    RISKY    ("Poco Confiable",     0)
}

data class KarmaScore(
    val score     : Int,               // 0–100
    val totalGames: Int,
    val attended  : Int,
    val missed    : Int
) {
    val percentage: Float get() = score / 100f
    val status: KarmaStatus get() = when {
        score >= KarmaStatus.TRUSTED.minScore -> KarmaStatus.TRUSTED
        score >= KarmaStatus.REGULAR.minScore -> KarmaStatus.REGULAR
        else                                  -> KarmaStatus.RISKY
    }
}

// ─── User ─────────────────────────────────────────────────────────────────────
data class User(
    val id          : String,
    val name        : String,
    val username    : String,
    val email       : String,
    val avatarUrl   : String?      = null,
    val location    : String?      = null,
    val bio         : String?      = null,
    val karma       : KarmaScore,
    val sports      : List<SportType> = emptyList(),
    val friendCount : Int          = 0,
    val eventsPlayed: Int          = 0,
    val isOnline    : Boolean      = false
)

// ─── Event ────────────────────────────────────────────────────────────────────
data class EventLocation(
    val latitude : Double,
    val longitude: Double,
    val address  : String,
    val placeName: String?   = null    // "Cancha Municipal Norte"
)

data class Event(
    val id           : String,
    val title        : String,
    val sport        : SportType,
    val level        : SkillLevel,
    val organizer    : User,
    val location     : EventLocation,
    val dateTime     : LocalDateTime,
    val totalSlots   : Int,
    val filledSlots  : Int,
    val description  : String?   = null,
    val price        : Double    = 0.0,     // 0 = gratis
    val participants : List<User> = emptyList(),
    val hasJoined    : Boolean   = false,
    val distanceKm   : Double?   = null     // calculado en cliente
) {
    val availableSlots  : Int     get() = totalSlots - filledSlots
    val isFull          : Boolean get() = availableSlots <= 0
    val occupancyPercent: Float   get() = filledSlots.toFloat() / totalSlots
    val isFree          : Boolean get() = price == 0.0
}

// ─── Message (Chat) ──────────────────────────────────────────────────────────
data class Message(
    val id        : String,
    val eventId   : String,
    val sender    : User,
    val content   : String,
    val timestamp : LocalDateTime,
    val isOwn     : Boolean = false   // true si lo envió el usuario actual
)

// ─── Friend request ───────────────────────────────────────────────────────────
enum class FriendRequestStatus { PENDING, ACCEPTED, REJECTED }

data class FriendRequest(
    val id       : String,
    val from     : User,
    val to       : User,
    val status   : FriendRequestStatus,
    val createdAt: LocalDateTime
)