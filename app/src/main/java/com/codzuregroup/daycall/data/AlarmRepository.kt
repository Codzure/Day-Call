package com.codzuregroup.daycall.data

import kotlinx.coroutines.flow.Flow

class AlarmRepository(private val dao: AlarmDao) {
    fun getAlarms(): Flow<List<AlarmEntity>> = dao.getAlarms()

    suspend fun upsertAlarm(alarm: AlarmEntity): Long = dao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: AlarmEntity) = dao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: AlarmEntity) = dao.deleteAlarm(alarm)

    suspend fun getAlarm(id: Long): AlarmEntity? = dao.getAlarmById(id)
} 