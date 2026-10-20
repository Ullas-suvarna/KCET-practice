package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object DailyNotificationManager {

    const val CHANNEL_STUDY_UPDATES = "study_updates_channel"
    const val CHANNEL_INSTANT_ALERTS = "instant_alerts_channel"

    const val ACTION_DAILY_STUDY_UPDATE = "com.example.ACTION_DAILY_STUDY_UPDATE"
    const val REQUEST_CODE_DAILY_ALARM = 1001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameUpdates = "Daily Study Updates & Materials"
            val descUpdates = "Notifications for newly uploaded study materials, mock tests, and daily preparation updates."
            val importanceUpdates = NotificationManager.IMPORTANCE_HIGH
            val channelUpdates = NotificationChannel(CHANNEL_STUDY_UPDATES, nameUpdates, importanceUpdates).apply {
                description = descUpdates
                enableVibration(true)
            }

            val nameAlerts = "Instant Announcements & Alerts"
            val descAlerts = "Urgent exam notices, counseling dates, and important alerts."
            val importanceAlerts = NotificationManager.IMPORTANCE_HIGH
            val channelAlerts = NotificationChannel(CHANNEL_INSTANT_ALERTS, nameAlerts, importanceAlerts).apply {
                description = descAlerts
                enableVibration(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelUpdates)
            notificationManager.createNotificationChannel(channelAlerts)
        }
    }

    fun showSystemNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_STUDY_UPDATES,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 150, 250))

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            android.util.Log.e("DailyNotificationManager", "Notification permission not granted: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("DailyNotificationManager", "Error showing notification: ${e.message}", e)
        }
    }

    fun scheduleDailyDigest(context: Context, hourOfDay: Int = 9, minute: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, DailyNotificationReceiver::class.java).apply {
            action = ACTION_DAILY_STUDY_UPDATE
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY_ALARM,
            intent,
            pendingIntentFlags
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            android.util.Log.i("DailyNotificationManager", "Scheduled daily study update alarm at $hourOfDay:$minute")
        } catch (e: Exception) {
            android.util.Log.e("DailyNotificationManager", "Failed to schedule alarm: ${e.message}", e)
        }
    }
}
