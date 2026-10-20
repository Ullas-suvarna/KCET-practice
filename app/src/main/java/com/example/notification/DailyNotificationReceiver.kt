package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        if (action == DailyNotificationManager.ACTION_DAILY_STUDY_UPDATE) {
            val title = "📚 Today's Study Update & New Materials"
            val message = "New study notes, formula sheets & mock questions have been updated! Tap to review today's preparation material."
            
            DailyNotificationManager.showSystemNotification(
                context = context,
                title = title,
                message = message,
                channelId = DailyNotificationManager.CHANNEL_STUDY_UPDATES
            )
        } else if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Phone restarted - re-schedule the daily alarm
            DailyNotificationManager.scheduleDailyDigest(context)
        }
    }
}
