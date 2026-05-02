package com.example.wellnessapp.model

import kotlinx.serialization.Serializable

/**
 * Represents a habit that can be tracked daily.
 * 
 * @param id Unique identifier for the habit
 * @param title Display name of the habit
 * @param targetPerDay Optional target number of completions per day (null for simple boolean habits)
 * @param sortOrder Order for displaying habits in the UI
 * @param category Category of the habit for theming and organization
 */
@Serializable
data class Habit(
    val id: String,
    val title: String,
    val targetPerDay: Int? = null,
    val sortOrder: Int = 0,
    val category: HabitCategory = HabitCategory.OTHER
) {
    /**
     * Returns true if this is a simple boolean habit (no target count)
     */
    fun isBooleanHabit(): Boolean = targetPerDay == null
    
    /**
     * Returns true if this is a countable habit with a target
     */
    fun isCountableHabit(): Boolean = targetPerDay != null
}
