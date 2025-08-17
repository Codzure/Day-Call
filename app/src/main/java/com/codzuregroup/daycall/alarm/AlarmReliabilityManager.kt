package com.codzuregroup.daycall.alarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.work.*
import com.codzuregroup.daycall.data.DayCallDatabase
import com.codzuregroup.daycall.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Manages alarm reliability by implementing multiple backup mechanisms
 * to ensure alarms ring even when the app is closed or device is in deep sleep
 */
class AlarmReliabilityManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("alarm_reliability", Context.MODE_PRIVATE)
    private val alarmScheduler = AlarmScheduler(context)
    
    companion object {
        private const val PREF_LAST_RELIABILITY_CHECK = "last_reliability_check"
        private const val PREF_BACKUP_ALARMS_ENABLED = "backup_alarms_enabled"
        private const val RELIABILITY_CHECK_WORK_TAG = "reliability_check"
        private const val BACKUP_ALARM_WORK_TAG = "backup_alarm"
    }
    
    /**
     * Initialize reliability features - call this when app starts
     */
    fun initializeReliabilityFeatures() {
        Log.d("AlarmReliabilityManager", "Initializing reliability features")
        
        // Schedule periodic reliability checks
        scheduleReliabilityCheck()
        
        // Check and request all necessary permissions
        checkAndRequestAllPermissions()
        
        // Verify alarm manager functionality
        verifyAlarmManagerFunctionality()
        
        // Only enable backup alarms if explicitly requested, not automatically
        Log.d("AlarmReliabilityManager", "Reliability features initialized (backup alarms disabled by default)")
    }
    
    /**
     * Schedule periodic checks to ensure alarms are still scheduled
     */
    private fun scheduleReliabilityCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setRequiresStorageNotLow(false)
            .build()
        
        val reliabilityCheckRequest = PeriodicWorkRequestBuilder<ReliabilityCheckWorker>(
            15, TimeUnit.MINUTES // Check every 15 minutes
        )
            .setConstraints(constraints)
            .addTag(RELIABILITY_CHECK_WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            RELIABILITY_CHECK_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            reliabilityCheckRequest
        )
        
        Log.d("AlarmReliabilityManager", "Scheduled periodic reliability checks")
    }
    
    /**
     * Enable backup alarm system using WorkManager as a fallback
     * Only call this when explicitly needed, not automatically
     */
    fun enableBackupAlarmSystem() {
        prefs.edit().putBoolean(PREF_BACKUP_ALARMS_ENABLED, true).apply()
        Log.d("AlarmReliabilityManager", "Backup alarm system enabled")
        
        // Don't automatically schedule backup alarms for all alarms
        // Only schedule them when individual alarms are created/updated
    }
    
    /**
     * Schedule a backup alarm using WorkManager
     */
    fun scheduleBackupAlarm(alarmId: Long, hour: Int, minute: Int, label: String) {
        val now = LocalDateTime.now()
        var targetTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        
        // If alarm time has passed today, schedule for tomorrow
        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1)
        }
        
        val delayMillis = targetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - 
                         System.currentTimeMillis()
        
        if (delayMillis > 0) {
            val backupAlarmRequest = OneTimeWorkRequestBuilder<BackupAlarmWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag(BACKUP_ALARM_WORK_TAG)
                .addTag("backup_alarm_$alarmId")
                .setInputData(
                    Data.Builder()
                        .putLong("alarm_id", alarmId)
                        .putString("alarm_label", label)
                        .putInt("hour", hour)
                        .putInt("minute", minute)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                "backup_alarm_$alarmId",
                ExistingWorkPolicy.REPLACE,
                backupAlarmRequest
            )
            
            Log.d("AlarmReliabilityManager", "Scheduled backup alarm for $label at $hour:$minute (delay: ${delayMillis}ms)")
        }
    }
    
    /**
     * Cancel backup alarm
     */
    fun cancelBackupAlarm(alarmId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("backup_alarm_$alarmId")
        Log.d("AlarmReliabilityManager", "Cancelled backup alarm for ID: $alarmId")
    }
    
    /**
     * Cancel all backup alarms
     */
    fun cancelAllBackupAlarms() {
        WorkManager.getInstance(context).cancelAllWorkByTag(BACKUP_ALARM_WORK_TAG)
        Log.d("AlarmReliabilityManager", "Cancelled all backup alarms")
    }
    
    /**
     * Check and request all necessary permissions for reliable alarms
     */
    private fun checkAndRequestAllPermissions() {
        val permissionHelper = AlarmPermissionHelper(context)
        val status = permissionHelper.checkAllPermissions()
        
        if (!status.allPermissionsGranted) {
            Log.w("AlarmReliabilityManager", "Not all permissions granted: $status")
            
            // Request missing permissions
            if (!status.exactAlarmGranted) {
                permissionHelper.requestExactAlarmPermission()
            }
            if (!status.batteryOptimizationDisabled) {
                permissionHelper.requestBatteryOptimizationExemption()
            }
        } else {
            Log.d("AlarmReliabilityManager", "All alarm permissions granted")
        }
    }
    
    /**
     * Verify that AlarmManager is functioning correctly
     */
    private fun verifyAlarmManagerFunctionality() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Check if exact alarms can be scheduled
        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        
        if (!canScheduleExactAlarms) {
            Log.e("AlarmReliabilityManager", "Cannot schedule exact alarms - alarms may not be reliable")
        }
        
        // Check battery optimization status
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isBatteryOptimized = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            false
        }
        
        if (isBatteryOptimized) {
            Log.w("AlarmReliabilityManager", "App is battery optimized - alarms may be delayed")
        }
        
        Log.d("AlarmReliabilityManager", "AlarmManager verification complete - CanScheduleExact: $canScheduleExactAlarms, BatteryOptimized: $isBatteryOptimized")
    }
    
    /**
     * Force reschedule all alarms (useful after permission changes)
     * Only reschedules alarms that are actually enabled and should be active
     */
    fun forceRescheduleAllAlarms() {
        Log.d("AlarmReliabilityManager", "Force rescheduling all enabled alarms")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = DayCallDatabase.getInstance(context)
                val repository = AlarmRepository(database.alarmDao())
                
                // First, cancel all existing backup alarms to prevent duplicates
                cancelAllBackupAlarms()
                
                repository.getAlarms().collect { alarms ->
                    val enabledAlarms = alarms.filter { it.enabled }
                    Log.d("AlarmReliabilityManager", "Found ${enabledAlarms.size} enabled alarms to reschedule")
                    
                    if (enabledAlarms.isEmpty()) {
                        Log.d("AlarmReliabilityManager", "No enabled alarms found - nothing to reschedule")
                        return@collect
                    }
                    
                    enabledAlarms.forEach { alarm ->
                        // Cancel existing alarm first
                        alarmScheduler.cancelAlarm(alarm)
                        
                        // Reschedule with fresh settings
                        alarmScheduler.scheduleAlarm(alarm)
                        
                        // Only schedule backup alarm if backup system is enabled
                        if (prefs.getBoolean(PREF_BACKUP_ALARMS_ENABLED, false)) {
                            scheduleBackupAlarm(alarm.id, alarm.hour, alarm.minute, alarm.label ?: "Alarm")
                        }
                        
                        Log.d("AlarmReliabilityManager", "Rescheduled alarm: ${alarm.id} at ${alarm.hour}:${alarm.minute}")
                    }
                }
                Log.d("AlarmReliabilityManager", "Successfully rescheduled all enabled alarms")
            } catch (e: Exception) {
                Log.e("AlarmReliabilityManager", "Failed to reschedule alarms", e)
            }
        }
    }
    
    /**
     * Get reliability status report
     */
    fun getReliabilityStatus(): AlarmReliabilityStatus {
        val permissionHelper = AlarmPermissionHelper(context)
        val permissionStatus = permissionHelper.checkAllPermissions()
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isBatteryOptimized = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            false
        }
        
        val backupAlarmsEnabled = prefs.getBoolean(PREF_BACKUP_ALARMS_ENABLED, false)
        
        return AlarmReliabilityStatus(
            exactAlarmPermission = permissionStatus.exactAlarmGranted,
            batteryOptimizationDisabled = permissionStatus.batteryOptimizationDisabled,
            canScheduleExactAlarms = canScheduleExactAlarms,
            isBatteryOptimized = isBatteryOptimized,
            backupAlarmsEnabled = backupAlarmsEnabled,
            lastReliabilityCheck = prefs.getLong(PREF_LAST_RELIABILITY_CHECK, 0)
        )
    }
}

