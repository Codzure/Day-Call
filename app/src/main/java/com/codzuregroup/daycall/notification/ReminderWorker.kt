package com.codzuregroup.daycall.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codzuregroup.daycall.data.AlarmEntity
import com.codzuregroup.daycall.data.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val alarmId = inputData.getLong("alarm_id", -1)
            if (alarmId == -1L) {
                return@withContext Result.failure()
            }
            
            val database = com.codzuregroup.daycall.data.AlarmDatabase.getInstance(context)
            val repository = AlarmRepository(database.alarmDao())
            val alarm = repository.getAlarm(alarmId)
            
            if (alarm != null && alarm.enabled) {
                val notificationManager = AlarmNotificationManager(context)
                notificationManager.scheduleReminderNotification(alarm)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 