package com.swiftroute.app.shared.data

/**
 * Platform-specific auth service provider
 */
expect fun provideAuthService(): AuthService

/**
 * Mock implementation for development and desktop
 */
class MockAuthService : AuthService() {
    
    private var mockUser: UserEntity? = null
    
    init {
        // Start as unauthenticated
        updateAuthState(null)
    }
    
    override suspend fun signIn(email: String, password: String): AuthResult {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)
        
        // Simple mock: accept any email with password "test123"
        return if (password == "test123" || password.length >= 6) {
            val user = UserEntity(
                id = "mock_${System.currentTimeMillis()}",
                email = email
            )
            mockUser = user
            updateAuthState(user)
            AuthResult(success = true, user = user)
        } else {
            AuthResult(success = false, error = "Invalid password")
        }
    }
    
    override suspend fun signUp(email: String, password: String): AuthResult {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)
        
        return if (password.length >= 6) {
            val user = UserEntity(
                id = "mock_${System.currentTimeMillis()}",
                email = email
            )
            mockUser = user
            updateAuthState(user)
            AuthResult(success = true, user = user)
        } else {
            AuthResult(success = false, error = "Password must be at least 6 characters")
        }
    }
    
    override fun signOut() {
        mockUser = null
        updateAuthState(null)
    }
}
