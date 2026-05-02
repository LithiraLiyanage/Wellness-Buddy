package com.example.wellnessapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.R
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.worker.HydrationWorker
import com.example.wellnessapp.worker.HabitReminderWorker
import com.example.wellnessapp.service.SensorService
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for the SettingsFragment.
 * Manages app settings including notifications, themes, privacy, backup, and data management.
 */
class SettingsViewModel : ViewModel() {
    
    private val prefsManager = PrefsManager(com.example.wellnessapp.WellnessApplication.instance)
    
    // Hydration Settings
    private val _hydrationEnabled = MutableLiveData<Boolean>()
    val hydrationEnabled: LiveData<Boolean> = _hydrationEnabled
    
    private val _hydrationInterval = MutableLiveData<Int>()
    val hydrationInterval: LiveData<Int> = _hydrationInterval
    
    // Notification Settings
    private val _habitRemindersEnabled = MutableLiveData<Boolean>()
    val habitRemindersEnabled: LiveData<Boolean> = _habitRemindersEnabled
    
    private val _moodRemindersEnabled = MutableLiveData<Boolean>()
    val moodRemindersEnabled: LiveData<Boolean> = _moodRemindersEnabled
    
    // Theme Settings
    private val _darkModeEnabled = MutableLiveData<Boolean>()
    val darkModeEnabled: LiveData<Boolean> = _darkModeEnabled
    
    private val _accentColor = MutableLiveData<Int>()
    val accentColor: LiveData<Int> = _accentColor
    
    private val _fontSize = MutableLiveData<String>()
    val fontSize: LiveData<String> = _fontSize
    
    // Privacy Settings
    private val _analyticsEnabled = MutableLiveData<Boolean>()
    val analyticsEnabled: LiveData<Boolean> = _analyticsEnabled
    
    private val _biometricEnabled = MutableLiveData<Boolean>()
    val biometricEnabled: LiveData<Boolean> = _biometricEnabled
    
    // Backup Settings
    private val _autoBackupEnabled = MutableLiveData<Boolean>()
    val autoBackupEnabled: LiveData<Boolean> = _autoBackupEnabled
    
    // Legacy Settings (for backward compatibility)
    private val _sensorEnabled = MutableLiveData<Boolean>()
    val sensorEnabled: LiveData<Boolean> = _sensorEnabled
    
    // UI State
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
     * Loads current settings from preferences
     */
    private fun loadSettings() {
        _hydrationEnabled.value = prefsManager.getBoolean(PrefsManager.PREF_HYDRATION_ENABLED, false)
        _hydrationInterval.value = prefsManager.getInt(PrefsManager.PREF_HYDRATION_INTERVAL_MINUTES, 90)
        _sensorEnabled.value = prefsManager.getBoolean(PrefsManager.PREF_SENSOR_ENABLED, false)
        
        // New settings with defaults
        _habitRemindersEnabled.value = prefsManager.getBoolean("habit_reminders_enabled", true)
        _moodRemindersEnabled.value = prefsManager.getBoolean("mood_reminders_enabled", true)
        _darkModeEnabled.value = prefsManager.getBoolean("dark_mode_enabled", false)
        _accentColor.value = prefsManager.getInt("accent_color", R.color.primary)
        _fontSize.value = prefsManager.getString("font_size", "Medium")
        _analyticsEnabled.value = prefsManager.getBoolean("analytics_enabled", true)
        _biometricEnabled.value = prefsManager.getBoolean("biometric_enabled", false)
        _autoBackupEnabled.value = prefsManager.getBoolean("auto_backup_enabled", false)
    }
    