/**
 * Data class representing the current reliability status
 */
data class AlarmReliabilityStatus(
    val exactAlarmPermission: Boolean,
    val batteryOptimizationDisabled: Boolean,
    val canScheduleExactAlarms: Boolean,
    val isBatteryOptimized: Boolean,
    val backupAlarmsEnabled: Boolean,
    val lastReliabilityCheck: Long
) {
    val isFullyReliable: Boolean
        get() = exactAlarmPermission && batteryOptimizationDisabled && 
                canScheduleExactAlarms && !isBatteryOptimized && backupAlarmsEnabled
    
    val reliabilityScore: Float
        get() {
            var score = 0f
            if (exactAlarmPermission) score += 0.3f
            if (batteryOptimizationDisabled) score += 0.3f
            if (canScheduleExactAlarms) score += 0.2f
            if (!isBatteryOptimized) score += 0.1f
            if (backupAlarmsEnabled) score += 0.1f
            return score
        }
}

/**
 * Worker to periodically check alarm reliability
 */
class ReliabilityCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        Log.d("ReliabilityCheckWorker", "Starting reliability check")
        
        return try {
            val reliabilityManager = AlarmReliabilityManager(applicationContext)
            val status = reliabilityManager.getReliabilityStatus()
            
            // Update last check time
            val prefs = applicationContext.getSharedPreferences("alarm_reliability", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_reliability_check", System.currentTimeMillis()).apply()
            
            // If reliability is compromised, log it but don't automatically reschedule
            // This prevents random alarms from being created
            if (!status.isFullyReliable) {
                Log.w("ReliabilityCheckWorker", "Reliability compromised - Score: ${status.reliabilityScore}")
                Log.w("ReliabilityCheckWorker", "Manual intervention may be required")
                // Don't automatically reschedule - this was causing random alarms
            }
            
            Log.d("ReliabilityCheckWorker", "Reliability check completed - Score: ${status.reliabilityScore}")
            Result.success()
        } catch (e: Exception) {
            Log.e("ReliabilityCheckWorker", "Reliability check failed", e)
            Result.retry()
        }
    }
}

