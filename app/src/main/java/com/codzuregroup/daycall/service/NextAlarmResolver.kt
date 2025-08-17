package com.codzuregroup.daycall.service

import android.content.Context
import com.codzuregroup.daycall.data.AlarmEntity
import com.codzuregroup.daycall.ui.AlarmViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object NextAlarmResolver {
    /** Compute the next occurring LocalDateTime from a list of alarms. */
    fun computeNext(alarmList: List<AlarmEntity>, now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        val candidates = mutableListOf<LocalDateTime>()
        for (a in alarmList.filter { it.enabled }) {
            val baseToday = LocalDateTime.of(LocalDate.now(), a.toLocalTime())
            val todayCandidate = if (baseToday.isAfter(now)) baseToday else baseToday.plusDays(1)
            candidates += todayCandidate
        }
        return candidates.minOrNull()
    }

    fun updateService(context: Context, alarms: List<AlarmEntity>) {
        val next = computeNext(alarms)
        if (next != null) NextAlarmNotifier.startOrUpdate(context, next) else NextAlarmNotifier.stop(context)
    }
}

