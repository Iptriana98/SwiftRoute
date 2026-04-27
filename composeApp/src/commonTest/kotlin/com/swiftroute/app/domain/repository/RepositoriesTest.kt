package com.swiftroute.app.domain.repository

import com.swiftroute.app.domain.model.OptimizationCriterion
import com.swiftroute.app.domain.model.Route
import com.swiftroute.app.domain.model.Stop
import com.swiftroute.app.domain.model.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertTrue

class RepositoriesTest {

    // Dummy implementations to verify contracts
    class DummyRouteRepository : RouteRepository {
        override suspend fun saveRoute(route: Route): DomainResult<Unit> = DomainResult.Success(Unit)
        override fun observeAllRoutes(): Flow<List<Route>> = flowOf(emptyList())
        override suspend fun getRouteById(id: String): DomainResult<Route> = DomainResult.Success(
            Route("id", "name", emptyList())
        )
        override suspend fun deleteRoute(id: String): DomainResult<Unit> = DomainResult.Success(Unit)
    }

    class DummyGeocodingRepository : GeocodingRepository {
        override suspend fun searchAddress(query: String): DomainResult<List<Stop>> = DomainResult.Success(emptyList())
    }

    class DummyOptimizationRepository : OptimizationRepository {
        override suspend fun optimizeRoute(route: Route, criterion: OptimizationCriterion): DomainResult<Route> = DomainResult.Success(route)
    }

    @Test
    fun `Repository interfaces should be implementable with correct signatures`() {
        val routeRepo: RouteRepository = DummyRouteRepository()
        val geoRepo: GeocodingRepository = DummyGeocodingRepository()
        val optRepo: OptimizationRepository = DummyOptimizationRepository()

        assertTrue(routeRepo is RouteRepository)
        assertTrue(geoRepo is GeocodingRepository)
        assertTrue(optRepo is OptimizationRepository)
    }
}
