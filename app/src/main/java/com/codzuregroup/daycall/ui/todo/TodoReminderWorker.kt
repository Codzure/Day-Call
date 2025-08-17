package com.codzuregroup.daycall.ui.todo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.codzuregroup.daycall.R

class TodoReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val todoId = inputData.getLong("todo_id", -1)
        val title = inputData.getString("title") ?: "Task Reminder"
        val description = inputData.getString("description") ?: "You have a scheduled task."
        if (todoId == -1L) return Result.failure()

        // Ensure notification channel exists
        com.codzuregroup.daycall.notification.AlarmNotificationManager(applicationContext)

        val notification = NotificationCompat.Builder(applicationContext, com.codzuregroup.daycall.notification.AlarmNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(2000 + todoId.toInt(), notification)
        }
        return Result.success()
    }
}

