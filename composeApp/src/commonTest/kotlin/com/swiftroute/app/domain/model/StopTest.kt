package com.swiftroute.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StopTest {

    @Test
    fun `Stop data class should be instantiated correctly`() {
        val stop = Stop(
            id = "stop-1",
            address = "123 Main St",
            latitude = 40.7128,
            longitude = -74.0060,
            notes = "Leave at front door"
        )
        
        assertEquals("stop-1", stop.id)
        assertEquals("123 Main St", stop.address)
        assertEquals(40.7128, stop.latitude)
        assertEquals(-74.0060, stop.longitude)
        assertEquals("Leave at front door", stop.notes)
    }
}
