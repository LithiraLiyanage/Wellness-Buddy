package com.example.wellnessapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wellnessapp.data.HabitsRepository
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.util.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * WorkManager worker that handles habit reminders.
 * Schedules periodic notifications to remind users about their daily habits.
 */
class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val prefsManager = PrefsManager(context)
    private val habitsRepository = HabitsRepository(prefsManager)
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check if habit reminders are enabled
            val isEnabled = prefsManager.getBoolean("habit_reminders_enabled", true)
            
            if (!isEnabled) {
                // If disabled, cancel any existing notifications
                NotificationUtils.cancelHabitReminder(applicationContext)
                return@withContext Result.success()
            }
            
            // Get current habits
            val habits = habitsRepository.getAllHabits()
            
            if (habits.isEmpty()) {
                // No habits to remind about
                return@withContext Result.success()
            }
            
            // Check if it's a good time to send reminders
            if (shouldSendReminder()) {
                // Show habit reminder notification
                NotificationUtils.showHabitReminder(applicationContext, habits)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    /**
     * Determines if it's a good time to send habit reminders
     */
    private fun shouldSendReminder(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Send reminders during active hours (8 AM to 10 PM)
        return hour in 8..22
    }
    
    companion object {
        const val WORK_NAME = "habit_reminder_work"
        const val MORNING_WORK_NAME = "habit_morning_reminder_work"
        const val EVENING_WORK_NAME = "habit_evening_reminder_work"
        
        /**
         * Schedules habit reminders based on settings
         */
        suspend fun scheduleHabitReminders(
            context: Context,
            enabled: Boolean
        ) {
            val workManager = androidx.work.WorkManager.getInstance(context)
            
            // Cancel existing work
            workManager.cancelUniqueWork(WORK_NAME)
            workManager.cancelUniqueWork(MORNING_WORK_NAME)
            workManager.cancelUniqueWork(EVENING_WORK_NAME)
            
            if (enabled) {
                // Schedule morning reminder (9 AM)
                scheduleMorningReminder(context, workManager)
                
                // Schedule evening reminder (8 PM)
                scheduleEveningReminder(context, workManager)
            }
        }
        
        /**
         * Schedules morning habit reminder
         */
        private fun scheduleMorningReminder(
            context: Context,
            workManager: androidx.work.WorkManager
        ) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If it's already past 9 AM today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val delay = calendar.timeInMillis - System.currentTimeMillis()
            
            val morningWork = androidx.work.OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setInitialDelay(java.time.Duration.ofMillis(delay))
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            
            workManager.enqueueUniqueWork(
                MORNING_WORK_NAME,
                androidx.work.ExistingWorkPolicy.REPLACE,
                morningWork
            )
        }
        
        /**
         * Schedules evening habit reminder
         */
        private fun scheduleEveningReminder(
            context: Context,
            workManager: androidx.work.WorkManager
        ) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20) // 8 PM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If it's already past 8 PM today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val delay = calendar.timeInMillis - System.currentTimeMillis()
            
            val eveningWork = androidx.work.OneTimeWorkRequestBuilder<HabitReminderWorker>()
                .setInitialDelay(java.time.Duration.ofMillis(delay))
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()
            
            workManager.enqueueUniqueWork(
                EVENING_WORK_NAME,
                androidx.work.ExistingWorkPolicy.REPLACE,
                eveningWork
            )
        }
    }
}
