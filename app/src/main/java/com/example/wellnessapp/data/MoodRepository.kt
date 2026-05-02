package com.example.wellnessapp.data

import com.example.wellnessapp.model.MoodEntry
import com.example.wellnessapp.util.DateUtils
import com.example.wellnessapp.util.EmojiUtils
import java.util.UUID

/**
 * Repository for managing mood entries data.
 * Handles CRUD operations for mood entries and provides analytics.
 */
class MoodRepository(private val prefsManager: PrefsManager) {
    
    /**
     * Gets all mood entries
     */
    fun getAllMoodEntries(): List<MoodEntry> {
        return prefsManager.getJsonList<MoodEntry>(PrefsManager.PREF_MOOD_ENTRIES)
            .sortedByDescending { it.timestamp }
    }
    
    /**
     * Gets a mood entry by ID
     */
    fun getMoodEntryById(id: String): MoodEntry? {
        return getAllMoodEntries().find { it.id == id }
    }
    
    /**
     * Adds a new mood entry
     */
    fun addMoodEntry(moodEntry: MoodEntry): MoodEntry {
        val entries = getAllMoodEntries().toMutableList()
        val newEntry = moodEntry.copy(
            id = moodEntry.id.ifEmpty { UUID.randomUUID().toString() }
        )
        entries.add(newEntry)
        saveMoodEntries(entries)
        return newEntry
    }
    
