package com.swiftroute.app.shared.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Firebase Auth implementation for Android
 */
class FirebaseAuthService : AuthService() {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    init {
        // Listen to auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                updateAuthState(
                    UserEntity(
                        id = user.uid,
                        email = user.email ?: ""
                    )
                )
            } else {
                updateAuthState(null)
            }
        }
        
        // Check initial state
        auth.currentUser?.let { firebaseUser ->
            updateAuthState(
                UserEntity(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: ""
                )
            )
        }
    }
    
    /**
     * Sign in with email and password using Firebase Auth
     */
    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            // Validate inputs
            if (email.isBlank() || !email.contains("@")) {
                return AuthResult(success = false, error = "Invalid email format")
            }
            
            if (password.isEmpty()) {
                return AuthResult(success = false, error = "Password is required")
            }
            
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val userEntity = UserEntity(
                    id = user.uid,
                    email = user.email ?: ""
                )
                updateAuthState(userEntity)
                AuthResult(success = true, user = userEntity)
            } else {
                AuthResult(success = false, error = "Authentication failed")
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("user-not-found", ignoreCase = true) == true -> 
                    "No account found with this email"
                e.message?.contains("wrong-password", ignoreCase = true) == true -> 
                    "Incorrect password"
                e.message?.contains("invalid-email", ignoreCase = true) == true -> 
                    "Invalid email format"
                e.message?.contains("user-disabled", ignoreCase = true) == true -> 
                    "This account has been disabled"
                else -> e.message ?: "Authentication failed"
            }
            AuthResult(success = false, error = errorMessage)
        }
    }
    
    /**
     * Sign up with email and password using Firebase Auth
     */
    override suspend fun signUp(email: String, password: String): AuthResult {
        return try {
            // Validate inputs
            if (email.isBlank() || !email.contains("@")) {
                return AuthResult(success = false, error = "Invalid email format")
            }
            
            if (password.length < 6) {
                return AuthResult(success = false, error = "Password must be at least 6 characters")
            }
            
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val userEntity = UserEntity(
                    id = user.uid,
                    email = user.email ?: ""
                )
                updateAuthState(userEntity)
                AuthResult(success = true, user = userEntity)
            } else {
                AuthResult(success = false, error = "Registration failed")
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("email-already-in-use", ignoreCase = true) == true -> 
                    "An account with this email already exists"
                e.message?.contains("invalid-email", ignoreCase = true) == true -> 
                    "Invalid email format"
                e.message?.contains("weak-password", ignoreCase = true) == true -> 
                    "Password is too weak"
                else -> e.message ?: "Registration failed"
            }
            AuthResult(success = false, error = errorMessage)
        }
    }
    
    /**
     * Sign out
     */
    override fun signOut() {
        auth.signOut()
        updateAuthState(null)
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
}

/**
 * Android implementation of AuthService using Firebase Auth
 */
actual fun provideAuthService(): AuthService = FirebaseAuthService()
