package com.codzuregroup.daycall.alarm

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Manages the triggering of alarms with multiple fallback mechanisms
 * to ensure alarms always wake up the user
 */
class AlarmTriggerManager(private val context: Context) {
    
    private val handler = Handler(Looper.getMainLooper())
    
    /**
     * Trigger alarm with maximum reliability
     */
    fun triggerAlarm(
        alarmId: Long,
        alarmLabel: String,
        sound: String,
        challengeType: String,
        vibe: String
    ) {
        Log.d("AlarmTriggerManager", "Triggering alarm $alarmId with maximum reliability")
        
        // Step 1: Immediately acquire wake lock and wake up device
        wakeUpDevice()
        
        // Step 2: Start alarm service for sound
        startAlarmService(alarmId, alarmLabel, sound, challengeType, vibe)
        
        // Step 3: Try to launch activity
        launchAlarmActivity(alarmId, alarmLabel, sound, challengeType, vibe)
        
        // Step 4: Show high-priority notification as backup
        showAlarmNotification(alarmId, alarmLabel)
        
        // Step 5: Set up fallback mechanisms
        setupFallbackMechanisms(alarmId, alarmLabel, sound, challengeType, vibe)
    }
    
    private fun wakeUpDevice() {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            
            // Create a wake lock that will wake up the device
            val wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or 
                PowerManager.ACQUIRE_CAUSES_WAKEUP or 
                PowerManager.ON_AFTER_RELEASE,
                "DayCall:AlarmTriggerWakeLock"
            )
            
            // Acquire for 30 seconds to ensure device wakes up
            wakeLock.acquire(30 * 1000L)
            
            Log.d("AlarmTriggerManager", "Device wake lock acquired")
            
            // Release after a short delay
            handler.postDelayed({
                try {
                    if (wakeLock.isHeld) {
                        wakeLock.release()
                        Log.d("AlarmTriggerManager", "Device wake lock released")
                    }
                } catch (e: Exception) {
                    Log.e("AlarmTriggerManager", "Failed to release wake lock", e)
                }
            }, 25000) // Release after 25 seconds
            
        } catch (e: Exception) {
            Log.e("AlarmTriggerManager", "Failed to wake up device", e)
        }
    }
    
    private fun startAlarmService(
        alarmId: Long,
        alarmLabel: String,
        sound: String,
        challengeType: String,
        vibe: String
    ) {
        try {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("sound_file", sound)
                putExtra("vibe_category", vibe)
                putExtra("alarm_time", java.time.LocalDateTime.now().toString())
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", alarmLabel)
                putExtra("SOUND", sound)
                putExtra("CHALLENGE_TYPE", challengeType)
                putExtra("VIBE", vibe)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            Log.d("AlarmTriggerManager", "Alarm service started successfully")
        } catch (e: Exception) {
            Log.e("AlarmTriggerManager", "Failed to start alarm service", e)
        }
    }
    
    private fun launchAlarmActivity(
        alarmId: Long,
        alarmLabel: String,
        sound: String,
        challengeType: String,
        vibe: String
    ) {
        try {
            val alarmIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", alarmLabel)
                putExtra("SOUND", sound)
                putExtra("CHALLENGE_TYPE", challengeType)
                putExtra("VIBE", vibe)
                putExtra("AUTO_TRIGGERED", true)
            }
            
            context.startActivity(alarmIntent)
            Log.d("AlarmTriggerManager", "Alarm activity launched successfully")
            
        } catch (e: Exception) {
            Log.e("AlarmTriggerManager", "Failed to launch alarm activity", e)
            
            // Try alternative launch method
            tryAlternativeLaunch(alarmId, alarmLabel, sound, challengeType, vibe)
        }
    }
    
    private fun tryAlternativeLaunch(
        alarmId: Long,
        alarmLabel: String,
        sound: String,
        challengeType: String,
        vibe: String
    ) {
        try {
            Log.d("AlarmTriggerManager", "Trying alternative launch method")
            
            // Try with minimal flags
            val alarmIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", alarmLabel)
                putExtra("SOUND", sound)
                putExtra("CHALLENGE_TYPE", challengeType)
                putExtra("VIBE", vibe)
                putExtra("FALLBACK_LAUNCH", true)
            }
            
            context.startActivity(alarmIntent)
            Log.d("AlarmTriggerManager", "Alternative launch successful")
            
        } catch (e: Exception) {
            Log.e("AlarmTriggerManager", "Alternative launch also failed", e)
        }
    }
    
    private fun showAlarmNotification(alarmId: Long, alarmLabel: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel if needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    "day_call_alarm_urgent",
                    "Urgent Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Critical alarm notifications"
                    enableLights(true)
                    enableVibration(true)
                    setBypassDnd(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setSound(null, null) // No sound - service handles audio
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val alarmIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
            
            val notification = NotificationCompat.Builder(context, "day_call_alarm_urgent")
                .setContentTitle("🚨 ALARM RINGING")
                .setContentText("$alarmLabel - Tap to dismiss")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
                .setLights(0xFFFF0000.toInt(), 500, 500)
                .setDefaults(0) // No defaults - we control everything
                .build()
            
            notificationManager.notify(alarmId.toInt(), notification)
            Log.d("AlarmTriggerManager", "High-priority alarm notification shown")
            
        } catch (e: Exception) {
            Log.e("AlarmTriggerManager", "Failed to show alarm notification", e)
        }
    }
    
    private fun setupFallbackMechanisms(
        alarmId: Long,
        alarmLabel: String,
        sound: String,
        challengeType: String,
        vibe: String
    ) {
        // Fallback 1: Try to relaunch activity after 5 seconds if it didn't start
        handler.postDelayed({
            checkAndRetryLaunch(alarmId, alarmLabel, sound, challengeType, vibe)
        }, 5000)
        
        // Fallback 2: Ensure service is still running after 10 seconds
        handler.postDelayed({
            ensureServiceRunning(alarmId, alarmLabel, sound, challengeType, vibe)
        }, 10000)
    }
    
    private fun checkAndRetryLaunch(
        alarmId: Long,
        alarmLabel: String,
        sound: String,
        challengeType: String,
        vibe: String
    ) {
        try {
            // Check if alarm service is running
            if (!AlarmService.isAlarmRunning()) {
                Log.w("AlarmTriggerManager", "Alarm service not running, restarting")
                startAlarmService(alarmId, alarmLabel, sound, challengeType, vibe)
            }
            
            // Try to launch activity again if needed
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (keyguardManager.isKeyguardLocked) {
                Log.d("AlarmTriggerManager", "Device still locked, retrying activity launch")
                launchAlarmActivity(alarmId, alarmLabel, sound, challengeType, vibe)
            }
            
        } catch (e: Exception) {
            Log.e("AlarmTriggerManager", "Failed to check and retry launch", e)
        }
    }
    
    private fun ensureServiceRunning(
        alarmId: Long,
        alarmLabel: String,
        sound: String,
        challengeType: String,
        vibe: String
    ) {
        try {
            if (!AlarmService.isAlarmRunning()) {
                Log.w("AlarmTriggerManager", "Alarm service stopped unexpectedly, restarting")
                startAlarmService(alarmId, alarmLabel, sound, challengeType, vibe)
            } else {
                Log.d("AlarmTriggerManager", "Alarm service still running correctly")
            }
        } catch (e: Exception) {
            Log.e("AlarmTriggerManager", "Failed to ensure service running", e)
        }
    }
}