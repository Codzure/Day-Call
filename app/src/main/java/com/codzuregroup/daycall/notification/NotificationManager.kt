package com.codzuregroup.daycall.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.codzuregroup.daycall.MainActivity
import com.codzuregroup.daycall.R
import com.codzuregroup.daycall.data.AlarmEntity
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.util.Log

class AlarmNotificationManager(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "alarm_reminder_channel"
        const val CHANNEL_NAME = "Alarm Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for upcoming alarms"
        const val NOTIFICATION_ID_PREFIX = 1000
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleReminderNotification(alarm: AlarmEntity) {
        val reminderTime = getReminderTime(alarm)
        Log.d("AlarmNotificationManager", "Attempting to show notification for alarm ${alarm.id}")
        Log.d("AlarmNotificationManager", "Reminder time: $reminderTime, Current time: ${LocalDateTime.now()}")
        
        if (reminderTime.isAfter(LocalDateTime.now())) {
            Log.d("AlarmNotificationManager", "Reminder time is in the future, scheduling notification")
            val notificationId = NOTIFICATION_ID_PREFIX + alarm.id.hashCode()
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val formattedTime = LocalTime.of(alarm.hour, alarm.minute).format(timeFormatter)
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Alarm Reminder")
                .setContentText("Your alarm \"${alarm.label}\" will ring at $formattedTime")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Your alarm \"${alarm.label}\" will ring at $formattedTime\n\nTime to start your morning routine! 🌅"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            
            notificationManager.notify(notificationId, notification)
            Log.d("AlarmNotificationManager", "Notification sent successfully for alarm ${alarm.id}")
        } else {
            Log.d("AlarmNotificationManager", "Reminder time has already passed, not showing notification")
        }
    }
    
    fun cancelReminderNotification(alarm: AlarmEntity) {
        val notificationId = NOTIFICATION_ID_PREFIX + alarm.id.hashCode()
        notificationManager.cancel(notificationId)
    }
    
    private fun getReminderTime(alarm: AlarmEntity): LocalDateTime {
        val alarmTime = LocalTime.of(alarm.hour, alarm.minute)
        val currentDate = LocalDateTime.now().toLocalDate()
        val alarmDateTime = LocalDateTime.of(currentDate, alarmTime)
        
        return if (alarmDateTime.isAfter(LocalDateTime.now())) {
            alarmDateTime.minusMinutes(15)
        } else {
            alarmDateTime.plusDays(1).minusMinutes(15)
        }
    }
    
    fun scheduleAllReminderNotifications(alarms: List<AlarmEntity>) {
        alarms.forEach { alarm ->
            if (alarm.enabled) {
                scheduleReminderNotification(alarm)
            }
        }
    }
    
    fun cancelAllReminderNotifications() {
        notificationManager.cancelAll()
    }
} 