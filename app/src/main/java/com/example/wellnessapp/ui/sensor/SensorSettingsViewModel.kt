package com.example.wellnessapp.ui.sensor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.data.SensorRepository
import com.example.wellnessapp.service.SensorService
import kotlinx.coroutines.launch

/**
 * ViewModel for sensor settings and data management.
 */
class SensorSettingsViewModel : ViewModel() {
    
    private val prefsManager = PrefsManager(com.example.wellnessapp.WellnessApplication.instance)
    private val sensorRepository = SensorRepository(prefsManager)
    
    // Sensor settings
    private val _sensorFeaturesEnabled = MutableLiveData<Boolean>()
    val sensorFeaturesEnabled: LiveData<Boolean> = _sensorFeaturesEnabled
    
    private val _shakeToMoodEnabled = MutableLiveData<Boolean>()
    val shakeToMoodEnabled: LiveData<Boolean> = _shakeToMoodEnabled
    
    // Sensor data
    private val _todayStepCount = MutableLiveData<Int>()
    val todayStepCount: LiveData<Int> = _todayStepCount
    
    private val _stepGoal = MutableLiveData<Int>()
    val stepGoal: LiveData<Int> = _stepGoal
    
    private val _sensorInfo = MutableLiveData<String>()
    val sensorInfo: LiveData<String> = _sensorInfo
    
    // UI state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    init {
        loadSettings()
    }
    
    /**
     * Loads current sensor settings and data
     */
    fun loadSensorData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Load settings
                loadSettings()
                
                // Load today's sensor data
                val todayData = sensorRepository.getTodaySensorData()
                _todayStepCount.value = todayData.dailySteps
                
                // Load step goal
                _stepGoal.value = sensorRepository.getStepGoal()
                
                // Load sensor info
                loadSensorInfo()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load sensor data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Loads sensor settings from preferences
     */
    private fun loadSettings() {
        _sensorFeaturesEnabled.value = prefsManager.getBoolean("sensor_features_enabled", false)
        _shakeToMoodEnabled.value = prefsManager.getBoolean("shake_to_mood_enabled", true)
    }
    
    /**
     * Loads sensor information
     */
    private fun loadSensorInfo() {
        try {
            // This would typically come from the SensorService
            // For now, we'll create a basic info string
            val info = buildString {
                appendLine("Accelerometer: Available")
                appendLine("Step Counter: ${if (prefsManager.getBoolean("hardware_step_counter_available", false)) "Hardware" else "Software"}")
                appendLine("Current Steps: ${_todayStepCount.value ?: 0}")
                appendLine("Step Goal: ${_stepGoal.value ?: 10000}")
                appendLine("Shake Detection: ${if (_shakeToMoodEnabled.value == true) "Enabled" else "Disabled"}")
            }
            _sensorInfo.value = info
        } catch (e: Exception) {
            _sensorInfo.value = "Error loading sensor information"
        }
    }
    
    /**
     * Updates sensor features setting
     */
    fun updateSensorFeatures(enabled: Boolean) {
        try {
            prefsManager.setBoolean("sensor_features_enabled", enabled)
            _sensorFeaturesEnabled.value = enabled
            
            _successMessage.value = if (enabled) {
                "Sensor features enabled"
            } else {
                "Sensor features disabled"
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update sensor features: ${e.message}"
        }
    }
    
    /**
     * Updates shake to mood setting
     */
    fun updateShakeToMood(enabled: Boolean) {
        try {
            prefsManager.setBoolean("shake_to_mood_enabled", enabled)
            _shakeToMoodEnabled.value = enabled
            
            _successMessage.value = if (enabled) {
                "Shake to mood enabled"
            } else {
                "Shake to mood disabled"
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update shake to mood: ${e.message}"
        }
    }
    
    /**
     * Sets step goal
     */
    fun setStepGoal(goal: Int) {
        try {
            sensorRepository.setStepGoal(goal)
            _stepGoal.value = goal
            
            _successMessage.value = "Step goal updated to $goal"
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update step goal: ${e.message}"
        }
    }
    
    /**
     * Resets today's step count
     */
    fun resetTodaySteps() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                sensorRepository.resetDailySensorData()
                _todayStepCount.value = 0
                
                _successMessage.value = "Step count reset"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset step count: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Tests shake detection
     */
    fun testShakeDetection() {
        try {
            // Simulate shake detection
            sensorRepository.incrementShakeCount()
            
            _successMessage.value = "Shake detected! Quick mood entry would open."
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to test shake detection: ${e.message}"
        }
    }
    
    /**
     * Refreshes sensor data
     */
    fun refreshSensorData() {
        loadSensorData()
    }
    
    /**
     * Gets sensor statistics
     */
    fun getSensorStatistics(days: Int = 7): com.example.wellnessapp.data.SensorStatistics {
        return sensorRepository.getSensorStatistics(days)
    }
    
    /**
     * Clears error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Clears success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
}
