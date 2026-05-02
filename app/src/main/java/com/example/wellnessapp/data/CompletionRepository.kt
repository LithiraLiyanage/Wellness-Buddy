package com.example.wellnessapp.data

import com.example.wellnessapp.util.DateUtils

/**
 * Repository for managing habit completion data.
 * Tracks daily completion counts for each habit.
 */
class CompletionRepository(private val prefsManager: PrefsManager) {
    
    /**
     * Gets all completion data
     */
    fun getAllCompletions(): Map<String, Map<String, Int>> {
        return prefsManager.getJsonMap<String, Map<String, Int>>(PrefsManager.PREF_COMPLETIONS_BY_DATE)
    }
    
    /**
     * Gets completion data for a specific date
     */
    fun getCompletionsForDate(dateString: String): Map<String, Int> {
        return getAllCompletions()[dateString] ?: emptyMap()
    }
    
    /**
     * Gets completion count for a specific habit on a specific date
     */
    fun getCompletionCount(habitId: String, dateString: String): Int {
        return getCompletionsForDate(dateString)[habitId] ?: 0
    }
    
    /**
     * Gets today's completion count for a habit
     */
    fun getTodayCompletionCount(habitId: String): Int {
        return getCompletionCount(habitId, DateUtils.getTodayString())
    }
    
    /**
     * Sets completion count for a habit on a specific date
     */
    fun setCompletionCount(habitId: String, dateString: String, count: Int) {
        val allCompletions = getAllCompletions().toMutableMap()
        val dateCompletions = allCompletions[dateString]?.toMutableMap() ?: mutableMapOf()
        
        if (count > 0) {
            dateCompletions[habitId] = count
        } else {
            dateCompletions.remove(habitId)
        }
        
        if (dateCompletions.isNotEmpty()) {
            allCompletions[dateString] = dateCompletions
        } else {
            allCompletions.remove(dateString)
        }
        
        saveAllCompletions(allCompletions)
    }
    
    /**
     * Increments completion count for a habit on a specific date
     */
    fun incrementCompletion(habitId: String, dateString: String) {
        val currentCount = getCompletionCount(habitId, dateString)
        setCompletionCount(habitId, dateString, currentCount + 1)
    }
    
    /**
     * Decrements completion count for a habit on a specific date
     */
    fun decrementCompletion(habitId: String, dateString: String) {
        val currentCount = getCompletionCount(habitId, dateString)
        if (currentCount > 0) {
            setCompletionCount(habitId, dateString, currentCount - 1)
        }
    }
    
    /**
     * Toggles completion for a boolean habit (0 or 1)
     */
    fun toggleBooleanCompletion(habitId: String, dateString: String) {
        val currentCount = getCompletionCount(habitId, dateString)
        val newCount = if (currentCount > 0) 0 else 1
        setCompletionCount(habitId, dateString, newCount)
    }
    
    /**
     * Checks if a habit is completed for a specific date (for boolean habits)
     */
    fun isCompleted(habitId: String, dateString: String): Boolean {
        return getCompletionCount(habitId, dateString) > 0
    }
    
    /**
     * Checks if a habit is completed today
     */
    fun isCompletedToday(habitId: String): Boolean {
        return isCompleted(habitId, DateUtils.getTodayString())
    }
    
    /**
     * Gets completion percentage for a specific date
     */
    fun getCompletionPercentage(dateString: String, habits: List<com.example.wellnessapp.model.Habit>): Double {
        if (habits.isEmpty()) return 0.0
        
        var totalPossible = 0
        var totalCompleted = 0
        
        habits.forEach { habit ->
            if (habit.isBooleanHabit()) {
                totalPossible += 1
                if (isCompleted(habit.id, dateString)) {
                    totalCompleted += 1
                }
            } else if (habit.isCountableHabit()) {
                val target = habit.targetPerDay ?: 1
                val completed = getCompletionCount(habit.id, dateString)
                totalPossible += target
                totalCompleted += minOf(completed, target)
            }
        }
        
        return if (totalPossible > 0) {
            (totalCompleted.toDouble() / totalPossible) * 100
        } else {
            0.0
        }
    }
    
    /**
     * Gets today's completion percentage
     */
    fun getTodayCompletionPercentage(habits: List<com.example.wellnessapp.model.Habit>): Double {
        return getCompletionPercentage(DateUtils.getTodayString(), habits)
    }
    
    /**
     * Gets completion data for the last N days
     */
    fun getCompletionsForLastDays(days: Int): Map<String, Map<String, Int>> {
        val allCompletions = getAllCompletions()
        val lastDays = com.example.wellnessapp.util.DateUtils.getLastNDays(days)
        
        return allCompletions.filterKeys { dateString ->
            lastDays.contains(dateString)
        }
    }
    
    /**
     * Clears all completion data
     */
    fun clearAllCompletions() {
        prefsManager.remove(PrefsManager.PREF_COMPLETIONS_BY_DATE)
    }
    
    /**
     * Clears completion data for a specific date
     */
    fun clearCompletionsForDate(dateString: String) {
        val allCompletions = getAllCompletions().toMutableMap()
        allCompletions.remove(dateString)
        saveAllCompletions(allCompletions)
    }
    
    /**
     * Saves all completion data to preferences
     */
    private fun saveAllCompletions(completions: Map<String, Map<String, Int>>) {
        prefsManager.setJsonMap(PrefsManager.PREF_COMPLETIONS_BY_DATE, completions)
    }
}
