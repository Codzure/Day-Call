package com.codzuregroup.daycall.ui

import android.app.Application
import android.app.AlarmManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codzuregroup.daycall.alarm.AlarmScheduler
import com.codzuregroup.daycall.data.DayCallDatabase
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
        val database = DayCallDatabase.getInstance(application)
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
            try {
                val alarmId = repository.upsertAlarm(alarm)
                val savedAlarm = alarm.copy(id = alarmId)
                
                Log.d("AlarmViewModel", "Saving alarm: ${savedAlarm.id}, enabled: ${savedAlarm.enabled}, time: ${savedAlarm.hour}:${savedAlarm.minute}")
                
                // Schedule the alarm if enabled
                if (savedAlarm.enabled) {
                    if (areExactAlarmsAllowed()) {
                        alarmScheduler.scheduleAlarm(savedAlarm)
                        Log.d("AlarmViewModel", "Alarm scheduled successfully: ${savedAlarm.id}")
                    } else {
                        Log.e("AlarmViewModel", "Cannot schedule alarm - exact alarms not allowed")
                        // Show user a message to enable exact alarms
                    }
                } else {
                    Log.d("AlarmViewModel", "Alarm disabled, not scheduling: ${savedAlarm.id}")
                }
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Error saving alarm", e)
            }
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            try {
                Log.d("AlarmViewModel", "Updating alarm: ${alarm.id}, enabled: ${alarm.enabled}")
                repository.updateAlarm(alarm)
                
                // Cancel existing alarm first
                alarmScheduler.cancelAlarm(alarm)
                
                // Schedule the alarm if enabled
                if (alarm.enabled) {
                    if (areExactAlarmsAllowed()) {
                        alarmScheduler.scheduleAlarm(alarm)
                        Log.d("AlarmViewModel", "Alarm rescheduled successfully: ${alarm.id}")
                    } else {
                        Log.e("AlarmViewModel", "Cannot reschedule alarm - exact alarms not allowed")
                    }
                } else {
                    Log.d("AlarmViewModel", "Alarm disabled, not rescheduling: ${alarm.id}")
                }
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Error updating alarm", e)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            try {
                Log.d("AlarmViewModel", "Deleting alarm: ${alarm.id}")
                // Cancel the alarm first
                alarmScheduler.cancelAlarm(alarm)
                // Then delete from database
                repository.deleteAlarm(alarm)
                Log.d("AlarmViewModel", "Alarm deleted successfully: ${alarm.id}")
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Error deleting alarm", e)
            }
        }
    }

    fun toggleEnabled(alarm: AlarmEntity, enabled: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("AlarmViewModel", "Toggling alarm ${alarm.id} enabled: $enabled")
                val updatedAlarm = alarm.copy(enabled = enabled)
                repository.updateAlarm(updatedAlarm)
                
                if (enabled) {
                    if (areExactAlarmsAllowed()) {
                        alarmScheduler.scheduleAlarm(updatedAlarm)
                        Log.d("AlarmViewModel", "Alarm enabled and scheduled: ${alarm.id}")
                    } else {
                        Log.e("AlarmViewModel", "Cannot enable alarm - exact alarms not allowed")
                    }
                } else {
                    alarmScheduler.cancelAlarm(updatedAlarm)
                    Log.d("AlarmViewModel", "Alarm disabled and cancelled: ${alarm.id}")
                }
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Error toggling alarm", e)
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
    
    // Test method to schedule an alarm for 10 seconds from now
    fun testAlarm() {
        viewModelScope.launch {
            try {
                val testAlarm = AlarmEntity(
                    hour = java.time.LocalTime.now().hour,
                    minute = java.time.LocalTime.now().minute + 1, // 1 minute from now
                    label = "Test Alarm",
                    sound = "Ascent Braam",
                    challengeType = "MATH",
                    vibe = "chill",
                    enabled = true
                )
                
                Log.d("AlarmViewModel", "Creating test alarm for ${testAlarm.hour}:${testAlarm.minute}")
                saveAlarm(testAlarm)
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Error creating test alarm", e)
            }
        }
    }

}
