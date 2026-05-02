package com.example.wellnessapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wellnessapp.util.NotificationUtils
import com.example.wellnessapp.worker.HydrationWorker

/**
 * BroadcastReceiver that handles actions from hydration notifications.
 */
class HydrationActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationUtils.ACTION_MARK_GLASS_DONE -> {
                HydrationWorker.handleMarkGlassDone(context)
            }
        }
    }
}
