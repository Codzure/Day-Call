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
    
    private val _timeFormat = MutableStateFlow(TimeFormat.HOUR_24)
    val timeFormat: StateFlow<TimeFormat> = _timeFormat.asStateFlow()
    
    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()
    
    private val _vibrationIntensity = MutableStateFlow(0.7f)
    val vibrationIntensity: StateFlow<Float> = _vibrationIntensity.asStateFlow()
    
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()
    
    private val _soundVolume = MutableStateFlow(0.8f)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()
    
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        val timeFormatValue = prefs.getString("time_format", "HOUR_24") ?: "HOUR_24"
        _timeFormat.value = TimeFormat.valueOf(timeFormatValue)
        
        _vibrationEnabled.value = prefs.getBoolean("vibration_enabled", true)
        _vibrationIntensity.value = prefs.getFloat("vibration_intensity", 0.7f)
        
        _soundEnabled.value = prefs.getBoolean("sound_enabled", true)
        _soundVolume.value = prefs.getFloat("sound_volume", 0.8f)
        
        _userName.value = prefs.getString("user_name", "") ?: ""
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
    
    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }
    
    fun setSoundVolume(volume: Float) {
        _soundVolume.value = volume.coerceIn(0f, 1f)
        prefs.edit().putFloat("sound_volume", _soundVolume.value).apply()
    }
    
    fun setUserName(name: String) {
        _userName.value = name
        prefs.edit().putString("user_name", name).apply()
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