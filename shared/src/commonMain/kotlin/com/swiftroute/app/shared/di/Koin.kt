package com.swiftroute.app.shared.di

import com.swiftroute.app.shared.optimizer.RouteOptimizer
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * SwiftRoute Shared Module DI.
 */
val sharedModule = module {
    single { RouteOptimizer() }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(sharedModule)
    }

// iOS entry point for Koin
fun initKoin() = initKoin {}
