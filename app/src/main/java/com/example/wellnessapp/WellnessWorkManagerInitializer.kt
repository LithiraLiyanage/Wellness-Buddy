package com.example.wellnessapp

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager

/**
 * Initializes WorkManager for the Wellness Buddy app.
 */
class WellnessWorkManagerInitializer : Initializer<WorkManager> {
    
    override fun create(context: Context): WorkManager {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
        
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }
    
    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
