package com.swiftroute.app.shared.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Authentication service interface
 */
abstract class AuthService {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: Flow<AuthState> = _authState.asStateFlow()
    
    protected var _currentUser: UserEntity? = null
    val currentUser: UserEntity? get() = _currentUser
    
    fun isAuthenticated(): Boolean = _authState.value is AuthState.Authenticated
    
    fun updateAuthState(user: UserEntity?) {
        _currentUser = user
        _authState.value = if (user != null) {
            AuthState.Authenticated(user)
        } else {
            AuthState.Unauthenticated
        }
    }
    
    /**
     * Sign in with email and password
     */
    abstract suspend fun signIn(email: String, password: String): AuthResult
    
    /**
     * Sign up with email and password
     */
    abstract suspend fun signUp(email: String, password: String): AuthResult
    
    /**
     * Sign out
     */
    abstract fun signOut()
}

/**
 * Authentication state
 */
sealed class AuthState {
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
    data object Loading : AuthState()
}

/**
 * Result wrapper for auth operations
 */
data class AuthResult(
    val success: Boolean,
    val user: UserEntity? = null,
    val error: String? = null
)
