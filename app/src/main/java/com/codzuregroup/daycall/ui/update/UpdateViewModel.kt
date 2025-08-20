package com.codzuregroup.daycall.ui.update

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codzuregroup.daycall.update.InAppUpdateManager
import com.codzuregroup.daycall.update.UpdateInfo
import com.codzuregroup.daycall.update.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    
    private val updateManager = InAppUpdateManager(application)
    
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()
    
    private val _isCheckingForUpdates = MutableStateFlow(false)
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()
    
    init {
        // Observe update state changes
        viewModelScope.launch {
            updateManager.updateState.collect { state ->
                _updateState.value = state
                
                // Auto-hide dialog for certain states
                when (state) {
                    is UpdateState.Installed -> {
                        // Keep dialog open briefly to show success
                        kotlinx.coroutines.delay(2000)
                        _showUpdateDialog.value = false
                    }
                    is UpdateState.Canceled -> {
                        // Keep dialog open for user to dismiss
                    }
                    is UpdateState.Failed -> {
                        // Keep dialog open for user to retry or dismiss
                    }
                    else -> {
                        // Keep dialog open for other states
                    }
                }
            }
        }
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            _isCheckingForUpdates.value = true
            
            try {
                val updateInfo = updateManager.checkForUpdates()
                _updateInfo.value = updateInfo
                
                if (updateInfo?.isUpdateAvailable == true) {
                    _showUpdateDialog.value = true
                }
            } catch (e: Exception) {
                // Handle error silently or show a toast
            } finally {
                _isCheckingForUpdates.value = false
            }
        }
    }
    
    fun startUpdate(updateInfo: UpdateInfo, activity: Activity) {
        when (updateInfo.updateType) {
            com.codzuregroup.daycall.update.UpdateType.FLEXIBLE -> {
                updateManager.startFlexibleUpdate(updateInfo, activity)
            }
            com.codzuregroup.daycall.update.UpdateType.IMMEDIATE -> {
                updateManager.startImmediateUpdate(updateInfo, activity)
            }
            else -> {
                // Handle other cases
            }
        }
    }
    
    fun completeUpdate() {
        updateManager.completeFlexibleUpdate()
    }
    
    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
        updateManager.resetUpdateState()
    }
    
    fun retryUpdate(activity: Activity) {
        _updateInfo.value?.let { updateInfo ->
            startUpdate(updateInfo, activity)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        updateManager.cleanup()
    }
}
