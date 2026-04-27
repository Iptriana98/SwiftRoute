package com.swiftroute.app.domain.repository

import com.swiftroute.app.domain.model.Route
import com.swiftroute.app.domain.model.Try
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    suspend fun saveRoute(route: Route): Try<Unit>
    fun observeAllRoutes(): Flow<List<Route>>
    suspend fun getRouteById(id: String): Try<Route>
    suspend fun deleteRoute(id: String): Try<Unit>
}
