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
import java.util.*

/**
 * ViewModel for mood analytics functionality.
 * Processes mood data to generate insights, statistics, and chart data.
 */
class MoodAnalyticsViewModel : ViewModel() {
    
    private val prefsManager = PrefsManager(com.example.wellnessapp.WellnessApplication.instance)
    private val repository = MoodRepository(prefsManager)
    
    private val _moodDistribution = MutableLiveData<Map<String, Int>>()
    val moodDistribution: LiveData<Map<String, Int>> = _moodDistribution
    
    private val _moodTrends = MutableLiveData<List<Pair<String, Float>>>()
    val moodTrends: LiveData<List<Pair<String, Float>>> = _moodTrends
    
    private val _moodStatistics = MutableLiveData<MoodStatistics>()
    val moodStatistics: LiveData<MoodStatistics> = _moodStatistics
    
    private val _moodInsights = MutableLiveData<List<String>>()
    val moodInsights: LiveData<List<String>> = _moodInsights
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadAnalytics(timePeriod: MoodAnalyticsFragment.TimePeriod) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val entries = getMoodEntriesForPeriod(timePeriod)
                
                // Calculate mood distribution
                val distribution = calculateMoodDistribution(entries)
                _moodDistribution.value = distribution
                
                // Calculate mood trends
                val trends = calculateMoodTrends(entries, timePeriod)
                _moodTrends.value = trends
                
                // Calculate statistics
                val statistics = calculateMoodStatistics(entries)
                _moodStatistics.value = statistics
                
                // Generate insights
                val insights = generateMoodInsights(entries, distribution, statistics)
                _moodInsights.value = insights
                
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun getMoodEntriesForPeriod(timePeriod: MoodAnalyticsFragment.TimePeriod): List<MoodEntry> {
        return when (timePeriod) {
            MoodAnalyticsFragment.TimePeriod.WEEK -> {
                repository.getMoodEntriesForLastDays(7)
            }
            MoodAnalyticsFragment.TimePeriod.MONTH -> {
                repository.getMoodEntriesForLastDays(30)
            }
            MoodAnalyticsFragment.TimePeriod.QUARTER -> {
                repository.getMoodEntriesForLastDays(90)
            }
        }
    }
    
    private fun calculateMoodDistribution(entries: List<MoodEntry>): Map<String, Int> {
        val distribution = mutableMapOf<String, Int>()
        
        entries.forEach { entry ->
            val moodCategory = getMoodCategory(entry.emoji)
            distribution[moodCategory] = distribution.getOrDefault(moodCategory, 0) + 1
        }
        
        return distribution
    }
    
    private fun calculateMoodTrends(entries: List<MoodEntry>, timePeriod: MoodAnalyticsFragment.TimePeriod): List<Pair<String, Float>> {
        val trends = mutableListOf<Pair<String, Float>>()
        
        // Group entries by day based on time period
        val groupedEntries = entries.groupBy { DateUtils.formatDate(it.timestamp) }
        
        groupedEntries.forEach { (period, periodEntries) ->
            val averageMood = periodEntries.map { getMoodValue(it.emoji) }.average().toFloat()
            trends.add(Pair(period, averageMood))
        }
        
        return trends.sortedBy { it.first }
    }
    
    private fun calculateMoodStatistics(entries: List<MoodEntry>): MoodStatistics {
        if (entries.isEmpty()) {
            return MoodStatistics(0f, 0, 0, 0f)
        }
        
        val moodValues = entries.map { getMoodValue(it.emoji) }
        val averageMood = moodValues.average().toFloat()
        val bestMood = moodValues.maxOrNull()?.toFloat() ?: 0f
        val currentStreak = calculateCurrentStreak(entries)
        
        return MoodStatistics(
            averageMood = averageMood,
            totalEntries = entries.size,
            currentStreak = currentStreak,
            bestMood = bestMood
        )
    }
    
