package com.example.wellnessapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.wellnessapp.MainActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.sensor.SensorManager
import android.util.Log
import kotlinx.coroutines.*

/**
 * Background service for continuous sensor monitoring.
 * Handles step counting and shake detection for quick mood entry.
 */
class SensorService : Service() {
    
    private val binder = SensorServiceBinder()
    private var sensorManager: SensorManager? = null
    private var prefsManager: PrefsManager? = null
    private var serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "sensor_service_channel"
        const val CHANNEL_NAME = "Sensor Service"
        const val ACTION_START_SERVICE = "com.example.wellnessapp.START_SENSOR_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.wellnessapp.STOP_SENSOR_SERVICE"
        
        fun startService(context: Context) {
            val intent = Intent(context, SensorService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, SensorService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.stopService(intent)
        }
    }
    
    inner class SensorServiceBinder : Binder() {
        fun getService(): SensorService = this@SensorService
    }
    
    override fun onCreate() {
        super.onCreate()
        prefsManager = PrefsManager(this)
        sensorManager = SensorManager(this)
        
        createNotificationChannel()
        setupSensorCallbacks()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                startForegroundService()
                startSensorMonitoring()
            }
            ACTION_STOP_SERVICE -> {
                stopSensorMonitoring()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onDestroy() {
        super.onDestroy()
        stopSensorMonitoring()
        serviceScope.cancel()
    }
    
    /**
     * Creates notification channel for foreground service
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background sensor monitoring for step counting and shake detection"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Starts foreground service with notification
     */
    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }
    
    /**
     * Creates notification for foreground service
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wellness Tracking Active")
            .setContentText("Monitoring steps and shake gestures")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    /**
     * Sets up sensor callbacks
     */
    private fun setupSensorCallbacks() {
        sensorManager?.apply {
            setOnStepDetected {
                // Handle step detection
                serviceScope.launch {
                    handleStepDetected()
                }
            }
            
            setOnShakeDetected {
                // Handle shake detection for quick mood entry
                serviceScope.launch {
                    handleShakeDetected()
                }
            }
            
            setOnStepCountChanged { stepCount ->
                // Save step count to preferences
                prefsManager?.setInt("daily_step_count", stepCount)
            }
        }
    }
    
    /**
     * Starts sensor monitoring
     */
    private fun startSensorMonitoring() {
        val isEnabled = prefsManager?.getBoolean("sensor_features_enabled", false) ?: false
        
        if (isEnabled) {
            sensorManager?.startListening()
            Log.d("SensorService", "Sensor monitoring started")
        } else {
            Log.d("SensorService", "Sensor features disabled")
        }
    }
    
    /**
     * Stops sensor monitoring
     */
    private fun stopSensorMonitoring() {
        sensorManager?.stopListening()
        Log.d("SensorService", "Sensor monitoring stopped")
    }
    
    /**
     * Handles step detection
     */
    private suspend fun handleStepDetected() {
        // Update step count in preferences
        val currentSteps = sensorManager?.getStepCount() ?: 0
        prefsManager?.setInt("daily_step_count", currentSteps)
        
        // Could add additional logic here like:
        // - Update widget
        // - Send analytics
        // - Check for step goals
        Log.d("SensorService", "Step detected: $currentSteps")
    }
    
    /**
     * Handles shake detection for quick mood entry
     */
    private suspend fun handleShakeDetected() {
        // Check if shake-to-mood is enabled
        val isEnabled = prefsManager?.getBoolean("shake_to_mood_enabled", true) ?: true
        
        if (isEnabled) {
            // Show quick mood entry dialog
            showQuickMoodDialog()
        }
    }
    
    /**
     * Shows quick mood entry dialog
     */
    private fun showQuickMoodDialog() {
        try {
            // Create intent for mood entry dialog
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("action", "quick_mood")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("SensorService", "Error showing quick mood dialog", e)
        }
    }
    
    /**
     * Gets current step count
     */
    fun getCurrentStepCount(): Int {
        return sensorManager?.getStepCount() ?: 0
    }
    
    /**
     * Resets step count
     */
    fun resetStepCount() {
        sensorManager?.resetStepCount()
    }
    
    /**
     * Gets sensor information
     */
    fun getSensorInfo(): String {
        return sensorManager?.getSensorInfo() ?: "Sensor manager not available"
    }
    
    /**
     * Updates sensor settings
     */
    fun updateSensorSettings(enabled: Boolean) {
        if (enabled) {
            startSensorMonitoring()
        } else {
            stopSensorMonitoring()
        }
    }
}
