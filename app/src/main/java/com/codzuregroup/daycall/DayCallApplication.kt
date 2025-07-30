package com.codzuregroup.daycall

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class DayCallApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
        )
    }
} 