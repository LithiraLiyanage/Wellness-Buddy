package com.example.wellnessapp.data

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnessapp.util.JsonUtils

/**
 * Manages SharedPreferences operations with JSON serialization support.
 * Provides a clean interface for storing and retrieving complex data types.
 */
class PrefsManager(context: Context) {
    
    val prefs: SharedPreferences = context.getSharedPreferences(
        "wellness_app_prefs", 
        Context.MODE_PRIVATE
    )
    
    // Keys for different data types
    companion object {
        const val PREF_HABITS = "pref_habits"
        const val PREF_COMPLETIONS_BY_DATE = "pref_completions_by_date"
        const val PREF_MOOD_ENTRIES = "pref_mood_entries"
        const val PREF_HYDRATION_ENABLED = "pref_hydration_enabled"
        const val PREF_HYDRATION_INTERVAL_MINUTES = "pref_hydration_interval_minutes"
        const val PREF_SENSOR_ENABLED = "pref_sensor_enabled"
    }
    
    /**
     * Stores a JSON-serialized object
     */
    inline fun <reified T> setJson(key: String, value: T) {
        val jsonString = JsonUtils.encodeToString(value)
        prefs.edit().putString(key, jsonString).apply()
    }
    
    /**
     * Retrieves and deserializes a JSON object
     */
    inline fun <reified T> getJson(key: String, defaultValue: T? = null): T? {
        val jsonString = prefs.getString(key, null)
        return if (jsonString != null) {
            JsonUtils.decodeFromString<T>(jsonString) ?: defaultValue
        } else {
            defaultValue
        }
    }
    
    /**
     * Stores a JSON-serialized list
     */
    inline fun <reified T> setJsonList(key: String, list: List<T>) {
        val jsonString = JsonUtils.encodeListToString(list)
        prefs.edit().putString(key, jsonString).apply()
    }
    
    /**
     * Retrieves and deserializes a JSON list
     */
    inline fun <reified T> getJsonList(key: String): List<T> {
        val jsonString = prefs.getString(key, null)
        return if (jsonString != null) {
            JsonUtils.decodeListFromString<T>(jsonString)
        } else {
            emptyList()
        }
    }
    
    /**
     * Stores a JSON-serialized map
     */
    inline fun <reified K, reified V> setJsonMap(key: String, map: Map<K, V>) {
        val jsonString = JsonUtils.encodeMapToString(map)
        prefs.edit().putString(key, jsonString).apply()
    }
    
    /**
     * Retrieves and deserializes a JSON map
     */
    inline fun <reified K, reified V> getJsonMap(key: String): Map<K, V> {
        val jsonString = prefs.getString(key, null)
        return if (jsonString != null) {
            JsonUtils.decodeMapFromString<K, V>(jsonString)
        } else {
            emptyMap()
        }
    }
    
    /**
     * Stores a boolean value
     */
    fun setBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
    
    /**
     * Retrieves a boolean value
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
    
    /**
     * Stores an integer value
     */
    fun setInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
    
    /**
     * Retrieves an integer value
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }
    
    /**
     * Stores a string value
     */
    fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    
    /**
     * Retrieves a string value
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return prefs.getString(key, defaultValue)
    }
    
    /**
     * Removes a key from preferences
     */
    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
    
    /**
     * Clears all preferences
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Checks if a key exists
     */
    fun contains(key: String): Boolean {
        return prefs.contains(key)
    }
}
