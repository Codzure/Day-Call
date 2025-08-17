package com.codzuregroup.daycall.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.LifecycleService
import com.codzuregroup.daycall.MainActivity
import com.codzuregroup.daycall.R
import com.codzuregroup.daycall.ui.todo.TodoItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NextAlarmForegroundService : LifecycleService() {
    companion object {
        const val CHANNEL_ID = "next_alarm_channel"
        const val NOTIFICATION_ID = 1002
        const val ACTION_UPDATE = "com.codzuregroup.daycall.NEXT_ALARM_UPDATE"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TIME = "time"
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannel()
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Next alarm"
        val timeText = intent?.getStringExtra(EXTRA_TIME) ?: "--:--"
        startForeground(NOTIFICATION_ID, buildNotification(title, timeText))
        return START_STICKY
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "Next Alarm",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows the next upcoming alarm"
                    enableVibration(false)
                    enableLights(false)
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                mgr.createNotificationChannel(ch)
            }
        }
    }

    private fun buildNotification(title: String, timeText: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            // Use a proper status-bar small icon (monochrome/vector) instead of a full-color bitmap
            .setSmallIcon(R.drawable.ic_alarm)
            .setColor(Color.parseColor("#4F46E5"))
            .setContentTitle(title)
            .setContentText(timeText)
            .setContentIntent(pi)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            // For modern Android, indicate this is for an active foreground service
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        // Avoid bubble metadata on the foreground service notification to prevent validation issues
        return builder.build()
    }
}

