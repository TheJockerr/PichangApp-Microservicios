package com.pichangapp.domain.repository

import com.pichangapp.core.NetworkResult
import com.pichangapp.domain.model.*
import kotlinx.coroutines.flow.Flow

// ─── Auth ─────────────────────────────────────────────────────────────────────
interface AuthRepository {
    suspend fun login(email: String, password: String): NetworkResult<User>
    suspend fun register(name: String, email: String, password: String): NetworkResult<User>
    suspend fun logout(): NetworkResult<Unit>
    suspend fun refreshToken(): NetworkResult<String>
    fun getCurrentUser(): Flow<User?>
    fun isLoggedIn(): Boolean
}

// ─── Events ───────────────────────────────────────────────────────────────────
interface EventRepository {
    suspend fun getNearbyEvents(
        latitude    : Double,
        longitude   : Double,
        radiusKm    : Double        = 10.0,
        sport       : SportType?    = null
    ): NetworkResult<List<Event>>

    suspend fun getEventById(eventId: String): NetworkResult<Event>

    suspend fun createEvent(event: Event): NetworkResult<Event>

    suspend fun joinEvent(eventId: String): NetworkResult<Unit>

    suspend fun leaveEvent(eventId: String): NetworkResult<Unit>

    suspend fun getMyEvents(): NetworkResult<List<Event>>

    fun observeEvent(eventId: String): Flow<Event>
}

// ─── Karma ────────────────────────────────────────────────────────────────────
interface KarmaRepository {
    suspend fun getKarmaForUser(userId: String): NetworkResult<KarmaScore>
    suspend fun validateAttendance(eventId: String, userId: String): NetworkResult<Unit>
    suspend fun reportMissed(eventId: String, userId: String): NetworkResult<Unit>
}

// ─── Friends ─────────────────────────────────────────────────────────────────
interface FriendsRepository {
    suspend fun getFriends(): NetworkResult<List<User>>
    suspend fun searchUsers(query: String): NetworkResult<List<User>>
    suspend fun sendFriendRequest(userId: String): NetworkResult<Unit>
    suspend fun acceptFriendRequest(requestId: String): NetworkResult<Unit>
    suspend fun getPendingRequests(): NetworkResult<List<FriendRequest>>
    suspend fun getUserById(userId: String): NetworkResult<User>
}

// ─── Chat ─────────────────────────────────────────────────────────────────────
interface ChatRepository {
    fun observeMessages(eventId: String): Flow<List<Message>>
    suspend fun sendMessage(eventId: String, content: String): NetworkResult<Message>
    suspend fun getMessageHistory(eventId: String): NetworkResult<List<Message>>
}