package com.codzuregroup.daycall.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager
import com.codzuregroup.daycall.data.DayCallDatabase
import com.codzuregroup.daycall.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility class to clean up orphaned alarms and prevent random alarms
 */
class AlarmCleanupUtility(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    /**
     * Clean up all orphaned alarms and work manager tasks
     */
    fun performFullCleanup() {
        Log.d("AlarmCleanupUtility", "Starting full alarm cleanup")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Cancel all backup alarm work
                cancelAllBackupWork()
                
                // 2. Get all alarms from database
                val database = DayCallDatabase.getInstance(context)
                val repository = AlarmRepository(database.alarmDao())
                
                repository.getAlarms().collect { alarms ->
                    val enabledAlarms = alarms.filter { it.enabled }
                    val disabledAlarms = alarms.filter { !it.enabled }
                    
                    Log.d("AlarmCleanupUtility", "Found ${enabledAlarms.size} enabled alarms, ${disabledAlarms.size} disabled alarms")
                    
                    // 3. Cancel all system alarms for disabled alarms
                    disabledAlarms.forEach { alarm ->
                        cancelSystemAlarm(alarm.id)
                        Log.d("AlarmCleanupUtility", "Cancelled system alarm for disabled alarm: ${alarm.id}")
                    }
                    
                    // 4. Verify enabled alarms are properly scheduled
                    val alarmScheduler = AlarmScheduler(context)
                    enabledAlarms.forEach { alarm ->
                        Log.d("AlarmCleanupUtility", "Re-scheduling enabled alarm: ${alarm.id} at ${alarm.hour}:${alarm.minute}")
                        alarmScheduler.scheduleAlarm(alarm)
                    }
                    
                    Log.d("AlarmCleanupUtility", "Cleanup completed successfully")
                }
            } catch (e: Exception) {
                Log.e("AlarmCleanupUtility", "Failed to perform cleanup", e)
            }
        }
    }
    
    /**
     * Cancel all backup work manager tasks
     */
    private fun cancelAllBackupWork() {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel all backup alarm work
        workManager.cancelAllWorkByTag("backup_alarm")
        
        // Cancel reliability check work if needed
        workManager.cancelAllWorkByTag("reliability_check")
        
        Log.d("AlarmCleanupUtility", "Cancelled all backup work manager tasks")
    }
    
    /**
     * Cancel system alarm for a specific alarm ID
     */
    private fun cancelSystemAlarm(alarmId: Long) {
        try {
            // Cancel one-time alarm
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { 
                alarmManager.cancel(it)
                it.cancel()
            }
            
            // Cancel potential repeating alarms (for each day of week)
            for (dayValue in 1..7) {
                val repeatIntent = Intent(context, AlarmReceiver::class.java)
                val repeatPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (alarmId * 10 + dayValue).toInt(),
                    repeatIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                repeatPendingIntent?.let { 
                    alarmManager.cancel(it)
                    it.cancel()
                }
            }
            
            Log.d("AlarmCleanupUtility", "Cancelled system alarms for ID: $alarmId")
        } catch (e: Exception) {
            Log.e("AlarmCleanupUtility", "Failed to cancel system alarm for ID: $alarmId", e)
        }
    }
    
    /**
     * Get debug information about current alarm state
     */
    fun getDebugInfo(): String {
        val sb = StringBuilder()
        sb.appendLine("=== ALARM DEBUG INFO ===")
        
        try {
            // WorkManager info
            val workManager = WorkManager.getInstance(context)
            val backupWorks = workManager.getWorkInfosByTag("backup_alarm").get()
            sb.appendLine("Active backup works: ${backupWorks.size}")
            
            backupWorks.forEach { workInfo ->
                sb.appendLine("  Work: ${workInfo.id}, State: ${workInfo.state}")
            }
            
            // Database info
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = DayCallDatabase.getInstance(context)
                    val repository = AlarmRepository(database.alarmDao())
                    
                    repository.getAlarms().collect { alarms ->
                        sb.appendLine("Database alarms: ${alarms.size}")
                        sb.appendLine("Enabled alarms: ${alarms.count { it.enabled }}")
                        sb.appendLine("Disabled alarms: ${alarms.count { !it.enabled }}")
                        
                        alarms.forEach { alarm ->
                            sb.appendLine("  Alarm ${alarm.id}: ${alarm.hour}:${alarm.minute}, enabled=${alarm.enabled}, label=${alarm.label}")
                        }
                    }
                } catch (e: Exception) {
                    sb.appendLine("Error reading database: ${e.message}")
                }
            }
            
        } catch (e: Exception) {
            sb.appendLine("Error getting debug info: ${e.message}")
        }
        
        sb.appendLine("=== END DEBUG INFO ===")
        return sb.toString()
    }
    
    /**
     * Emergency cleanup - cancel everything
     */
    fun emergencyCleanup() {
        Log.w("AlarmCleanupUtility", "Performing emergency cleanup - cancelling ALL alarms and work")
        
        try {
            // Cancel all WorkManager work
            WorkManager.getInstance(context).cancelAllWork()
            
            // This is more aggressive - we can't easily cancel all system alarms
            // but we can clear our database and let the user re-add alarms
            Log.w("AlarmCleanupUtility", "Emergency cleanup completed")
        } catch (e: Exception) {
            Log.e("AlarmCleanupUtility", "Emergency cleanup failed", e)
        }
    }
}