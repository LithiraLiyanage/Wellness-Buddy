package com.example.wellnessapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wellnessapp.data.CompletionRepository
import com.example.wellnessapp.data.HabitsRepository
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.util.DateUtils
import com.example.wellnessapp.util.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager worker that handles hydration reminders.
 * Schedules periodic notifications and manages hydration tracking.
 */
class HydrationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val prefsManager = PrefsManager(context)
    private val habitsRepository = HabitsRepository(prefsManager)
    private val completionRepository = CompletionRepository(prefsManager)
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check if hydration reminders are enabled
            val isEnabled = prefsManager.getBoolean(PrefsManager.PREF_HYDRATION_ENABLED, false)
            
            if (!isEnabled) {
                // If disabled, cancel any existing notifications
                NotificationUtils.cancelHydrationReminder(applicationContext)
                return@withContext Result.success()
            }
            
            // Show hydration reminder notification
            NotificationUtils.showHydrationReminder(applicationContext)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    companion object {
        const val WORK_NAME = "hydration_reminder_work"
        
        /**
         * Schedules or cancels hydration reminders based on settings
         */
        suspend fun scheduleHydrationReminders(
            context: Context,
            enabled: Boolean,
            intervalMinutes: Int
        ) {
            val workManager = androidx.work.WorkManager.getInstance(context)
            
            // Cancel existing work
            workManager.cancelUniqueWork(WORK_NAME)
            
            if (enabled && intervalMinutes > 0) {
                // Create periodic work request
                val constraints = androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                    .build()
                
                val hydrationWork = androidx.work.PeriodicWorkRequestBuilder<HydrationWorker>(
                    java.time.Duration.ofMinutes(intervalMinutes.toLong())
                )
                    .setConstraints(constraints)
                    .setInitialDelay(
                        java.time.Duration.ofMinutes(intervalMinutes.toLong())
                    )
                    .build()
                
                // Enqueue the work
                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                    hydrationWork
                )
            }
        }
        
        /**
         * Handles the "Mark 1 Glass Done" action from notification
         */
        fun handleMarkGlassDone(context: Context) {
            val prefsManager = PrefsManager(context)
            val habitsRepository = HabitsRepository(prefsManager)
            val completionRepository = CompletionRepository(prefsManager)
            
            // Find or create "Drink Water" habit
            val waterHabit = habitsRepository.getAllHabits()
                .find { it.title.equals("Drink Water", ignoreCase = true) }
                ?: habitsRepository.addHabit(
                    com.example.wellnessapp.model.Habit(
                        id = "",
                        title = "Drink Water",
                        targetPerDay = 8, // Default to 8 glasses per day
                        sortOrder = habitsRepository.getNextSortOrder()
                    )
                )
            
            // Increment today's completion count
            completionRepository.incrementCompletion(
                waterHabit.id,
                DateUtils.getTodayString()
            )
            
            // Cancel the current notification
            NotificationUtils.cancelHydrationReminder(context)
        }
    }
}
