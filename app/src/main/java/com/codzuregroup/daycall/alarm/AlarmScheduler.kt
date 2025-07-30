package com.codzuregroup.daycall.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.codzuregroup.daycall.data.AlarmEntity
import com.codzuregroup.daycall.notification.ReminderScheduler
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val reminderScheduler = ReminderScheduler(context)

    fun scheduleAlarm(alarm: AlarmEntity) {
        Log.d("AlarmScheduler", "Scheduling alarm: ${alarm.id}, enabled: ${alarm.enabled}, time: ${alarm.hour}:${alarm.minute}")
        
        if (!alarm.enabled) {
            cancelAlarm(alarm)
            return
        }

        // Check if exact alarms are allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("AlarmScheduler", "Exact alarms not allowed. Please grant permission in settings.")
                return
            }
        }

        val alarmTime = LocalTime.of(alarm.hour, alarm.minute)
        val now = LocalDateTime.now()
        var targetTime = now.with(alarmTime)

        // If the alarm time has passed today, schedule for tomorrow
        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1)
            Log.d("AlarmScheduler", "Alarm time has passed today, scheduling for tomorrow: $targetTime")
        }

        Log.d("AlarmScheduler", "Target time: $targetTime, Repeat days: ${alarm.repeatDays}")

        // Handle repeat days
        if (alarm.repeatDays > 0) {
            scheduleRepeatingAlarm(alarm, targetTime)
        } else {
            scheduleOneTimeAlarm(alarm, targetTime)
        }
        
        // Schedule reminder notification 15 minutes before alarm
        reminderScheduler.scheduleReminderNotification(alarm)
    }

    private fun scheduleOneTimeAlarm(alarm: AlarmEntity, targetTime: LocalDateTime) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ALARM_TRIGGER"
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("AUDIO_FILE", alarm.audioFile)
            putExtra("CHALLENGE_TYPE", alarm.challengeType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = targetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        Log.d("AlarmScheduler", "Setting one-time alarm for ${alarm.label} at ${targetTime}, trigger time: $triggerTime")
        
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Successfully scheduled one-time alarm: ${alarm.id}")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Failed to schedule one-time alarm: ${alarm.id}", e)
        }
    }

    private fun scheduleRepeatingAlarm(alarm: AlarmEntity, targetTime: LocalDateTime) {
        val repeatDays = getRepeatDaysSet(alarm.repeatDays)
        
        Log.d("AlarmScheduler", "Scheduling repeating alarm for days: $repeatDays")
        
        repeatDays.forEach { dayOfWeek ->
            val nextAlarmTime = getNextAlarmTime(targetTime, dayOfWeek)
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "ALARM_TRIGGER"
                putExtra("ALARM_ID", alarm.id)
                putExtra("ALARM_LABEL", alarm.label)
                putExtra("AUDIO_FILE", alarm.audioFile)
                putExtra("CHALLENGE_TYPE", alarm.challengeType)
                putExtra("REPEAT_DAY", dayOfWeek.value)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (alarm.id * 10 + dayOfWeek.value).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = nextAlarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            Log.d("AlarmScheduler", "Setting repeating alarm for ${alarm.label} on ${dayOfWeek} at ${nextAlarmTime}, trigger time: $triggerTime")
            
            try {
                // For repeating alarms, we need to set them individually for each day
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d("AlarmScheduler", "Successfully scheduled repeating alarm: ${alarm.id} for ${dayOfWeek}")
            } catch (e: Exception) {
                Log.e("AlarmScheduler", "Failed to schedule repeating alarm: ${alarm.id} for ${dayOfWeek}", e)
            }
        }
    }

    private fun getNextAlarmTime(targetTime: LocalDateTime, dayOfWeek: DayOfWeek): LocalDateTime {
        var nextTime = targetTime
        while (nextTime.dayOfWeek != dayOfWeek) {
            nextTime = nextTime.plusDays(1)
        }
        return nextTime
    }

    private fun getRepeatDaysSet(repeatDays: Int): Set<DayOfWeek> {
        val days = mutableSetOf<DayOfWeek>()
        if (repeatDays and 0b1 != 0) days.add(DayOfWeek.MONDAY)
        if (repeatDays and 0b10 != 0) days.add(DayOfWeek.TUESDAY)
        if (repeatDays and 0b100 != 0) days.add(DayOfWeek.WEDNESDAY)
        if (repeatDays and 0b1000 != 0) days.add(DayOfWeek.THURSDAY)
        if (repeatDays and 0b10000 != 0) days.add(DayOfWeek.FRIDAY)
        if (repeatDays and 0b100000 != 0) days.add(DayOfWeek.SATURDAY)
        if (repeatDays and 0b1000000 != 0) days.add(DayOfWeek.SUNDAY)
        return days
    }

    fun cancelAlarm(alarm: AlarmEntity) {
        Log.d("AlarmScheduler", "Cancelling alarm: ${alarm.id}")
        
        // Cancel one-time alarm
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { 
            alarmManager.cancel(it)
            Log.d("AlarmScheduler", "Cancelled one-time alarm: ${alarm.id}")
        }

        // Cancel repeating alarms for each day
        if (alarm.repeatDays > 0) {
            val repeatDays = getRepeatDaysSet(alarm.repeatDays)
            repeatDays.forEach { dayOfWeek ->
                val repeatIntent = Intent(context, AlarmReceiver::class.java)
                val repeatPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (alarm.id * 10 + dayOfWeek.value).toInt(),
                    repeatIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                repeatPendingIntent?.let { 
                    alarmManager.cancel(it)
                    Log.d("AlarmScheduler", "Cancelled repeating alarm: ${alarm.id} for ${dayOfWeek}")
                }
            }
        }
        
        // Cancel reminder notification
        reminderScheduler.cancelReminderNotification(alarm)
    }

    fun cancelAllAlarms() {
        // This would need to be implemented based on your alarm list
        // For now, we'll just cancel known alarms
        Log.d("AlarmScheduler", "Cancelling all alarms")
        reminderScheduler.cancelAllReminderNotifications()
    }
} 