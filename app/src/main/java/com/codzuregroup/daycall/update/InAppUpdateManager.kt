package com.codzuregroup.daycall.update

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class InAppUpdateManager(private val context: Context) {
    
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    private val _updateProgress = MutableStateFlow(0f)
    val updateProgress: StateFlow<Float> = _updateProgress.asStateFlow()
    
    private val installStateListener = InstallStateUpdatedListener { installState ->
        when (installState.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytesDownloaded = installState.bytesDownloaded()
                val totalBytesToDownload = installState.totalBytesToDownload()
                if (totalBytesToDownload > 0) {
                    val progress = bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()
                    _updateProgress.value = progress
                    _updateState.value = UpdateState.Downloading(progress)
                }
            }
            InstallStatus.DOWNLOADED -> {
                _updateState.value = UpdateState.Downloaded
            }
            InstallStatus.INSTALLED -> {
                _updateState.value = UpdateState.Installed
            }
            InstallStatus.FAILED -> {
                _updateState.value = UpdateState.Failed(installState.installErrorCode().toString())
            }
            InstallStatus.CANCELED -> {
                _updateState.value = UpdateState.Canceled
            }
            InstallStatus.PENDING -> {
                _updateState.value = UpdateState.Pending
            }
            else -> {
                // Handle other states if needed
            }
        }
    }
    
    init {
        appUpdateManager.registerListener(installStateListener)
    }
    
    suspend fun checkForUpdates(): UpdateInfo? {
        return suspendCancellableCoroutine { continuation ->
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                val updateInfo = if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    
                    UpdateInfo(
                        isUpdateAvailable = true,
                        updateType = UpdateType.FLEXIBLE,
                        priority = appUpdateInfo.updatePriority(),
                        totalBytes = appUpdateInfo.totalBytesToDownload(),
                        appUpdateInfo = appUpdateInfo
                    )
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                           appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    
                    UpdateInfo(
                        isUpdateAvailable = true,
                        updateType = UpdateType.IMMEDIATE,
                        priority = appUpdateInfo.updatePriority(),
                        totalBytes = appUpdateInfo.totalBytesToDownload(),
                        appUpdateInfo = appUpdateInfo
                    )
                } else {
                    UpdateInfo(
                        isUpdateAvailable = false,
                        updateType = UpdateType.NONE,
                        priority = 0,
                        totalBytes = 0,
                        appUpdateInfo = appUpdateInfo
                    )
                }
                
                continuation.resume(updateInfo)
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error checking for updates", exception)
                continuation.resume(null)
            }
        }
    }
    
    fun startFlexibleUpdate(updateInfo: UpdateInfo, activity: Activity) {
        if (updateInfo.updateType != UpdateType.FLEXIBLE) {
            Log.w(TAG, "Cannot start flexible update for non-flexible update type")
            return
        }
        
        try {
            appUpdateManager.startUpdateFlowForResult(
                updateInfo.appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                REQUEST_CODE_FLEXIBLE_UPDATE
            )
            _updateState.value = UpdateState.Starting
        } catch (e: Exception) {
            Log.e(TAG, "Error starting flexible update", e)
            _updateState.value = UpdateState.Failed(e.message ?: "Unknown error")
        }
    }
    
    fun startImmediateUpdate(updateInfo: UpdateInfo, activity: Activity) {
        if (updateInfo.updateType != UpdateType.IMMEDIATE) {
            Log.w(TAG, "Cannot start immediate update for non-immediate update type")
            return
        }
        
        try {
            appUpdateManager.startUpdateFlowForResult(
                updateInfo.appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                activity,
                REQUEST_CODE_IMMEDIATE_UPDATE
            )
            _updateState.value = UpdateState.Starting
        } catch (e: Exception) {
            Log.e(TAG, "Error starting immediate update", e)
            _updateState.value = UpdateState.Failed(e.message ?: "Unknown error")
        }
    }
    
    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }
    
    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
        _updateProgress.value = 0f
    }
    
    fun cleanup() {
        appUpdateManager.unregisterListener(installStateListener)
    }
    
    companion object {
        private const val TAG = "InAppUpdateManager"
        const val REQUEST_CODE_FLEXIBLE_UPDATE = 1001
        const val REQUEST_CODE_IMMEDIATE_UPDATE = 1002
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Starting : UpdateState()
    object Started : UpdateState()
    data class Downloading(val progress: Float) : UpdateState()
    object Downloaded : UpdateState()
    object Installed : UpdateState()
    object Canceled : UpdateState()
    object Pending : UpdateState()
    data class Failed(val error: String) : UpdateState()
}

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val updateType: UpdateType,
    val priority: Int,
    val totalBytes: Long,
    val appUpdateInfo: AppUpdateInfo
)

enum class UpdateType {
    NONE,
    FLEXIBLE,
    IMMEDIATE
}
