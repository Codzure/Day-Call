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
                val alarmId = intent.getLongExtra("ALARM_ID", -1)
                val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
                val sound = intent.getStringExtra("SOUND") ?: "Ascent Braam"
                val challengeType = intent.getStringExtra("CHALLENGE_TYPE") ?: "MATH"
                val vibe = intent.getStringExtra("VIBE") ?: "default"
                
                Log.d("AlarmReceiver", "Processing alarm: ID=$alarmId, Label=$alarmLabel, Sound=$sound, Challenge=$challengeType, Vibe=$vibe")
                
                // Acquire wake lock to ensure the device stays awake
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    "DayCall:AlarmWakeLock"
                )
                wakeLock.acquire(10 * 60 * 1000L) // 10 minutes timeout
                
                try {
                    // Start the alarm service to play the sound
                    startAlarmService(context, sound, vibe, alarmId, alarmLabel, challengeType)
                    
                    // Start the alarm ringing activity
                    val alarmIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("ALARM_ID", alarmId)
                        putExtra("ALARM_LABEL", alarmLabel)
                        putExtra("SOUND", sound)
                        putExtra("CHALLENGE_TYPE", challengeType)
                        putExtra("VIBE", vibe)
                    }
                    
                    context.startActivity(alarmIntent)
                    Log.d("AlarmReceiver", "Successfully started AlarmRingingActivity and AlarmService")
                } catch (e: Exception) {
                    Log.e("AlarmReceiver", "Failed to start alarm components", e)
                } finally {
                    // Release wake lock after a short delay to ensure activity starts
                    wakeLock.release()
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("AlarmReceiver", "Boot completed, rescheduling alarms")
                rescheduleAlarms(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d("AlarmReceiver", "App updated, rescheduling alarms")
                rescheduleAlarms(context)
            }
            else -> {
                Log.d("AlarmReceiver", "Unknown action: ${intent.action}")
            }
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