package com.swiftroute.app.shared.data

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Repository interface for route operations
 */
interface RouteRepository {
    fun getRoutes(userId: String): Flow<List<RouteEntity>>
    suspend fun getRoute(routeId: String): RouteEntity?
    suspend fun createRoute(route: RouteEntity): String
    suspend fun updateRoute(route: RouteEntity)
    suspend fun deleteRoute(routeId: String)
    suspend fun getStops(routeId: String): List<StopEntity>
    suspend fun addStop(stop: StopEntity): String
    suspend fun updateStop(stop: StopEntity)
    suspend fun deleteStop(stopId: String)
}

/**
 * Repository interface for route sessions
 */
interface RouteSessionRepository {
    fun getSessions(userId: String): Flow<List<RouteSessionEntity>>
    suspend fun createSession(session: RouteSessionEntity): String
    suspend fun updateSession(session: RouteSessionEntity)
    suspend fun getSession(sessionId: String): RouteSessionEntity?
}

/**
 * Repository interface for user preferences
 */
interface UserRepository {
    suspend fun getCurrentUser(): UserEntity?
    suspend fun updateUser(user: UserEntity)
    fun signIn(email: String, password: String): Result<UserEntity>
    fun signUp(email: String, password: String): Result<UserEntity>
    fun signOut()
}

/**
 * Route entity for Firestore
 */
@Serializable
data class RouteEntity(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val startLat: Double = 0.0,
    val startLng: Double = 0.0,
    val endLat: Double? = null,
    val endLng: Double? = null,
    val estimatedTotalMinutes: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Stop entity for Firestore
 */
@Serializable
data class StopEntity(
    val id: String = "",
    val routeId: String = "",
    val address: String = "",
    val label: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val order: Int = 0,
    val status: String = "PENDING",
    val serviceTimeMinutes: Int = 15,
    val notes: String? = null
)

/**
 * Route session entity for Firestore
 */
@Serializable
data class RouteSessionEntity(
    val id: String = "",
    val routeId: String = "",
    val userId: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val completedStopIds: List<String> = emptyList(),
    val skippedStopIds: List<String> = emptyList(),
    val currentStopIndex: Int = 0,
    val totalDurationMinutes: Int? = null,
    val endedManually: Boolean = false
)

/**
 * User entity for Firestore
 */
@Serializable
data class UserEntity(
    val id: String = "",
    val email: String = "",
    val preferredNavApp: String = "google_maps",
    val defaultServiceTimeMinutes: Int = 15,
    val createdAt: Long = System.currentTimeMillis()
)
