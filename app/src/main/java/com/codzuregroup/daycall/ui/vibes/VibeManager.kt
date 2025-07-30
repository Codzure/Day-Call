package com.codzuregroup.daycall.ui.vibes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VibeManager {
    private val _selectedVibe = MutableStateFlow<Vibe?>(null)
    val selectedVibe: StateFlow<Vibe?> = _selectedVibe.asStateFlow()
    
    private const val PREF_NAME = "vibe_preferences"
    private const val KEY_SELECTED_VIBE = "selected_vibe"

    fun setSelectedVibe(vibe: Vibe?) {
        _selectedVibe.value = vibe
        // TODO: Add SharedPreferences persistence here when needed
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
} 