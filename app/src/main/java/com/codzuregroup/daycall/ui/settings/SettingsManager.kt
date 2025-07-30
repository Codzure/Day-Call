package com.codzuregroup.daycall.ui.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TimeFormat {
    HOUR_12,
    HOUR_24
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("daycall_settings", Context.MODE_PRIVATE)
    
    private val _timeFormat = MutableStateFlow(TimeFormat.HOUR_12)
    val timeFormat: StateFlow<TimeFormat> = _timeFormat.asStateFlow()
    
    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()
    
    private val _vibrationIntensity = MutableStateFlow(0.7f)
    val vibrationIntensity: StateFlow<Float> = _vibrationIntensity.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        val timeFormatValue = prefs.getString("time_format", "HOUR_12") ?: "HOUR_12"
        _timeFormat.value = TimeFormat.valueOf(timeFormatValue)
        
        _vibrationEnabled.value = prefs.getBoolean("vibration_enabled", true)
        _vibrationIntensity.value = prefs.getFloat("vibration_intensity", 0.7f)
    }
    
    fun setTimeFormat(format: TimeFormat) {
        _timeFormat.value = format
        prefs.edit().putString("time_format", format.name).apply()
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }
    
    fun setVibrationIntensity(intensity: Float) {
        _vibrationIntensity.value = intensity.coerceIn(0f, 1f)
        prefs.edit().putFloat("vibration_intensity", _vibrationIntensity.value).apply()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null
        
        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context).also { INSTANCE = it }
            }
        }
    }
} 