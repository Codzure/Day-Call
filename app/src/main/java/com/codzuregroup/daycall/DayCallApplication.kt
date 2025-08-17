package com.codzuregroup.daycall

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.codzuregroup.daycall.alarm.AlarmReliabilityManager

class DayCallApplication : Application(), Configuration.Provider {
    
    private lateinit var alarmReliabilityManager: AlarmReliabilityManager
    
    override fun onCreate() {
        super.onCreate()
        Log.d("DayCallApplication", "Application starting")
        
        // Initialize WorkManager
        WorkManager.initialize(this, workManagerConfiguration)
        
        // Initialize alarm reliability features
        initializeAlarmReliability()
        
        Log.d("DayCallApplication", "Application initialized")
    }
    
    private fun initializeAlarmReliability() {
        try {
            // Perform cleanup first to prevent random alarms
            val cleanupUtility = com.codzuregroup.daycall.alarm.AlarmCleanupUtility(this)
            cleanupUtility.performFullCleanup()
            
            alarmReliabilityManager = AlarmReliabilityManager(this)
            
            // Initialize reliability features (without auto-scheduling backup alarms)
            alarmReliabilityManager.initializeReliabilityFeatures()
            
            Log.d("DayCallApplication", "Alarm reliability features initialized with cleanup")
        } catch (e: Exception) {
            Log.e("DayCallApplication", "Failed to initialize alarm reliability", e)
        }
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
    
    fun getAlarmReliabilityManager(): AlarmReliabilityManager {
        return alarmReliabilityManager
    }
    
    /**
     * Debug method to check alarm status
     */
    fun debugAlarmStatus() {
        Log.d("DayCallApplication", "=== ALARM DEBUG STATUS ===")
        try {
            // Check WorkManager status
            val workManager = WorkManager.getInstance(this)
            val workInfos = workManager.getWorkInfosByTag("backup_alarm").get()
            Log.d("DayCallApplication", "Active backup alarm works: ${workInfos.size}")
            
            workInfos.forEach { workInfo ->
                Log.d("DayCallApplication", "Work: ${workInfo.id}, State: ${workInfo.state}, Tags: ${workInfo.tags}")
            }
            
            // Check reliability status
            val status = alarmReliabilityManager.getReliabilityStatus()
            Log.d("DayCallApplication", "Reliability score: ${status.reliabilityScore}")
            Log.d("DayCallApplication", "Backup alarms enabled: ${status.backupAlarmsEnabled}")
            
        } catch (e: Exception) {
            Log.e("DayCallApplication", "Failed to get debug status", e)
        }
        Log.d("DayCallApplication", "=== END ALARM DEBUG ===")
    }
}