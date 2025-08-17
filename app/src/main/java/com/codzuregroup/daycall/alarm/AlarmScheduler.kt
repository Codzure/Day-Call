package com.codzuregroup.daycall.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
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

        // Check and request necessary permissions, but continue scheduling even if not all are granted
        val permissionsGranted = checkAndRequestPermissions()
        if (!permissionsGranted) {
            Log.w("AlarmScheduler", "Not all permissions granted, but continuing with alarm scheduling")
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
            putExtra("SOUND", alarm.sound)
            putExtra("CHALLENGE_TYPE", alarm.challengeType)
            putExtra("VIBE", alarm.vibe)
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
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    // Android 12+ - Use setAlarmClock for highest priority
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(
                        triggerTime,
                        pendingIntent
                    )
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    // Android 6+ - Use setExactAndAllowWhileIdle to bypass doze mode
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                else -> {
                    // Older versions
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
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
                putExtra("SOUND", alarm.sound)
                putExtra("CHALLENGE_TYPE", alarm.challengeType)
                putExtra("VIBE", alarm.vibe)
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
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        // Android 12+ - Use setAlarmClock for highest priority
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(
                            triggerTime,
                            pendingIntent
                        )
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                        // Android 6+ - Use setExactAndAllowWhileIdle to bypass doze mode
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                    else -> {
                        // Older versions
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                }
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
    
    fun rescheduleAllAlarms() {
        Log.d("AlarmScheduler", "Rescheduling all alarms")
        // This would typically be called after boot or when permissions are granted
        // Implementation would depend on your alarm repository
    }
    
    private fun checkAndRequestPermissions(): Boolean {
        var allPermissionsGranted = true
        
        // Check exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmScheduler", "Exact alarms permission not granted")
                requestExactAlarmPermission()
                allPermissionsGranted = false
            }
        }
        
        // Check battery optimization
        if (!isBatteryOptimizationDisabled()) {
            Log.w("AlarmScheduler", "Battery optimization not disabled")
            requestBatteryOptimizationExemption()
            allPermissionsGranted = false
        }
        
        return allPermissionsGranted
    }
    
    private fun isBatteryOptimizationDisabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Not applicable for older versions
        }
    }
    
    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d("AlarmScheduler", "Requested exact alarm permission")
            } catch (e: Exception) {
                Log.e("AlarmScheduler", "Failed to request exact alarm permission", e)
            }
        }
    }
    
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d("AlarmScheduler", "Requested battery optimization exemption")
            } catch (e: Exception) {
                Log.e("AlarmScheduler", "Failed to request battery optimization exemption", e)
            }
        }
    }
    
    fun checkAlarmPermissions(): AlarmPermissionStatus {
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        
        val batteryOptimizationDisabled = isBatteryOptimizationDisabled()
        
        return AlarmPermissionStatus(
            exactAlarmGranted = exactAlarmGranted,
            batteryOptimizationDisabled = batteryOptimizationDisabled
        )
    }
}

data class AlarmPermissionStatus(
    val exactAlarmGranted: Boolean,
    val batteryOptimizationDisabled: Boolean
) {
    val allPermissionsGranted: Boolean
        get() = exactAlarmGranted && batteryOptimizationDisabled
} 