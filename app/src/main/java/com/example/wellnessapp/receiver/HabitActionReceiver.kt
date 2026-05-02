package com.example.wellnessapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wellnessapp.MainActivity
import com.example.wellnessapp.util.NotificationUtils

/**
 * BroadcastReceiver that handles actions from habit reminder notifications.
 */
class HabitActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationUtils.ACTION_OPEN_HABITS -> {
                // Open the app to the habits fragment
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("fragment", "habits")
                }
                context.startActivity(mainIntent)
            }
        }
    }
}