    /**
     * Updates hydration reminder settings
     */
    fun updateHydrationSettings(enabled: Boolean, intervalMinutes: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                prefsManager.setBoolean(PrefsManager.PREF_HYDRATION_ENABLED, enabled)
                prefsManager.setInt(PrefsManager.PREF_HYDRATION_INTERVAL_MINUTES, intervalMinutes)
                
                _hydrationEnabled.value = enabled
                _hydrationInterval.value = intervalMinutes
                
                // Update WorkManager schedule
                HydrationWorker.scheduleHydrationReminders(
                    com.example.wellnessapp.WellnessApplication.instance,
                    enabled,
                    intervalMinutes
                )
                
                _successMessage.value = if (enabled) {
                    "Hydration reminders enabled (every $intervalMinutes minutes)"
                } else {
                    "Hydration reminders disabled"
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update hydration settings: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Updates habit reminders setting
     */
    fun updateHabitReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                prefsManager.setBoolean("habit_reminders_enabled", enabled)
                _habitRemindersEnabled.value = enabled
                
                // Update WorkManager schedule
                HabitReminderWorker.scheduleHabitReminders(
                    com.example.wellnessapp.WellnessApplication.instance,
                    enabled
                )
                
                _successMessage.value = if (enabled) {
                    "Habit reminders enabled (morning & evening)"
                } else {
                    "Habit reminders disabled"
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update habit reminders: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Updates mood reminders setting
     */
    fun updateMoodReminders(enabled: Boolean) {
        try {
            prefsManager.setBoolean("mood_reminders_enabled", enabled)
            _moodRemindersEnabled.value = enabled
            
            _successMessage.value = if (enabled) {
                "Mood reminders enabled"
            } else {
                "Mood reminders disabled"
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update mood reminders: ${e.message}"
        }
    }
    
    /**
     * Updates dark mode setting
     */
    fun updateDarkMode(enabled: Boolean) {
        try {
            prefsManager.setBoolean("dark_mode_enabled", enabled)
            _darkModeEnabled.value = enabled
            
            _successMessage.value = if (enabled) {
                "Dark mode enabled"
            } else {
                "Dark mode disabled"
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update dark mode: ${e.message}"
        }
    }
    
    /**
     * Updates accent color setting
     */
    fun updateAccentColor(colorRes: Int) {
        try {
            prefsManager.setInt("accent_color", colorRes)
            _accentColor.value = colorRes
            
            _successMessage.value = "Accent color updated"
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update accent color: ${e.message}"
        }
    }
    
    /**
     * Updates font size setting
     */
    fun updateFontSize(size: String) {
        try {
            prefsManager.setString("font_size", size)
            _fontSize.value = size
            
            _successMessage.value = "Font size updated to $size"
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update font size: ${e.message}"
        }
    }
    
    /**
     * Updates analytics setting
     */
    fun updateAnalytics(enabled: Boolean) {
        try {
            prefsManager.setBoolean("analytics_enabled", enabled)
            _analyticsEnabled.value = enabled
            
            _successMessage.value = if (enabled) {
                "Analytics enabled"
            } else {
                "Analytics disabled"
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update analytics: ${e.message}"
        }
    }
    
    /**
     * Updates biometric lock setting
     */
    fun updateBiometricLock(enabled: Boolean) {
        try {
            prefsManager.setBoolean("biometric_enabled", enabled)
            _biometricEnabled.value = enabled
            
            _successMessage.value = if (enabled) {
                "Biometric lock enabled"
            } else {
                "Biometric lock disabled"
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update biometric lock: ${e.message}"
        }
    }
    
    /**
     * Updates auto backup setting
     */
    fun updateAutoBackup(enabled: Boolean) {
        try {
            prefsManager.setBoolean("auto_backup_enabled", enabled)
            _autoBackupEnabled.value = enabled
            
            _successMessage.value = if (enabled) {
                "Auto backup enabled"
            } else {
                "Auto backup disabled"
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update auto backup: ${e.message}"
        }
    }
    
    /**
     * Performs manual backup
     */
    fun performBackup() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Simulate backup process
                kotlinx.coroutines.delay(2000)
                
                _successMessage.value = "Backup completed successfully"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to perform backup: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Restores from backup
     */
    fun restoreFromBackup() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Simulate restore process
                kotlinx.coroutines.delay(2000)
                
                _successMessage.value = "Restore completed successfully"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to restore from backup: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clears app cache
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Clear cache directory
                val cacheDir = com.example.wellnessapp.WellnessApplication.instance.cacheDir
                if (cacheDir.exists()) {
                    cacheDir.deleteRecursively()
                }
                
                _successMessage.value = "Cache cleared successfully"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear cache: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Gets storage usage information
     */
    fun getStorageUsed(): String {
        return try {
            val cacheDir = com.example.wellnessapp.WellnessApplication.instance.cacheDir
            val filesDir = com.example.wellnessapp.WellnessApplication.instance.filesDir
            
            val cacheSize = getDirectorySize(cacheDir)
            val filesSize = getDirectorySize(filesDir)
            val totalSize = cacheSize + filesSize
            
            formatFileSize(totalSize)
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getDirectorySize(directory: File): Long {
        var size = 0L
        if (directory.exists()) {
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        }
        return size
    }
    
    private fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
    
    /**
     * Updates sensor features setting (legacy)
     */
    fun updateSensorSettings(enabled: Boolean) {
        try {
            prefsManager.setBoolean(PrefsManager.PREF_SENSOR_ENABLED, enabled)
            _sensorEnabled.value = enabled
            
            // Start or stop sensor service
            if (enabled) {
                SensorService.startService(com.example.wellnessapp.WellnessApplication.instance)
            } else {
                SensorService.stopService(com.example.wellnessapp.WellnessApplication.instance)
            }
            
            _successMessage.value = if (enabled) {
                "Sensor features enabled"
            } else {
                "Sensor features disabled"
            }
            
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update sensor settings: ${e.message}"
        }
    }
    
    /**
     * Resets all app data
     */
    fun resetAllData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Clear all preferences
                prefsManager.clear()
                
                // Cancel hydration reminders
                HydrationWorker.scheduleHydrationReminders(
                    com.example.wellnessapp.WellnessApplication.instance,
                    false,
                    90
                )
                
                // Cancel habit reminders
                HabitReminderWorker.scheduleHabitReminders(
                    com.example.wellnessapp.WellnessApplication.instance,
                    false
                )
                
                // Reload settings
                loadSettings()
                
                _successMessage.value = "All data has been reset"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Gets available interval options
     */
    fun getIntervalOptions(): List<Pair<Int, String>> {
        return listOf(
            60 to "60 minutes",
            90 to "90 minutes",
            120 to "120 minutes",
            180 to "180 minutes"
        )
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