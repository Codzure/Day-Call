package com.codzuregroup.daycall.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.codzuregroup.daycall.service.NextAlarmForegroundService.Companion.ACTION_UPDATE
import com.codzuregroup.daycall.service.NextAlarmForegroundService.Companion.EXTRA_TIME
import com.codzuregroup.daycall.service.NextAlarmForegroundService.Companion.EXTRA_TITLE
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NextAlarmNotifier {
    fun startOrUpdate(context: Context, nextTime: LocalDateTime?, label: String = "Next alarm") {
        val svc = Intent(context, NextAlarmForegroundService::class.java).apply {
            action = ACTION_UPDATE
            putExtra(EXTRA_TITLE, label)
            putExtra(EXTRA_TIME, nextTime?.format(DateTimeFormatter.ofPattern("EEE, MMM d • HH:mm")) ?: "No upcoming alarms")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(svc)
        } else {
            context.startService(svc)
        }
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, NextAlarmForegroundService::class.java))
    }
}

