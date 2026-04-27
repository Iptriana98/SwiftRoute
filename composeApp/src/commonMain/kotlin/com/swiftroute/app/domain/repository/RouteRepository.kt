package com.swiftroute.app.domain.repository

import com.swiftroute.app.domain.model.Route
import com.swiftroute.app.domain.model.DomainResult
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    suspend fun saveRoute(route: Route): DomainResult<Unit>
    fun observeAllRoutes(): Flow<List<Route>>
    suspend fun getRouteById(id: String): DomainResult<Route>
    suspend fun deleteRoute(id: String): DomainResult<Unit>
}
