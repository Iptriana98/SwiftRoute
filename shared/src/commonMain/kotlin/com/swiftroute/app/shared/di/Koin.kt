package com.swiftroute.app.shared.di

import com.swiftroute.app.shared.data.AuthService
import com.swiftroute.app.shared.data.FirebaseRouteRepository
import com.swiftroute.app.shared.data.provideAuthService
import com.swiftroute.app.shared.data.FirebaseSessionRepository
import com.swiftroute.app.shared.data.FirebaseUserRepository
import com.swiftroute.app.shared.data.RouteRepository
import com.swiftroute.app.shared.data.RouteSessionRepository
import com.swiftroute.app.shared.data.UserRepository
import com.swiftroute.app.shared.optimizer.RouteOptimizer
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Shared module - pure Kotlin, no platform dependencies
 */
val sharedModule = module {
    // Route optimizer (pure Kotlin)
    single { RouteOptimizer() }
    
    // Auth service
    single { provideAuthService() }
    
    // Repositories
    // Note: RouteRepository is provided by platform-specific module
    // For common/shared code, we use FirebaseRouteRepository as fallback
    single<RouteRepository> { FirebaseRouteRepository() }
    single<UserRepository> { FirebaseUserRepository() }
    single<RouteSessionRepository> { FirebaseSessionRepository() }
}



/**
 * Initialize Koin for Android
 */
fun initKoinAndroid(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(sharedModule)
    }

/**
 * Initialize Koin for iOS
 */
fun initKoinIos(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(sharedModule)
    }
