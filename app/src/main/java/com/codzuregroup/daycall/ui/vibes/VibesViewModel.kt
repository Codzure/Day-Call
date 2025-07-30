package com.codzuregroup.daycall.ui.vibes

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VibesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VibesUiState())
    val uiState: StateFlow<VibesUiState> = _uiState.asStateFlow()

    init {
        loadVibes()
        // Initialize with currently selected vibe if any
        val currentVibe = VibeManager.getSelectedVibe()
        if (currentVibe != null) {
            selectVibe(currentVibe)
        }
    }

    private fun loadVibes() {
        _uiState.update { currentState ->
            currentState.copy(
                vibes = VibeDefaults.availableVibes,
                isLoading = false
            )
        }
    }

    fun handleEvent(event: VibesEvent) {
        when (event) {
            is VibesEvent.SelectVibe -> {
                selectVibe(event.vibe)
            }
            is VibesEvent.UnlockVibe -> {
                unlockVibe(event.vibeId)
            }
            VibesEvent.RefreshVibes -> {
                loadVibes()
            }
        }
    }

    private fun selectVibe(vibe: Vibe) {
        // Update VibeManager immediately
        VibeManager.setSelectedVibe(vibe)
        
        _uiState.update { currentState ->
            val updatedVibes = currentState.vibes.map { existingVibe ->
                existingVibe.copy(isSelected = existingVibe.id == vibe.id)
            }
            currentState.copy(
                vibes = updatedVibes,
                selectedVibe = vibe
            )
        }
    }

    private fun unlockVibe(vibeId: String) {
        _uiState.update { currentState ->
            val updatedVibes = currentState.vibes.map { vibe ->
                if (vibe.id == vibeId) {
                    vibe.copy(isUnlocked = true)
                } else {
                    vibe
                }
            }
            currentState.copy(vibes = updatedVibes)
        }
    }

    fun getSelectedVibe(): Vibe? {
        return _uiState.value.selectedVibe
    }

    fun isVibeSelected(vibeId: String): Boolean {
        return _uiState.value.selectedVibe?.id == vibeId
    }
} 