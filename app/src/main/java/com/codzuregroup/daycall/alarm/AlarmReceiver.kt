package com.codzuregroup.daycall.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.codzuregroup.daycall.data.AlarmDatabase
import com.codzuregroup.daycall.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            "ALARM_TRIGGER" -> {
                val alarmId = intent.getLongExtra("ALARM_ID", -1)
                val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
                val audioFile = intent.getStringExtra("AUDIO_FILE")
                val challengeType = intent.getStringExtra("CHALLENGE_TYPE") ?: "MATH"
                
                Log.d("AlarmReceiver", "Processing alarm: ID=$alarmId, Label=$alarmLabel, Audio=$audioFile, Challenge=$challengeType")
                
                // Start the alarm ringing activity
                val alarmIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("ALARM_ID", alarmId)
                    putExtra("ALARM_LABEL", alarmLabel)
                    putExtra("AUDIO_FILE", audioFile)
                    putExtra("CHALLENGE_TYPE", challengeType)
                }
                
                try {
                    context.startActivity(alarmIntent)
                    Log.d("AlarmReceiver", "Successfully started AlarmRingingActivity")
                } catch (e: Exception) {
                    Log.e("AlarmReceiver", "Failed to start AlarmRingingActivity", e)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("AlarmReceiver", "Boot completed, rescheduling alarms")
                rescheduleAlarms(context)
            }
            else -> {
                Log.d("AlarmReceiver", "Unknown action: ${intent.action}")
            }
        }
    }
    
    private fun rescheduleAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AlarmDatabase.getInstance(context)
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