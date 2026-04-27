package com.swiftroute.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteTest {

    @Test
    fun `Route should contain expected properties and default values`() {
        val stop = Stop(
            id = "stop-1",
            address = "123 Main St",
            latitude = 40.7128,
            longitude = -74.0060,
            notes = null
        )

        val route = Route(
            id = "route-1",
            name = "Morning Delivery",
            stops = listOf(stop),
            status = RouteStatus.Draft
        )

        assertEquals("route-1", route.id)
        assertEquals("Morning Delivery", route.name)
        assertTrue(route.stops.isNotEmpty())
        assertEquals(RouteStatus.Draft, route.status)
        assertEquals(OptimizationCriterion.Fastest, route.optimizationCriterion) // Default
    }
}
