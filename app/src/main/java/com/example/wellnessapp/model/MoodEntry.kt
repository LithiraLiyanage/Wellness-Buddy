package com.example.wellnessapp.model

import kotlinx.serialization.Serializable

/**
 * Represents a mood entry with timestamp, emoji, and optional note.
 * 
 * @param id Unique identifier for the mood entry
 * @param timestamp Unix timestamp when the mood was recorded
 * @param emoji The emoji representing the mood
 * @param note Optional text note accompanying the mood
 */
@Serializable
data class MoodEntry(
    val id: String,
    val timestamp: Long,
    val emoji: String,
    val note: String? = null,
    val intensity: Float = 5f
) {
    /**
     * Returns the date string in YYYY-MM-DD format for this mood entry
     */
    fun getDateString(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Returns the time string in HH:mm format for this mood entry
     */
    fun getTimeString(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
}
