package com.codzuregroup.daycall.ui.vibes

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VibeManager {
    private val _selectedVibe = MutableStateFlow<Vibe?>(null)
    val selectedVibe: StateFlow<Vibe?> = _selectedVibe.asStateFlow()
    
    private const val PREF_NAME = "vibe_preferences"
    private const val KEY_SELECTED_VIBE = "selected_vibe"
    
    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadSelectedVibe()
    }

    fun setSelectedVibe(vibe: Vibe?) {
        _selectedVibe.value = vibe
        saveSelectedVibe(vibe)
    }

    fun getSelectedVibe(): Vibe? {
        return _selectedVibe.value
    }

    fun getSelectedVibeId(): String {
        return _selectedVibe.value?.id ?: "chill"
    }
    
    fun getSelectedVibeForAlarm(): Vibe {
        return _selectedVibe.value ?: VibeDefaults.availableVibes.first()
    }
    
    fun initializeWithDefault() {
        if (_selectedVibe.value == null) {
            _selectedVibe.value = VibeDefaults.availableVibes.first()
        }
    }
    
    private fun saveSelectedVibe(vibe: Vibe?) {
        prefs?.edit()?.putString(KEY_SELECTED_VIBE, vibe?.id)?.apply()
    }
    
    private fun loadSelectedVibe() {
        val vibeId = prefs?.getString(KEY_SELECTED_VIBE, "chill") ?: "chill"
        val vibe = VibeDefaults.availableVibes.find { it.id == vibeId }
        _selectedVibe.value = vibe ?: VibeDefaults.availableVibes.first()
    }
} 