    private fun generateMoodInsights(
        entries: List<MoodEntry>, 
        distribution: Map<String, Int>, 
        statistics: MoodStatistics
    ): List<String> {
        val insights = mutableListOf<String>()
        
        if (entries.isEmpty()) {
            insights.add("Start tracking your mood to get personalized insights!")
            return insights
        }
        
        // Most common mood
        val mostCommonMood = distribution.maxByOrNull { it.value }
        if (mostCommonMood != null) {
            insights.add("Your most common mood is ${mostCommonMood.key} (${mostCommonMood.value} entries)")
        }
        
        // Average mood insight
        when {
            statistics.averageMood >= 7f -> insights.add("You've been feeling great overall! Keep up the positive energy! 🌟")
            statistics.averageMood >= 5f -> insights.add("Your mood has been balanced. Consider activities that boost your happiness! 😊")
            else -> insights.add("You might benefit from mood-boosting activities. Consider talking to someone or doing something you enjoy. 💙")
        }
        
        // Streak insight
        if (statistics.currentStreak > 0) {
            insights.add("Great job! You've been tracking your mood for ${statistics.currentStreak} days in a row! 🔥")
        }
        
        // Trend insight
        if (entries.size >= 7) {
            val recentEntries = entries.takeLast(7)
            val olderEntries = entries.dropLast(7).takeLast(7)
            
            if (recentEntries.isNotEmpty() && olderEntries.isNotEmpty()) {
                val recentAvg = recentEntries.map { getMoodValue(it.emoji) }.average()
                val olderAvg = olderEntries.map { getMoodValue(it.emoji) }.average()
                
                when {
                    recentAvg > olderAvg + 0.5 -> insights.add("Your mood has been improving recently! 📈")
                    recentAvg < olderAvg - 0.5 -> insights.add("Your mood has been declining. Consider reaching out for support. 🤗")
                    else -> insights.add("Your mood has been stable recently. Consistency is key! ⚖️")
                }
            }
        }
        
        // Best mood insight
        if (statistics.bestMood >= 8f) {
            insights.add("You've experienced some amazing highs! What activities help you feel your best? ✨")
        }
        
        return insights
    }
    
    private fun getMoodCategory(emoji: String): String {
        return when (emoji) {
            "😊", "😄", "😃", "😁", "🤗", "😀", "🙂", "😇", "🥰", "😍" -> "Happy"
            "😐", "😑", "😶", "🤔", "😏", "🙃" -> "Neutral"
            "😢", "😭", "😔", "😞", "😟", "😕", "🙁", "☹️", "😣", "😖" -> "Sad"
            "😠", "😡", "🤬", "😤", "😾", "👿" -> "Angry"
            "🤩", "😎", "🤪", "😜", "😝", "🤭", "🥳", "🎉" -> "Excited"
            "😌", "😴", "🤤", "😪", "😑", "🧘", "🙏" -> "Calm"
            else -> "Other"
        }
    }
    
    private fun getMoodValue(emoji: String): Float {
        return when (emoji) {
            "😊", "😄", "😃", "😁", "🤗", "😀", "🙂", "😇", "🥰", "😍" -> 8f
            "😐", "😑", "😶", "🤔", "😏", "🙃" -> 5f
            "😢", "😭", "😔", "😞", "😟", "😕", "🙁", "☹️", "😣", "😖" -> 2f
            "😠", "😡", "🤬", "😤", "😾", "👿" -> 1f
            "🤩", "😎", "🤪", "😜", "😝", "🤭", "🥳", "🎉" -> 9f
            "😌", "😴", "🤤", "😪", "😑", "🧘", "🙏" -> 6f
            else -> 5f
        }
    }
    
    private fun calculateCurrentStreak(entries: List<MoodEntry>): Int {
        if (entries.isEmpty()) return 0
        
        val sortedEntries = entries.sortedByDescending { it.timestamp }
        val today = DateUtils.getTodayString()
        var streak = 0
        var currentDate = today
        
        for (entry in sortedEntries) {
            val entryDate = DateUtils.formatDate(entry.timestamp)
            if (entryDate == currentDate) {
                streak++
                currentDate = getPreviousDay(currentDate)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun getPreviousDay(dateString: String): String {
        val date = DateUtils.parseDate(dateString) ?: return dateString
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return DateUtils.formatDate(calendar.timeInMillis)
    }
}