    /**
     * Updates an existing mood entry
     */
    fun updateMoodEntry(moodEntry: MoodEntry): Boolean {
        val entries = getAllMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == moodEntry.id }
        return if (index != -1) {
            entries[index] = moodEntry
            saveMoodEntries(entries)
            true
        } else {
            false
        }
    }
    
    /**
     * Updates a mood entry's note
     */
    fun updateMoodEntryNote(moodEntryId: String, newNote: String): Boolean {
        val entries = getAllMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == moodEntryId }
        return if (index != -1) {
            entries[index] = entries[index].copy(note = newNote.ifBlank { null })
            saveMoodEntries(entries)
            true
        } else {
            false
        }
    }
    
    /**
     * Deletes a mood entry by ID
     */
    fun deleteMoodEntry(id: String): Boolean {
        val entries = getAllMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == id }
        return if (index != -1) {
            entries.removeAt(index)
            saveMoodEntries(entries)
            true
        } else {
            false
        }
    }
    
    /**
     * Gets mood entries for a specific date
     */
    fun getMoodEntriesForDate(dateString: String): List<MoodEntry> {
        return getAllMoodEntries().filter { it.getDateString() == dateString }
    }
    
    /**
     * Gets mood entries for today
     */
    fun getTodayMoodEntries(): List<MoodEntry> {
        return getMoodEntriesForDate(DateUtils.getTodayString())
    }
    
    /**
     * Gets mood entries for the last N days
     */
    fun getMoodEntriesForLastDays(days: Int): List<MoodEntry> {
        val lastDays = DateUtils.getLastNDays(days)
        return getAllMoodEntries().filter { entry ->
            lastDays.contains(entry.getDateString())
        }
    }
    
    /**
     * Gets the average mood score for a specific date
     */
    fun getAverageMoodForDate(dateString: String): Double {
        val entries = getMoodEntriesForDate(dateString)
        if (entries.isEmpty()) return 0.0
        
        val scores = entries.map { EmojiUtils.getMoodScore(it.emoji) }
        return scores.average()
    }
    
    /**
     * Gets the average mood score for today
     */
    fun getTodayAverageMood(): Double {
        return getAverageMoodForDate(DateUtils.getTodayString())
    }
    
    /**
     * Gets daily average mood scores for the last N days
     */
    fun getDailyAverageMoodsForLastDays(days: Int): Map<String, Double> {
        val lastDays = DateUtils.getLastNDays(days)
        return lastDays.associateWith { dateString ->
            getAverageMoodForDate(dateString)
        }
    }
    
    /**
     * Gets the most recent mood entry
     */
    fun getMostRecentMoodEntry(): MoodEntry? {
        return getAllMoodEntries().firstOrNull()
    }
    
    /**
     * Gets mood entries within a date range
     */
    fun getMoodEntriesInRange(startDate: String, endDate: String): List<MoodEntry> {
        val startTimestamp = DateUtils.getStartOfDay(startDate)
        val endTimestamp = DateUtils.getEndOfDay(endDate)
        
        return getAllMoodEntries().filter { entry ->
            entry.timestamp in startTimestamp..endTimestamp
        }
    }
    
    /**
     * Generates a weekly mood summary text
     */
    fun generateWeeklyMoodSummary(): String {
        val last7Days = DateUtils.getLastNDays(7)
        val dailyAverages = getDailyAverageMoodsForLastDays(7)
        
        val summary = StringBuilder()
        summary.append("Weekly Mood Summary\n")
        summary.append("==================\n\n")
        
        last7Days.forEach { dateString ->
            val average = dailyAverages[dateString] ?: 0.0
            val date = DateUtils.parseDate(dateString)
            val displayDate = if (date != null) {
                DateUtils.formatDisplayDate(date.time)
            } else {
                dateString
            }
            
            if (average > 0) {
                val emoji = EmojiUtils.getRepresentativeEmoji(average.toInt())
                val description = EmojiUtils.getMoodDescription(average.toInt())
                summary.append("$displayDate: $emoji $description (${String.format("%.1f", average)}/5)\n")
            } else {
                summary.append("$displayDate: No mood recorded\n")
            }
        }
        
        // Calculate overall weekly average
        val validAverages = dailyAverages.values.filter { it > 0 }
        if (validAverages.isNotEmpty()) {
            val weeklyAverage = validAverages.average()
            val overallEmoji = EmojiUtils.getRepresentativeEmoji(weeklyAverage.toInt())
            val overallDescription = EmojiUtils.getMoodDescription(weeklyAverage.toInt())
            summary.append("\nOverall Weekly Average: $overallEmoji $overallDescription (${String.format("%.1f", weeklyAverage)}/5)")
        } else {
            summary.append("\nNo mood data recorded this week.")
        }
        
        return summary.toString()
    }
    
    /**
     * Gets mood statistics for analytics
     */
    fun getMoodStatistics(): Map<String, Any> {
        val allEntries = getAllMoodEntries()
        val last30Days = getMoodEntriesForLastDays(30)
        
        val scores = allEntries.map { EmojiUtils.getMoodScore(it.emoji) }
        val recentScores = last30Days.map { EmojiUtils.getMoodScore(it.emoji) }
        
        return mapOf<String, Any>(
            "totalEntries" to allEntries.size,
            "entriesLast30Days" to last30Days.size,
            "overallAverage" to if (scores.isNotEmpty()) scores.average() else 0.0,
            "recentAverage" to if (recentScores.isNotEmpty()) recentScores.average() else 0.0,
            "mostCommonMood" to (scores.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: 0),
            "moodTrend" to calculateMoodTrend()
        )
    }
    
    /**
     * Calculates mood trend (improving, declining, or stable)
     */
    private fun calculateMoodTrend(): String {
        val last14Days = getDailyAverageMoodsForLastDays(14)
        val validAverages = last14Days.values.filter { it > 0 }
        
        if (validAverages.size < 7) return "insufficient_data"
        
        val firstHalf = validAverages.take(validAverages.size / 2).average()
        val secondHalf = validAverages.takeLast(validAverages.size / 2).average()
        
        val difference = secondHalf - firstHalf
        
        return when {
            difference > 0.5 -> "improving"
            difference < -0.5 -> "declining"
            else -> "stable"
        }
    }
    
    /**
     * Clears all mood entries
     */
    fun clearAllMoodEntries() {
        prefsManager.remove(PrefsManager.PREF_MOOD_ENTRIES)
    }
    
    /**
     * Saves mood entries to preferences
     */
    private fun saveMoodEntries(entries: List<MoodEntry>) {
        prefsManager.setJsonList(PrefsManager.PREF_MOOD_ENTRIES, entries)
    }
}
