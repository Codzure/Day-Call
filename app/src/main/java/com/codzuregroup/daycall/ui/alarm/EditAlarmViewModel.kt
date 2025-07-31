package com.codzuregroup.daycall.ui.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codzuregroup.daycall.data.DayCallDatabase
import com.codzuregroup.daycall.data.AlarmRepository
import com.codzuregroup.daycall.ui.challenges.ChallengeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class EditAlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository
    
    private val _alarmTime = MutableStateFlow(AlarmTime(7, 0))
    val alarmTime: StateFlow<AlarmTime> = _alarmTime.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _label = MutableStateFlow("Alarm")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _selectedSound = MutableStateFlow("Default")
    val selectedSound: StateFlow<String> = _selectedSound.asStateFlow()

    private val _selectedDays = MutableStateFlow<Set<DayOfWeek>>(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
    val selectedDays: StateFlow<Set<DayOfWeek>> = _selectedDays.asStateFlow()

    private val _challengeType = MutableStateFlow(ChallengeType.MATH)
    val challengeType: StateFlow<ChallengeType> = _challengeType.asStateFlow()

    init {
        val database = DayCallDatabase.getInstance(application)
        repository = AlarmRepository(database.alarmDao())
    }

    fun loadAlarm(alarmId: Long) {
        viewModelScope.launch {
            val alarm = repository.getAlarm(alarmId)
            alarm?.let {
                _alarmTime.value = AlarmTime(it.hour, it.minute)
                _isEnabled.value = it.enabled
                _label.value = it.label ?: "Alarm"
                _selectedSound.value = it.sound
                _challengeType.value = it.getChallengeTypeEnum()
                
                // Convert repeat days bitmask to set
                val days = mutableSetOf<DayOfWeek>()
                if (it.repeatDays and 0b1 != 0) days.add(DayOfWeek.MONDAY)
                if (it.repeatDays and 0b10 != 0) days.add(DayOfWeek.TUESDAY)
                if (it.repeatDays and 0b100 != 0) days.add(DayOfWeek.WEDNESDAY)
                if (it.repeatDays and 0b1000 != 0) days.add(DayOfWeek.THURSDAY)
                if (it.repeatDays and 0b10000 != 0) days.add(DayOfWeek.FRIDAY)
                if (it.repeatDays and 0b100000 != 0) days.add(DayOfWeek.SATURDAY)
                if (it.repeatDays and 0b1000000 != 0) days.add(DayOfWeek.SUNDAY)
                _selectedDays.value = days
            }
        }
    }

    fun updateTime(hour: Int, minute: Int) {
        _alarmTime.value = AlarmTime(hour, minute)
    }

    fun updateLabel(newLabel: String) {
        _label.value = newLabel
    }

    fun toggleDay(day: DayOfWeek) {
        val current = _selectedDays.value.toMutableSet()
        if (current.contains(day)) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _selectedDays.value = current
    }

    fun updateSound(sound: String) {
        _selectedSound.value = sound
    }

    fun toggleEnabled() {
        _isEnabled.value = !_isEnabled.value
    }

    fun updateChallengeType(type: ChallengeType) {
        _challengeType.value = type
    }

    fun saveAlarm(alarmId: Long = 0) {
        viewModelScope.launch {
            val repeatDays = _selectedDays.value.fold(0) { acc, day ->
                acc or (1 shl day.value)
            }
            
            val alarmEntity = com.codzuregroup.daycall.data.AlarmEntity(
                id = alarmId,
                hour = _alarmTime.value.hour,
                minute = _alarmTime.value.minute,
                repeatDays = repeatDays,
                label = _label.value,
                sound = _selectedSound.value,
                challengeType = _challengeType.value.name,
                enabled = _isEnabled.value
            )
            
            if (alarmId == 0L) {
                repository.upsertAlarm(alarmEntity)
            } else {
                repository.updateAlarm(alarmEntity)
            }
        }
    }
}
