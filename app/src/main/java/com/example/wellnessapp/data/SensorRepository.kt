package com.example.wellnessapp.data

import android.util.Log
import com.example.wellnessapp.model.SensorData
import com.example.wellnessapp.model.DailySensorSummary
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for managing sensor data persistence and retrieval.
 */
class SensorRepository(private val prefsManager: PrefsManager) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val PREF_SENSOR_DATA = "sensor_data"
        private const val PREF_DAILY_STEPS = "daily_step_count"
        private const val PREF_SHAKE_COUNT = "shake_count"
        private const val PREF_STEP_GOAL = "step_goal"
        private const val PREF_LAST_SENSOR_DATE = "last_sensor_date"
    }
    
    /**
     * Saves sensor data for a specific date
     */
    fun saveSensorData(sensorData: SensorData) {
        try {
            val dateKey = "${PREF_SENSOR_DATA}_${sensorData.date}"
            val jsonString = json.encodeToString(sensorData)
            prefsManager.setString(dateKey, jsonString)
            
            // Update daily steps
            prefsManager.setInt(PREF_DAILY_STEPS, sensorData.dailySteps)
            prefsManager.setString(PREF_LAST_SENSOR_DATE, sensorData.date)
            
            Log.d("SensorRepository", "Sensor data saved for ${sensorData.date}")
        } catch (e: Exception) {
            Log.e("SensorRepository", "Error saving sensor data", e)
        }
    }
    
    /**
     * Gets sensor data for a specific date
     */
    fun getSensorData(date: String): SensorData? {
        return try {
            val dateKey = "${PREF_SENSOR_DATA}_${date}"
            val jsonString = prefsManager.getString(dateKey, null)
            
            if (jsonString != null) {
                json.decodeFromString<SensorData>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SensorRepository", "Error loading sensor data for $date", e)
            null
        }
    }
    
    /**
     * Gets today's sensor data
     */
    fun getTodaySensorData(): SensorData {
        val today = getCurrentDateString()
        return getSensorData(today) ?: SensorData(
            date = today,
            dailySteps = prefsManager.getInt(PREF_DAILY_STEPS, 0),
            isHardwareStepCounter = prefsManager.getBoolean("hardware_step_counter_available", false)
        )
    }
    
    /**
     * Updates daily step count
     */
    fun updateDailySteps(steps: Int) {
        prefsManager.setInt(PREF_DAILY_STEPS, steps)
        
        // Update today's sensor data
        val today = getCurrentDateString()
        val todayData = getTodaySensorData().copy(dailySteps = steps)
        saveSensorData(todayData)
    }
    
    /**
     * Increments shake count
     */
    fun incrementShakeCount() {
        val today = getCurrentDateString()
        val todayData = getTodaySensorData()
        val updatedData = todayData.copy(shakeCount = todayData.shakeCount + 1)
        saveSensorData(updatedData)
    }
    
    /**
     * Gets daily sensor summary for a specific date
     */
    fun getDailySensorSummary(date: String): DailySensorSummary {
        val sensorData = getSensorData(date) ?: SensorData(date = date)
        val stepGoal = getStepGoal()
        
        return DailySensorSummary(
            date = date,
            totalSteps = sensorData.dailySteps,
            shakeCount = sensorData.shakeCount,
            isHardwareStepCounter = sensorData.isHardwareStepCounter,
            goalSteps = stepGoal,
            goalAchieved = sensorData.dailySteps >= stepGoal
        )
    }
    
    /**
     * Gets today's sensor summary
     */
    fun getTodaySensorSummary(): DailySensorSummary {
        val today = getCurrentDateString()
        return getDailySensorSummary(today)
    }
    
    /**
     * Sets step goal
     */
    fun setStepGoal(goal: Int) {
        prefsManager.setInt(PREF_STEP_GOAL, goal)
    }
    
    /**
     * Gets step goal
     */
    fun getStepGoal(): Int {
        return prefsManager.getInt(PREF_STEP_GOAL, 10000) // Default 10,000 steps
    }
    
    /**
     * Gets all sensor data for the last N days
     */
    fun getSensorDataForLastDays(days: Int): List<SensorData> {
        val sensorDataList = mutableListOf<SensorData>()
        
        for (i in 0 until days) {
            val date = getDateStringForDaysAgo(i)
            val data = getSensorData(date)
            if (data != null) {
                sensorDataList.add(data)
            }
        }
        
        return sensorDataList
    }
    
    /**
     * Gets all daily summaries for the last N days
     */
    fun getDailySummariesForLastDays(days: Int): List<DailySensorSummary> {
        val summaries = mutableListOf<DailySensorSummary>()
        
        for (i in 0 until days) {
            val date = getDateStringForDaysAgo(i)
            summaries.add(getDailySensorSummary(date))
        }
        
        return summaries
    }
    
    /**
     * Resets daily sensor data
     */
    fun resetDailySensorData() {
        prefsManager.setInt(PREF_DAILY_STEPS, 0)
        prefsManager.setInt(PREF_SHAKE_COUNT, 0)
        
        val today = getCurrentDateString()
        val resetData = SensorData(
            date = today,
            dailySteps = 0,
            shakeCount = 0
        )
        saveSensorData(resetData)
    }
    
    /**
     * Clears all sensor data
     */
    fun clearAllSensorData() {
        // Clear daily steps
        prefsManager.setInt(PREF_DAILY_STEPS, 0)
        prefsManager.setInt(PREF_SHAKE_COUNT, 0)
        prefsManager.setString(PREF_LAST_SENSOR_DATE, "")
        
        // Note: We can't easily clear all date-specific sensor data without knowing all dates
        // This would require a more sophisticated storage solution
        Log.d("SensorRepository", "Sensor data cleared")
    }
    
    /**
     * Gets sensor statistics
     */
    fun getSensorStatistics(days: Int = 7): SensorStatistics {
        val summaries = getDailySummariesForLastDays(days)
        
        val totalSteps = summaries.sumOf { it.totalSteps }
        val averageSteps = if (summaries.isNotEmpty()) totalSteps / summaries.size else 0
        val totalShakes = summaries.sumOf { it.shakeCount }
        val goalAchievedDays = summaries.count { it.goalAchieved }
        
        return SensorStatistics(
            totalSteps = totalSteps,
            averageSteps = averageSteps,
            totalShakes = totalShakes,
            goalAchievedDays = goalAchievedDays,
            totalDays = summaries.size,
            stepGoal = getStepGoal()
        )
    }
}

/**
 * Data class for sensor statistics
 */
data class SensorStatistics(
    val totalSteps: Int,
    val averageSteps: Int,
    val totalShakes: Int,
    val goalAchievedDays: Int,
    val totalDays: Int,
    val stepGoal: Int
) {
    val goalAchievementRate: Float
        get() = if (totalDays > 0) goalAchievedDays.toFloat() / totalDays.toFloat() else 0f
    
    val goalAchievementPercentage: Int
        get() = (goalAchievementRate * 100).toInt()
}

/**
 * Utility functions for date handling
 */
private fun getCurrentDateString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}

private fun getDateStringForDaysAgo(daysAgo: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -daysAgo)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(calendar.time)
}
