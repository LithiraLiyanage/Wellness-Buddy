package com.example.wellnessapp.data

import com.example.wellnessapp.model.Habit
import java.util.UUID

/**
 * Repository for managing habits data.
 * Handles CRUD operations for habits using SharedPreferences.
 */
class HabitsRepository(private val prefsManager: PrefsManager) {
    
    /**
     * Gets all habits
     */
    fun getAllHabits(): List<Habit> {
        return prefsManager.getJsonList<Habit>(PrefsManager.PREF_HABITS)
            .sortedBy { it.sortOrder }
    }
    
    /**
     * Gets a habit by ID
     */
    fun getHabitById(id: String): Habit? {
        return getAllHabits().find { it.id == id }
    }
    
    /**
     * Adds a new habit
     */
    fun addHabit(habit: Habit): Habit {
        val habits = getAllHabits().toMutableList()
        val newHabit = habit.copy(
            id = habit.id.ifEmpty { UUID.randomUUID().toString() },
            sortOrder = habits.size
        )
        habits.add(newHabit)
        saveHabits(habits)
        return newHabit
    }
    
    /**
     * Updates an existing habit
     */
    fun updateHabit(habit: Habit): Boolean {
        val habits = getAllHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        return if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
            true
        } else {
            false
        }
    }
    
    /**
     * Deletes a habit by ID
     */
    fun deleteHabit(id: String): Boolean {
        val habits = getAllHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == id }
        return if (index != -1) {
            habits.removeAt(index)
            // Reorder remaining habits
            habits.forEachIndexed { i, habit ->
                if (habit.sortOrder != i) {
                    habits[i] = habit.copy(sortOrder = i)
                }
            }
            saveHabits(habits)
            true
        } else {
            false
        }
    }
    
    /**
     * Reorders habits
     */
    fun reorderHabits(habitIds: List<String>): Boolean {
        val habits = getAllHabits().toMutableList()
        val reorderedHabits = mutableListOf<Habit>()
        
        // Add habits in the new order
        habitIds.forEach { id ->
            val habit = habits.find { it.id == id }
            if (habit != null) {
                reorderedHabits.add(habit.copy(sortOrder = reorderedHabits.size))
            }
        }
        
        // Add any remaining habits that weren't in the reorder list
        habits.forEach { habit ->
            if (!habitIds.contains(habit.id)) {
                reorderedHabits.add(habit.copy(sortOrder = reorderedHabits.size))
            }
        }
        
        saveHabits(reorderedHabits)
        return true
    }
    
    /**
     * Gets the next available sort order
     */
    fun getNextSortOrder(): Int {
        return getAllHabits().size
    }
    
    /**
     * Checks if a habit with the given title already exists
     */
    fun habitExists(title: String, excludeId: String? = null): Boolean {
        return getAllHabits().any { 
            it.title.equals(title, ignoreCase = true) && it.id != excludeId 
        }
    }
    
    /**
     * Saves the habits list to preferences
     */
    private fun saveHabits(habits: List<Habit>) {
        prefsManager.setJsonList(PrefsManager.PREF_HABITS, habits)
    }
}