/**
 * Worker to trigger backup alarms
 */
class BackupAlarmWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        Log.d("BackupAlarmWorker", "Backup alarm triggered")
        
        return try {
            val alarmId = inputData.getLong("alarm_id", -1)
            val alarmLabel = inputData.getString("alarm_label") ?: "Backup Alarm"
            val hour = inputData.getInt("hour", 0)
            val minute = inputData.getInt("minute", 0)
            
            // Check if main alarm is already ringing
            if (AlarmService.isAlarmRunning()) {
                Log.d("BackupAlarmWorker", "Main alarm is already running, backup not needed")
                return Result.success()
            }
            
            Log.w("BackupAlarmWorker", "Main alarm failed, triggering backup alarm: $alarmLabel")
            
            // Trigger backup alarm
            val intent = Intent(applicationContext, AlarmReceiver::class.java).apply {
                action = "ALARM_TRIGGER"
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", "$alarmLabel (Backup)")
                putExtra("SOUND", "Ascent Braam")
                putExtra("CHALLENGE_TYPE", "MATH")
                putExtra("VIBE", "default")
            }
            
            applicationContext.sendBroadcast(intent)
            
            // Reschedule for next day if this was a repeating alarm
            val reliabilityManager = AlarmReliabilityManager(applicationContext)
            reliabilityManager.scheduleBackupAlarm(alarmId, hour, minute, alarmLabel)
            
            Log.d("BackupAlarmWorker", "Backup alarm triggered successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("BackupAlarmWorker", "Failed to trigger backup alarm", e)
            Result.failure()
        }
    }
}