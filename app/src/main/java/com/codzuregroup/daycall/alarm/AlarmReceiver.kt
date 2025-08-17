package com.codzuregroup.daycall.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.codzuregroup.daycall.data.DayCallDatabase
import com.codzuregroup.daycall.data.AlarmRepository
import com.codzuregroup.daycall.audio.AudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            "ALARM_TRIGGER" -> {
                handleAlarmTrigger(context, intent)
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_REBOOT -> {
                Log.d("AlarmReceiver", "Device boot/reboot detected, rescheduling alarms")
                rescheduleAlarms(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_RESTARTED -> {
                Log.d("AlarmReceiver", "App updated/restarted, rescheduling alarms")
                rescheduleAlarms(context)
            }
            "android.intent.action.TIME_SET",
            "android.intent.action.TIMEZONE_CHANGED",
            "android.intent.action.DATE_CHANGED" -> {
                Log.d("AlarmReceiver", "Time/timezone changed, rescheduling alarms")
                rescheduleAlarms(context)
            }
            else -> {
                Log.d("AlarmReceiver", "Unknown action: ${intent.action}")
            }
        }
    }
    
    private fun handleAlarmTrigger(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val sound = intent.getStringExtra("SOUND") ?: "Ascent Braam"
        val challengeType = intent.getStringExtra("CHALLENGE_TYPE") ?: "MATH"
        val vibe = intent.getStringExtra("VIBE") ?: "default"
        
        Log.d("AlarmReceiver", "Processing alarm: ID=$alarmId, Label=$alarmLabel, Sound=$sound, Challenge=$challengeType, Vibe=$vibe")
        
        // Validate alarm ID
        if (alarmId == -1L) {
            Log.e("AlarmReceiver", "Invalid alarm ID received: $alarmId")
            return
        }
        
        // IMMEDIATELY trigger the alarm - don't wait for database validation
        // The alarm was scheduled by the system, so it should trigger
        Log.d("AlarmReceiver", "Alarm $alarmId triggering immediately")
        
        // Acquire wake lock to ensure the device stays awake
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or 
            PowerManager.ACQUIRE_CAUSES_WAKEUP or 
            PowerManager.ON_AFTER_RELEASE,
            "DayCall:AlarmWakeLock"
        )
        
        // Acquire wake lock for 20 minutes (enough time for user to wake up and dismiss)
        wakeLock.acquire(20 * 60 * 1000L)
        
        try {
            Log.d("AlarmReceiver", "Using AlarmTriggerManager for maximum reliability")
            
            // Use the comprehensive alarm trigger manager
            val triggerManager = AlarmTriggerManager(context)
            triggerManager.triggerAlarm(alarmId, alarmLabel, sound, challengeType, vibe)
            
            // If this is a repeating alarm, schedule the next occurrence
            scheduleNextOccurrence(context, intent)
            
            Log.d("AlarmReceiver", "Alarm trigger initiated successfully")
            
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to trigger alarm", e)
            
            // Ultimate fallback: basic service and notification
            try {
                Log.w("AlarmReceiver", "Using ultimate fallback mechanism")
                startAlarmService(context, sound, vibe, alarmId, alarmLabel, challengeType)
                showFallbackNotification(context, alarmId, alarmLabel)
            } catch (fallbackException: Exception) {
                Log.e("AlarmReceiver", "Even ultimate fallback failed", fallbackException)
            }
        } finally {
            // Don't release wake lock immediately - let the service handle it
            // The wake lock will auto-release after 20 minutes if not released manually
        }
    }
    
    private fun scheduleNextOccurrence(context: Context, intent: Intent) {
        val repeatDay = intent.getIntExtra("REPEAT_DAY", -1)
        if (repeatDay != -1) {
            // This is a repeating alarm, schedule next occurrence
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarmId = intent.getLongExtra("ALARM_ID", -1)
                    val database = DayCallDatabase.getInstance(context)
                    val repository = AlarmRepository(database.alarmDao())
                    val alarmScheduler = AlarmScheduler(context)
                    
                    val alarm = repository.getAlarm(alarmId)
                    alarm?.let {
                        alarmScheduler.scheduleAlarm(it)
                        Log.d("AlarmReceiver", "Scheduled next occurrence for repeating alarm: $alarmId")
                    }
                } catch (e: Exception) {
                    Log.e("AlarmReceiver", "Failed to schedule next occurrence", e)
                }
            }
        }
    }
    
    private fun showAlarmNotification(context: Context, alarmId: Long, alarmLabel: String) {
        try {
            // Create a high-priority notification that launches the alarm
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // Create notification channel if needed
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    "day_call_alarm_channel",
                    "Day Call Alarms",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alarm notifications"
                    enableLights(true)
                    enableVibration(true)
                    setBypassDnd(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val alarmIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", alarmLabel)
                putExtra("FROM_NOTIFICATION", true)
            }
            
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                alarmId.toInt(),
                alarmIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = androidx.core.app.NotificationCompat.Builder(context, "day_call_alarm_channel")
                .setContentTitle("🚨 ALARM: $alarmLabel")
                .setContentText("Alarm is ringing! Tap to open challenge.")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setLights(0xFFFF0000.toInt(), 1000, 1000)
                .build()
            
            notificationManager.notify(alarmId.toInt(), notification)
            Log.d("AlarmReceiver", "Showed alarm notification for alarm: $alarmId")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to show alarm notification", e)
        }
    }
    
    private fun showFallbackNotification(context: Context, alarmId: Long, alarmLabel: String) {
        try {
            Log.w("AlarmReceiver", "Using fallback notification for alarm: $alarmId")
            showAlarmNotification(context, alarmId, alarmLabel)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to show fallback notification", e)
        }
    }
    
    private fun startAlarmService(
        context: Context, 
        sound: String, 
        vibe: String, 
        alarmId: Long, 
        alarmLabel: String, 
        challengeType: String
    ) {
        try {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("sound_file", sound)
                putExtra("vibe_category", vibe)
                putExtra("alarm_time", LocalDateTime.now().toString())
                // Pass alarm details for notification
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", alarmLabel)
                putExtra("SOUND", sound)
                putExtra("CHALLENGE_TYPE", challengeType)
                putExtra("VIBE", vibe)
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            Log.d("AlarmReceiver", "Started AlarmService with sound: $sound, vibe: $vibe")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start AlarmService", e)
        }
    }
    
    private fun rescheduleAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = DayCallDatabase.getInstance(context)
                val repository = AlarmRepository(database.alarmDao())
                val alarmScheduler = AlarmScheduler(context)
                
                repository.getAlarms().collect { alarms ->
                    alarms.filter { it.enabled }.forEach { alarm ->
                        Log.d("AlarmReceiver", "Rescheduling alarm: ${alarm.id}")
                        alarmScheduler.scheduleAlarm(alarm)
                    }
                }
                Log.d("AlarmReceiver", "Successfully rescheduled all enabled alarms")
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Failed to reschedule alarms", e)
            }
        }
    }
} 