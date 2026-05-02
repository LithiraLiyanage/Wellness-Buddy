package com.example.wellnessapp

import android.app.Application

/**
 * Application class for Wellness Buddy app.
 * Provides global access to the application context.
 */
class WellnessApplication : Application() {
    
    companion object {
        lateinit var instance: WellnessApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
