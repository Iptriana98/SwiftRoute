package com.swiftroute.app.domain.repository

import com.swiftroute.app.domain.model.OptimizationCriterion
import com.swiftroute.app.domain.model.Route
import com.swiftroute.app.domain.model.Try

interface OptimizationRepository {
    suspend fun optimizeRoute(route: Route, criterion: OptimizationCriterion): Try<Route>
}
