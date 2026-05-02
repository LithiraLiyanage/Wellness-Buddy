package com.example.wellnessapp.model

import kotlinx.serialization.Serializable
import java.util.*

/**
 * Data class representing sensor data collected by the app.
 */
@Serializable
data class SensorData(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val stepCount: Int = 0,
    val dailySteps: Int = 0,
    val shakeCount: Int = 0,
    val date: String = "", // YYYY-MM-DD format
    val isHardwareStepCounter: Boolean = false
)

/**
 * Data class representing daily sensor summary.
 */
@Serializable
data class DailySensorSummary(
    val date: String, // YYYY-MM-DD format
    val totalSteps: Int,
    val shakeCount: Int,
    val isHardwareStepCounter: Boolean,
    val goalSteps: Int = 10000, // Default step goal
    val goalAchieved: Boolean = false
) {
    val stepProgress: Float
        get() = if (goalSteps > 0) (totalSteps.toFloat() / goalSteps.toFloat()).coerceAtMost(1.0f) else 0f
    
    val stepPercentage: Int
        get() = (stepProgress * 100).toInt()
}
