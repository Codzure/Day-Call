package com.codzuregroup.daycall.ui

import android.app.Application
import android.app.AlarmManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codzuregroup.daycall.alarm.AlarmScheduler
import com.codzuregroup.daycall.data.AlarmDatabase
import com.codzuregroup.daycall.data.AlarmEntity
import com.codzuregroup.daycall.data.AlarmRepository
import com.codzuregroup.daycall.ui.alarm.Alarm
import com.codzuregroup.daycall.ui.alarm.AlarmTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository
    private val alarmScheduler: AlarmScheduler
    private val _alarms = MutableStateFlow<List<AlarmEntity>>(emptyList())
    val alarms: StateFlow<List<AlarmEntity>> = _alarms.asStateFlow()

    init {
        val database = AlarmDatabase.getInstance(application)
        repository = AlarmRepository(database.alarmDao())
        alarmScheduler = AlarmScheduler(application)
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            repository.getAlarms().collect { alarmList ->
                _alarms.value = alarmList
            }
        }
    }

    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val alarmId = repository.upsertAlarm(alarm)
            val savedAlarm = alarm.copy(id = alarmId)
            
            Log.d("AlarmViewModel", "Saving alarm: ${savedAlarm.id}, enabled: ${savedAlarm.enabled}")
            
            // Schedule the alarm if enabled
            if (savedAlarm.enabled) {
                if (areExactAlarmsAllowed()) {
                    alarmScheduler.scheduleAlarm(savedAlarm)
                    Log.d("AlarmViewModel", "Alarm scheduled: ${savedAlarm.id}")
                } else {
                    Log.e("AlarmViewModel", "Cannot schedule alarm - exact alarms not allowed")
                }
            }
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            Log.d("AlarmViewModel", "Updating alarm: ${alarm.id}, enabled: ${alarm.enabled}")
            repository.updateAlarm(alarm)
            
            // Cancel existing alarm first
            alarmScheduler.cancelAlarm(alarm)
            
            // Schedule the alarm if enabled
            if (alarm.enabled) {
                if (areExactAlarmsAllowed()) {
                    alarmScheduler.scheduleAlarm(alarm)
                    Log.d("AlarmViewModel", "Alarm rescheduled: ${alarm.id}")
                } else {
                    Log.e("AlarmViewModel", "Cannot reschedule alarm - exact alarms not allowed")
                }
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            Log.d("AlarmViewModel", "Deleting alarm: ${alarm.id}")
            // Cancel the alarm first
            alarmScheduler.cancelAlarm(alarm)
            // Then delete from database
            repository.deleteAlarm(alarm)
        }
    }

    fun toggleEnabled(alarm: AlarmEntity, enabled: Boolean) {
        viewModelScope.launch {
            Log.d("AlarmViewModel", "Toggling alarm ${alarm.id} enabled: $enabled")
            val updatedAlarm = alarm.copy(enabled = enabled)
            repository.updateAlarm(updatedAlarm)
            
            if (enabled) {
                if (areExactAlarmsAllowed()) {
                    alarmScheduler.scheduleAlarm(updatedAlarm)
                } else {
                    Log.e("AlarmViewModel", "Cannot enable alarm - exact alarms not allowed")
                }
            } else {
                alarmScheduler.cancelAlarm(updatedAlarm)
            }
        }
    }

    // Check if exact alarms are allowed
    fun areExactAlarmsAllowed(): Boolean {
        val alarmManager = getApplication<Application>().getSystemService(AlarmManager::class.java)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // For older versions, assume allowed
        }
    }

    suspend fun getAlarmById(alarmId: Long): AlarmEntity? {
        return repository.getAlarm(alarmId)
    }
    
    fun toggleAlarm(alarmId: Long) {
        viewModelScope.launch {
            val alarm = repository.getAlarm(alarmId)
            alarm?.let {
                toggleEnabled(it, !it.enabled)
            }
        }
    }
    
    // Test method - creates an alarm that rings in 1 minute
    fun createTestAlarm() {
        viewModelScope.launch {
            val now = java.time.LocalTime.now()
            val testTime = now.plusMinutes(1)
            
            val testAlarm = com.codzuregroup.daycall.data.AlarmEntity(
                hour = testTime.hour,
                minute = testTime.minute,
                label = "Test Alarm - Should ring in 1 minute",
                repeatDays = 0, // One-time alarm
                challengeType = "MATH",
                audioFile = "labyrinth_for_the_brain_190096.mp3",
                enabled = true
            )
            
            Log.d("AlarmViewModel", "Creating test alarm for ${testTime.hour}:${testTime.minute}")
            saveAlarm(testAlarm)
        }
    }
}
