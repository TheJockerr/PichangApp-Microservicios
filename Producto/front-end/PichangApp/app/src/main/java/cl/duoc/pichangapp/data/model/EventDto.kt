package cl.duoc.pichangapp.data.model

import androidx.compose.runtime.Stable

@Stable
data class EventDto(
    val id: Int,
    val organizerId: Int,
    val name: String,
    val sport: String,
    val eventDate: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val maxPlayers: Int,
    val currentPlayers: Int,
    val status: String,
    val createdAt: String,
    val distanceKm: Double?,
    val nombreCreador: String? = null   // nombre del organizador (lo envía el backend)
)

@Stable
data class CreateEventRequest(
    val name: String,
    val sport: String,
    val eventDate: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val maxPlayers: Int
)

@Stable
data class EventCheckInRequest(
    val latitude: Double,
    val longitude: Double
)

@Stable
data class AttendanceRequest(
    val userId: Int,
    val attended: Boolean
)

@Stable
data class EventRegistrationDto(
    val id: Int,
    val userId: Int,
    val status: String,
    val registeredAt: String
)
