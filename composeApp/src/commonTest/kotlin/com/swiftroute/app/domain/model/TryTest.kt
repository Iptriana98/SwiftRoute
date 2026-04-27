package com.swiftroute.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TryTest {

    @Test
    fun `Success should contain the expected value`() {
        val result = Try.Success("Hello")
        
        assertTrue(result is Try.Success)
        assertEquals("Hello", result.value)
    }

    @Test
    fun `Failure should contain the expected AppError`() {
        val error = AppError.Network
        val result = Try.Failure(error)
        
        assertTrue(result is Try.Failure)
        assertEquals(error, result.error)
    }
}
