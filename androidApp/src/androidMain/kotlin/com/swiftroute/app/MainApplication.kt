package com.swiftroute.app

import android.app.Application
import com.swiftroute.app.shared.data.FirestoreRouteRepository
import com.swiftroute.app.shared.data.RouteRepository
import com.swiftroute.app.shared.di.initKoinAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import org.koin.dsl.module
import java.io.File

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Load API keys from .env file
        val projectRoot = File(filesDir, "..").parentFile?.parentFile?.parentFile?.parentFile?.absolutePath
            ?: System.getProperty("user.dir")
        ConfigManager.load(projectRoot)
        
        initKoinAndroid {
            androidLogger(Level.DEBUG)
            androidContext(this@MainApplication)
            // Override with Android-specific repository
            koin.loadModules(listOf(
                module {
                    single<RouteRepository> { FirestoreRouteRepository() }
                }
            ))
        }
    }
}
