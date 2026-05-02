package com.example.wellnessapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.wellnessapp.R
import com.example.wellnessapp.model.Habit

/**
 * Utility class for managing notifications and notification channels.
 */
object NotificationUtils {
    
    const val CHANNEL_ID = "wellness_reminders"
    const val CHANNEL_NAME = "Wellness Reminders"
    const val CHANNEL_DESCRIPTION = "Notifications for hydration reminders and wellness tips"
    
    const val NOTIFICATION_ID_HYDRATION = 1001
    const val NOTIFICATION_ID_HABIT = 1002
    const val ACTION_MARK_GLASS_DONE = "com.example.wellnessapp.MARK_GLASS_DONE"
    const val ACTION_OPEN_HABITS = "com.example.wellnessapp.OPEN_HABITS"
    
    /**
     * Creates the notification channel for the app
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Shows a hydration reminder notification
     */
    fun showHydrationReminder(context: Context) {
        // Create intent for opening the app
        val intent = Intent(context, com.example.wellnessapp.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fragment", "habits")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create intent for marking a glass as done
        val markDoneIntent = Intent(ACTION_MARK_GLASS_DONE).apply {
            setPackage(context.packageName)
        }
        
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to Hydrate! 💧")
            .setContentText("Stay healthy and drink some water")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Mark 1 Glass Done",
                markDonePendingIntent
            )
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Remember to stay hydrated throughout the day. Your body will thank you!"))
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_HYDRATION, notification)
        }
    }
    
    /**
     * Cancels the hydration reminder notification
     */
    fun cancelHydrationReminder(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID_HYDRATION)
    }
    
    /**
     * Shows a habit reminder notification
     */
    fun showHabitReminder(context: Context, habits: List<Habit>) {
        val intent = Intent(context, com.example.wellnessapp.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fragment", "habits")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create intent for opening habits
        val openHabitsIntent = Intent(ACTION_OPEN_HABITS).apply {
            setPackage(context.packageName)
        }
        
        val openHabitsPendingIntent = PendingIntent.getBroadcast(
            context,
            4,
            openHabitsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create habit list text
        val habitText = if (habits.size <= 3) {
            habits.joinToString(", ") { it.title }
        } else {
            habits.take(3).joinToString(", ") + " and ${habits.size - 3} more"
        }
        
        val title = when {
            habits.size == 1 -> "Complete Your Habit! 🎯"
            habits.size <= 3 -> "Time for Your Habits! 📝"
            else -> "Don't Forget Your Habits! ⭐"
        }
        
        val contentText = if (habits.size == 1) {
            "Time to complete: ${habits.first().title}"
        } else {
            "Complete your daily habits: $habitText"
        }
        
        val bigText = if (habits.size == 1) {
            "Stay consistent with your habit: ${habits.first().title}. Every small step counts towards your goals!"
        } else {
            "You have ${habits.size} habits to complete today:\n\n" +
            habits.joinToString("\n") { "• ${it.title}" } +
            "\n\nKeep up the great work! Consistency is key to building lasting habits."
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Open Habits",
                openHabitsPendingIntent
            )
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(bigText))
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_HABIT, notification)
        }
    }
    
    /**
     * Cancels the habit reminder notification
     */
    fun cancelHabitReminder(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID_HABIT)
    }
    
    /**
     * Shows a general wellness tip notification
     */
    fun showWellnessTip(context: Context, title: String, message: String) {
        val intent = Intent(context, com.example.wellnessapp.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
