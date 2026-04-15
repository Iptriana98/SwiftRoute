package com.swiftroute.app.shared.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Mock Firestore implementation of RouteRepository
 * Uses in-memory storage for development/demo purposes
 * 
 * This is a drop-in replacement that provides the same interface
 * but stores data in memory. For production, replace with
 * actual Firestore implementation.
 */
class FirestoreRouteRepository : RouteRepository {
    
    // In-memory cache for routes
    private val routesCache = mutableMapOf<String, MutableStateFlow<List<RouteEntity>>>()
    
    // In-memory cache for stops
    private val stopsCache = mutableMapOf<String, MutableStateFlow<List<StopEntity>>>()
    
    override fun getRoutes(userId: String): Flow<List<RouteEntity>> {
        return getOrCreateRoutesFlow(userId)
    }
    
    override suspend fun getRoute(routeId: String): RouteEntity? {
        return routesCache.values.asSequence()
            .map { it.value }
            .flatten()
            .find { it.id == routeId }
    }
    
    override suspend fun createRoute(route: RouteEntity): String {
        val routeId = route.id.ifEmpty { generateId() }
        val newRoute = route.copy(
            id = routeId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        getOrCreateRoutesFlow(route.userId).let { flow ->
            flow.value = flow.value + newRoute
        }
        
        return routeId
    }
    
    override suspend fun updateRoute(route: RouteEntity) {
        val userId = route.userId
        val updatedRoute = route.copy(updatedAt = System.currentTimeMillis())
        
        getOrCreateRoutesFlow(userId).let { flow ->
            flow.value = flow.value.map { if (it.id == route.id) updatedRoute else it }
        }
    }
    
    override suspend fun deleteRoute(routeId: String) {
        // Find the route to get its userId
        val route = getRoute(routeId) ?: return
        val userId = route.userId
        
        getOrCreateRoutesFlow(userId).let { flow ->
            flow.value = flow.value.filter { it.id != routeId }
        }
        
        // Also delete associated stops
        stopsCache.remove(routeId)
    }
    
    override suspend fun getStops(routeId: String): List<StopEntity> {
        return getOrCreateStopsFlow(routeId).value.sortedBy { it.order }
    }
    
    override suspend fun addStop(stop: StopEntity): String {
        val stopId = stop.id.ifEmpty { generateId() }
        val newStop = stop.copy(id = stopId)
        
        getOrCreateStopsFlow(stop.routeId).let { flow ->
            flow.value = flow.value + newStop
        }
        
        return stopId
    }
    
    override suspend fun updateStop(stop: StopEntity) {
        getOrCreateStopsFlow(stop.routeId).let { flow ->
            flow.value = flow.value.map { if (it.id == stop.id) stop else it }
        }
    }
    
    override suspend fun deleteStop(stopId: String) {
        // Find and remove the stop from any route
        stopsCache.forEach { (routeId, flow) ->
            val updatedStops = flow.value.filter { it.id != stopId }
            if (updatedStops.size != flow.value.size) {
                flow.value = updatedStops
            }
        }
    }
    
    private fun getOrCreateRoutesFlow(userId: String): MutableStateFlow<List<RouteEntity>> {
        return routesCache.getOrPut(userId) { 
            MutableStateFlow(emptyList()) 
        }
    }
    
    private fun getOrCreateStopsFlow(routeId: String): MutableStateFlow<List<StopEntity>> {
        return stopsCache.getOrPut(routeId) { 
            MutableStateFlow(emptyList()) 
        }
    }
    
    private fun generateId(): String = "id_${System.currentTimeMillis()}_${(0..9999).random()}"
}
