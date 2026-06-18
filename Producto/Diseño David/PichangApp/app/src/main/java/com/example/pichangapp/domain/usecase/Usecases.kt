package com.pichangapp.domain.usecase

import com.pichangapp.core.NetworkResult
import com.pichangapp.domain.model.*
import com.pichangapp.domain.repository.*
import javax.inject.Inject

// ─── Auth use cases ───────────────────────────────────────────────────────────
class LoginUseCase @Inject constructor(
    private val authRepo: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): NetworkResult<User> {
        if (email.isBlank() || password.isBlank())
            return NetworkResult.Error("Email y contraseña son requeridos.")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return NetworkResult.Error("El email no tiene un formato válido.")
        if (password.length < 6)
            return NetworkResult.Error("La contraseña debe tener al menos 6 caracteres.")
        return authRepo.login(email.trim(), password)
    }
}

class RegisterUseCase @Inject constructor(
    private val authRepo: AuthRepository
) {
    suspend operator fun invoke(
        name    : String,
        email   : String,
        password: String,
        confirm : String
    ): NetworkResult<User> {
        if (name.isBlank())    return NetworkResult.Error("El nombre es requerido.")
        if (email.isBlank())   return NetworkResult.Error("El email es requerido.")
        if (password != confirm) return NetworkResult.Error("Las contraseñas no coinciden.")
        if (password.length < 6) return NetworkResult.Error("Mínimo 6 caracteres.")
        return authRepo.register(name.trim(), email.trim(), password)
    }
}

// ─── Event use cases ──────────────────────────────────────────────────────────
class GetNearbyEventsUseCase @Inject constructor(
    private val eventRepo: EventRepository
) {
    suspend operator fun invoke(
        latitude : Double,
        longitude: Double,
        radiusKm : Double     = 10.0,
        sport    : SportType? = null
    ): NetworkResult<List<Event>> = eventRepo.getNearbyEvents(latitude, longitude, radiusKm, sport)
}

class GetEventDetailUseCase @Inject constructor(
    private val eventRepo: EventRepository
) {
    suspend operator fun invoke(eventId: String): NetworkResult<Event> =
        eventRepo.getEventById(eventId)
}

class JoinEventUseCase @Inject constructor(
    private val eventRepo: EventRepository
) {
    suspend operator fun invoke(event: Event): NetworkResult<Unit> {
        if (event.isFull) return NetworkResult.Error("El partido ya no tiene cupos disponibles.")
        if (event.hasJoined) return NetworkResult.Error("Ya estás inscrito en este partido.")
        return eventRepo.joinEvent(event.id)
    }
}

class LeaveEventUseCase @Inject constructor(
    private val eventRepo: EventRepository
) {
    suspend operator fun invoke(eventId: String): NetworkResult<Unit> =
        eventRepo.leaveEvent(eventId)
}

// ─── Karma use cases ──────────────────────────────────────────────────────────
class GetUserKarmaUseCase @Inject constructor(
    private val karmaRepo: KarmaRepository
) {
    suspend operator fun invoke(userId: String): NetworkResult<KarmaScore> =
        karmaRepo.getKarmaForUser(userId)
}

// ─── Friends use cases ───────────────────────────────────────────────────────
class SearchUsersUseCase @Inject constructor(
    private val friendsRepo: FriendsRepository
) {
    suspend operator fun invoke(query: String): NetworkResult<List<User>> {
        if (query.length < 2) return NetworkResult.Success(emptyList())
        return friendsRepo.searchUsers(query.trim())
    }
}

class SendFriendRequestUseCase @Inject constructor(
    private val friendsRepo: FriendsRepository
) {
    suspend operator fun invoke(userId: String): NetworkResult<Unit> =
        friendsRepo.sendFriendRequest(userId)
}