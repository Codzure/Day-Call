package com.codzuregroup.daycall.notification

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.codzuregroup.daycall.data.AlarmEntity
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import android.util.Log

class ReminderScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    fun scheduleReminderNotification(alarm: AlarmEntity) {
        val reminderTime = getReminderTime(alarm)
        val now = LocalDateTime.now()
        
        Log.d("ReminderScheduler", "Scheduling reminder for alarm ${alarm.id} (${alarm.hour}:${alarm.minute})")
        Log.d("ReminderScheduler", "Reminder time: $reminderTime, Current time: $now")
        
        if (reminderTime.isAfter(now)) {
            val delay = Duration.between(now, reminderTime)
            Log.d("ReminderScheduler", "Delay: ${delay.toMinutes()} minutes")
            
            val inputData = Data.Builder()
                .putLong("alarm_id", alarm.id.toLong())
                .build()
            
            val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(inputData)
                .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                .addTag("reminder_${alarm.id}")
                .build()
            
            workManager.enqueue(reminderWork)
            Log.d("ReminderScheduler", "Reminder scheduled successfully for alarm ${alarm.id}")
        } else {
            Log.d("ReminderScheduler", "Reminder time has already passed for alarm ${alarm.id}")
        }
    }
    
    fun cancelReminderNotification(alarm: AlarmEntity) {
        val tag = "reminder_${alarm.id}"
        workManager.cancelAllWorkByTag(tag)
        Log.d("ReminderScheduler", "Cancelled reminder work with tag: $tag")
    }
    
    fun scheduleAllReminderNotifications(alarms: List<AlarmEntity>) {
        alarms.forEach { alarm ->
            if (alarm.enabled) {
                scheduleReminderNotification(alarm)
            }
        }
    }
    
    fun cancelAllReminderNotifications() {
        workManager.cancelAllWork()
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
} 