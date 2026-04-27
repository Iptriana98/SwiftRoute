package com.swiftroute.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DomainResultTest {

    @Test
    fun `Success should contain the expected value`() {
        val result = DomainResult.Success("Hello")
        
        assertTrue(result is DomainResult.Success)
        assertEquals("Hello", result.value)
    }

    @Test
    fun `Failure should contain the expected AppError`() {
        val error = AppError.Network
        val result = DomainResult.Failure(error)
        
        assertTrue(result is DomainResult.Failure)
        assertEquals(error, result.error)
    }
}
