package com.example.wellnessapp.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for date operations throughout the app.
 */
object DateUtils {
    
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val displayDateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val displayTimeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    /**
     * Returns today's date in YYYY-MM-DD format
     */
    fun getTodayString(): String {
        return dateFormatter.format(Date())
    }
    
    /**
     * Returns the current timestamp
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * Formats a timestamp to YYYY-MM-DD string
     */
    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }
    
    /**
     * Formats a timestamp to HH:mm string
     */
    fun formatTime(timestamp: Long): String {
        return timeFormatter.format(Date(timestamp))
    }
    
    /**
     * Formats a timestamp to a display-friendly date string (e.g., "Jan 15, 2024")
     */
    fun formatDisplayDate(timestamp: Long): String {
        return displayDateFormatter.format(Date(timestamp))
    }
    
    /**
     * Formats a timestamp to a display-friendly time string (e.g., "2:30 PM")
     */
    fun formatDisplayTime(timestamp: Long): String {
        return displayTimeFormatter.format(Date(timestamp))
    }
    
    /**
     * Parses a date string in YYYY-MM-DD format to a Date object
     */
    fun parseDate(dateString: String): Date? {
        return try {
            dateFormatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Returns the date string for N days ago from today
     */
    fun getDaysAgoString(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormatter.format(calendar.time)
    }
    
    /**
     * Returns a list of the last N days in YYYY-MM-DD format
     */
    fun getLastNDays(n: Int): List<String> {
        val days = mutableListOf<String>()
        for (i in 0 until n) {
            days.add(getDaysAgoString(i))
        }
        return days.reversed() // Return in chronological order
    }
    
    /**
     * Returns the start of day timestamp for a given date string
     */
    fun getStartOfDay(dateString: String): Long {
        val date = parseDate(dateString) ?: return 0L
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    /**
     * Returns the end of day timestamp for a given date string
     */
    fun getEndOfDay(dateString: String): Long {
        val date = parseDate(dateString) ?: return 0L
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
