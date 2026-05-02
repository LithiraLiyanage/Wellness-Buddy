package com.example.wellnessapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.*
import com.example.wellnessapp.MainActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.data.CompletionRepository
import com.example.wellnessapp.data.HabitsRepository
import com.example.wellnessapp.data.PrefsManager
import com.example.wellnessapp.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * AppWidgetProvider for the Wellness Buddy widget.
 * Displays today's habit completion percentage and provides quick access to the app.
 */
class WellnessWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widget instances
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Start periodic updates when the first widget is added
        schedulePeriodicUpdates(context)
    }
    
    override fun onDisabled(context: Context) {
        // Stop periodic updates when the last widget is removed
        cancelPeriodicUpdates(context)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Handle custom actions
        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, WellnessWidgetProvider::class.java)
                )
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }
    
    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.wellnessapp.UPDATE_WIDGET"
        private const val WORK_NAME = "widget_update_work"
        
        /**
         * Updates a specific widget instance
         */
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Create RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_wellness)
            
            // Calculate completion percentage
            val completionPercentage = calculateCompletionPercentage(context)
            
            // Update widget content
            views.setTextViewText(R.id.widget_title, "Wellness Buddy")
            views.setTextViewText(
                R.id.widget_percentage,
                "${completionPercentage.toInt()}%"
            )
            views.setTextViewText(
                R.id.widget_subtitle,
                "Today's Progress"
            )
            views.setTextViewText(
                R.id.widget_last_updated,
                "Updated: ${DateUtils.formatDisplayTime(DateUtils.getCurrentTimestamp())}"
            )
            
            // Set up click intent to open the app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("fragment", "habits")
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        /**
         * Calculates today's completion percentage
         */
        private fun calculateCompletionPercentage(context: Context): Double {
            val prefsManager = PrefsManager(context)
            val habitsRepository = HabitsRepository(prefsManager)
            val completionRepository = CompletionRepository(prefsManager)
            
            val habits = habitsRepository.getAllHabits()
            return completionRepository.getTodayCompletionPercentage(habits)
        }
        
        /**
         * Schedules periodic widget updates
         */
        private fun schedulePeriodicUpdates(context: Context) {
            val workManager = WorkManager.getInstance(context)
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            
            val updateWork = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                updateWork
            )
        }
        
        /**
         * Cancels periodic widget updates
         */
        private fun cancelPeriodicUpdates(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_NAME)
        }
        
        /**
         * Manually triggers widget update
         */
        fun triggerWidgetUpdate(context: Context) {
            val intent = Intent(context, WellnessWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}

/**
 * Worker class for periodic widget updates
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            WellnessWidgetProvider.triggerWidgetUpdate(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
