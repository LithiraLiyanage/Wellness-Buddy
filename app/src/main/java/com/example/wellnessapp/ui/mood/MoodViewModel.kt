package com.example.wellnessapp.ui.mood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.MoodRepository
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.model.MoodEntry
import com.example.wellnessapp.util.DateUtils
import com.example.wellnessapp.util.EmojiUtils
import kotlinx.coroutines.launch

/**
 * ViewModel for the MoodFragment.
 * Manages mood entries and chart data.
 */
class MoodViewModel : ViewModel() {
    
    private val prefsManager = PrefsManager(com.example.wellnessapp.WellnessApplication.instance)
    private val moodRepository = MoodRepository(prefsManager)
    
    private val _moodEntries = MutableLiveData<List<MoodEntry>>()
    val moodEntries: LiveData<List<MoodEntry>> = _moodEntries
    
    private val _dailyAverages = MutableLiveData<Map<String, Double>>()
    val dailyAverages: LiveData<Map<String, Double>> = _dailyAverages
    
    private val _selectedDate = MutableLiveData<String?>()
    val selectedDate: LiveData<String?> = _selectedDate
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        loadMoodData()
    }
    
    /**
     * Loads mood entries and chart data
     */
    fun loadMoodData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                loadMoodEntries()
                loadChartData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load mood data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Loads mood entries based on selected date filter
     */
    private fun loadMoodEntries() {
        val entries = if (_selectedDate.value != null) {
            moodRepository.getMoodEntriesForDate(_selectedDate.value!!)
        } else {
            moodRepository.getAllMoodEntries()
        }
        _moodEntries.value = entries
    }
    
    /**
     * Loads chart data for the last 7 days
     */
    private fun loadChartData() {
        val averages = moodRepository.getDailyAverageMoodsForLastDays(7)
        _dailyAverages.value = averages
    }
    
    /**
     * Adds a new mood entry
     */
    fun addMoodEntry(emoji: String, note: String?, intensity: Float = 5f) {
        viewModelScope.launch {
            try {
                val moodEntry = MoodEntry(
                    id = "",
                    timestamp = DateUtils.getCurrentTimestamp(),
                    emoji = emoji,
                    note = note?.takeIf { it.isNotBlank() },
                    intensity = intensity
                )
                
                moodRepository.addMoodEntry(moodEntry)
                loadMoodData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add mood entry: ${e.message}"
            }
        }
    }
    
    /**
     * Adds a standalone note
     */
    fun addNote(note: String) {
        viewModelScope.launch {
            try {
                val moodEntry = MoodEntry(
                    id = "",
                    timestamp = DateUtils.getCurrentTimestamp(),
                    emoji = "📝",
                    note = note,
                    intensity = 5f
                )
                
                moodRepository.addMoodEntry(moodEntry)
                loadMoodData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add note: ${e.message}"
            }
        }
    }
    
    /**
     * Updates a mood entry's note
     */
    fun updateMoodEntry(moodEntryId: String, newNote: String) {
        viewModelScope.launch {
            try {
                moodRepository.updateMoodEntryNote(moodEntryId, newNote)
                loadMoodData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update mood entry: ${e.message}"
            }
        }
    }
    
    /**
     * Deletes a mood entry
     */
    fun deleteMoodEntry(moodEntryId: String) {
        viewModelScope.launch {
            try {
                moodRepository.deleteMoodEntry(moodEntryId)
                loadMoodData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete mood entry: ${e.message}"
            }
        }
    }
    
    /**
     * Filters mood entries by date
     */
    fun filterByDate(dateString: String?) {
        _selectedDate.value = dateString
        loadMoodEntries()
    }
    
    /**
     * Clears date filter
     */
    fun clearDateFilter() {
        _selectedDate.value = null
        loadMoodEntries()
    }
    
    /**
     * Generates weekly mood summary for sharing
     */
    fun generateWeeklySummary(): String {
        return moodRepository.generateWeeklyMoodSummary()
    }
    
    /**
     * Gets available emojis for selection
     */
    fun getAvailableEmojis(): List<String> {
        return EmojiUtils.ALLOWED_EMOJIS
    }
    
    /**
     * Gets emojis grouped by mood level
     */
    fun getEmojisByMoodLevel(): Map<Int, List<String>> {
        return EmojiUtils.getEmojisByMoodLevel()
    }
    
    /**
     * Clears error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
