package com.codzuregroup.daycall.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codzuregroup.daycall.data.AlarmEntity
import com.codzuregroup.daycall.data.AlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("ReminderWorker", "Worker started")
            val alarmId = inputData.getLong("alarm_id", -1)
            Log.d("ReminderWorker", "Alarm ID: $alarmId")
            
            if (alarmId == -1L) {
                Log.e("ReminderWorker", "Invalid alarm ID")
                return@withContext Result.failure()
            }
            
            val database = com.codzuregroup.daycall.data.AlarmDatabase.getInstance(context)
            val repository = AlarmRepository(database.alarmDao())
            val alarm = repository.getAlarm(alarmId)
            
            Log.d("ReminderWorker", "Retrieved alarm: $alarm")
            
            if (alarm != null && alarm.enabled) {
                Log.d("ReminderWorker", "Alarm is enabled, showing notification")
                val notificationManager = AlarmNotificationManager(context)
                notificationManager.showReminderNotification(alarm)
                Log.d("ReminderWorker", "Notification shown successfully")
            } else {
                Log.d("ReminderWorker", "Alarm is null or disabled")
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Error in worker", e)
            Result.failure()
        }
    }
} 