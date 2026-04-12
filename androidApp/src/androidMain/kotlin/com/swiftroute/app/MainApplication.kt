package com.swiftroute.app

import android.app.Application
import com.swiftroute.app.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MainApplication)
        }
    }
}
