package com.swiftroute.app.shared.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.jetbrains.annotations.Nullable

/**
 * Firebase implementation of RouteRepository
 * Note: Full Firebase implementation requires platform-specific setup
 */
class FirebaseRouteRepository : RouteRepository {
    
    // In-memory cache for demo (replace with Firestore in production)
    private val routesFlow = MutableStateFlow<List<RouteEntity>>(emptyList())
    private val stopsCache = mutableMapOf<String, MutableStateFlow<List<StopEntity>>>()

    override fun getRoutes(userId: String): Flow<List<RouteEntity>> {
        return routesFlow.map { routes -> 
            routes.filter { it.userId == userId }
                .sortedByDescending { it.updatedAt }
        }
    }

    override suspend fun getRoute(routeId: String): RouteEntity? {
        return routesFlow.value.find { it.id == routeId }
    }

    override suspend fun createRoute(route: RouteEntity): String {
        val id = generateId()
        val newRoute = route.copy(id = id, createdAt = System.currentTimeMillis())
        routesFlow.value = routesFlow.value + newRoute
        return id
    }

    override suspend fun updateRoute(route: RouteEntity) {
        routesFlow.value = routesFlow.value.map { 
            if (it.id == route.id) route.copy(updatedAt = System.currentTimeMillis())
            else it
        }
    }

    override suspend fun deleteRoute(routeId: String) {
        routesFlow.value = routesFlow.value.filter { it.id != routeId }
        stopsCache.remove(routeId)
    }

    override suspend fun getStops(routeId: String): List<StopEntity> {
        return stopsCache[routeId]?.value ?: emptyList()
    }

    override suspend fun addStop(stop: StopEntity): String {
        val id = generateId()
        val newStop = stop.copy(id = id)
        val stops = stopsCache.getOrPut(stop.routeId) { MutableStateFlow(emptyList()) }
        stops.value = stops.value + newStop
        return id
    }

    override suspend fun updateStop(stop: StopEntity) {
        val stops = stopsCache[stop.routeId] ?: return
        stops.value = stops.value.map { if (it.id == stop.id) stop else it }
    }

    override suspend fun deleteStop(stopId: String) {
        stopsCache.forEach { (_, stops) ->
            stops.value = stops.value.filter { it.id != stopId }
        }
    }

    private fun generateId(): String = "id_${System.currentTimeMillis()}_${(0..9999).random()}"
}

/**
 * Firebase implementation of UserRepository
 */
class FirebaseUserRepository : UserRepository {
    
    private var currentUser: UserEntity? = null

    override suspend fun getCurrentUser(): UserEntity? = currentUser

    override suspend fun updateUser(user: UserEntity) {
        currentUser = user
    }

    override fun signIn(email: String, password: String): Result<UserEntity> {
        // TODO: Implement Firebase Auth
        // For now, simulate successful login
        return try {
            val user = UserEntity(
                id = generateId(),
                email = email
            )
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signUp(email: String, password: String): Result<UserEntity> {
        // TODO: Implement Firebase Auth
        return try {
            val user = UserEntity(
                id = generateId(),
                email = email
            )
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        currentUser = null
    }

    private fun generateId(): String = "user_${System.currentTimeMillis()}"
}

/**
 * Firebase implementation of RouteSessionRepository
 */
class FirebaseSessionRepository : RouteSessionRepository {
    
    private val sessionsFlow = MutableStateFlow<List<RouteSessionEntity>>(emptyList())

    override fun getSessions(userId: String): Flow<List<RouteSessionEntity>> {
        return sessionsFlow.map { sessions ->
            sessions.filter { it.userId == userId }
                .sortedByDescending { it.startedAt }
        }
    }

    override suspend fun createSession(session: RouteSessionEntity): String {
        val id = "session_${System.currentTimeMillis()}"
        val newSession = session.copy(id = id)
        sessionsFlow.value = sessionsFlow.value + newSession
        return id
    }

    override suspend fun updateSession(session: RouteSessionEntity) {
        sessionsFlow.value = sessionsFlow.value.map { 
            if (it.id == session.id) session else it 
        }
    }

    override suspend fun getSession(sessionId: String): RouteSessionEntity? {
        return sessionsFlow.value.find { it.id == sessionId }
    }
}
