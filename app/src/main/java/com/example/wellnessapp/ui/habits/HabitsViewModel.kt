package com.example.wellnessapp.ui.habits

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.CompletionRepository
import com.example.wellnessapp.data.HabitsRepository
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.model.Habit
import com.example.wellnessapp.model.HabitCategory
import com.example.wellnessapp.util.DateUtils
import com.example.wellnessapp.widget.WellnessWidgetProvider
import kotlinx.coroutines.launch

/**
 * ViewModel for the HabitsFragment.
 * Manages habit data and completion tracking.
 */
class HabitsViewModel : ViewModel() {
    
    private val prefsManager = PrefsManager(com.example.wellnessapp.WellnessApplication.instance)
    private val habitsRepository = HabitsRepository(prefsManager)
    private val completionRepository = CompletionRepository(prefsManager)
    
    private val _habits = MutableLiveData<List<Habit>>()
    val habits: LiveData<List<Habit>> = _habits
    
    private val _completionPercentage = MutableLiveData<Double>()
    val completionPercentage: LiveData<Double> = _completionPercentage
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        loadHabits()
    }
    
    /**
     * Loads all habits and calculates completion percentage
     */
    fun loadHabits() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val habitsList = habitsRepository.getAllHabits()
                _habits.value = habitsList
                updateCompletionPercentage(habitsList)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load habits: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Adds a new habit
     */
    fun addHabit(title: String, targetPerDay: Int?, category: HabitCategory = HabitCategory.OTHER) {
        viewModelScope.launch {
            try {
                val habit = Habit(
                    id = "",
                    title = title,
                    targetPerDay = targetPerDay,
                    sortOrder = habitsRepository.getNextSortOrder(),
                    category = category
                )
                
                if (habitsRepository.habitExists(title)) {
                    _errorMessage.value = "A habit with this name already exists"
                    return@launch
                }
                
                habitsRepository.addHabit(habit)
                loadHabits()
                updateWidget()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add habit: ${e.message}"
            }
        }
    }
    
    /**
     * Updates an existing habit
     */
    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                if (habitsRepository.habitExists(habit.title, habit.id)) {
                    _errorMessage.value = "A habit with this name already exists"
                    return@launch
                }
                
                habitsRepository.updateHabit(habit)
                loadHabits()
                updateWidget()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update habit: ${e.message}"
            }
        }
    }
    
    /**
     * Deletes a habit
     */
    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            try {
                habitsRepository.deleteHabit(habitId)
                loadHabits()
                updateWidget()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete habit: ${e.message}"
            }
        }
    }
    
    /**
     * Toggles completion for a boolean habit
     */
    fun toggleHabitCompletion(habitId: String) {
        viewModelScope.launch {
            try {
                val today = DateUtils.getTodayString()
                completionRepository.toggleBooleanCompletion(habitId, today)
                loadHabits()
                updateWidget()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update completion: ${e.message}"
            }
        }
    }
    
    /**
     * Increments completion count for a countable habit
     */
    fun incrementHabitCompletion(habitId: String) {
        viewModelScope.launch {
            try {
                val today = DateUtils.getTodayString()
                completionRepository.incrementCompletion(habitId, today)
                loadHabits()
                updateWidget()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to increment completion: ${e.message}"
            }
        }
    }
    
    /**
     * Decrements completion count for a countable habit
     */
    fun decrementHabitCompletion(habitId: String) {
        viewModelScope.launch {
            try {
                val today = DateUtils.getTodayString()
                completionRepository.decrementCompletion(habitId, today)
                loadHabits()
                updateWidget()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to decrement completion: ${e.message}"
            }
        }
    }
    
    /**
     * Gets completion count for a habit today
     */
    fun getTodayCompletionCount(habitId: String): Int {
        return completionRepository.getTodayCompletionCount(habitId)
    }
    
    /**
     * Checks if a habit is completed today
     */
    fun isHabitCompletedToday(habitId: String): Boolean {
        return completionRepository.isCompletedToday(habitId)
    }
    
    /**
     * Checks if a countable habit is fully completed (reached target)
     */
    fun isCountableHabitFullyCompleted(habit: Habit): Boolean {
        if (!habit.isCountableHabit()) return false
        val todayCount = getTodayCompletionCount(habit.id)
        val target = habit.targetPerDay ?: 1
        return todayCount >= target
    }
    
    /**
     * Updates the completion percentage
     */
    private fun updateCompletionPercentage(habits: List<Habit>) {
        val percentage = completionRepository.getTodayCompletionPercentage(habits)
        _completionPercentage.value = percentage
    }
    
    /**
     * Updates the widget
     */
    private fun updateWidget() {
        WellnessWidgetProvider.triggerWidgetUpdate(com.example.wellnessapp.WellnessApplication.instance)
    }
    
    /**
     * Clears error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
