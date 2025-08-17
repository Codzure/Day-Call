package com.codzuregroup.daycall.alarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

class AlarmPermissionHelper(private val context: Context) {
    
    fun checkAllPermissions(): AlarmPermissionStatus {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        
        val batteryOptimizationDisabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
        
        return AlarmPermissionStatus(
            exactAlarmGranted = exactAlarmGranted,
            batteryOptimizationDisabled = batteryOptimizationDisabled
        )
    }
    
    fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d("AlarmPermissionHelper", "Requested exact alarm permission")
            } catch (e: Exception) {
                Log.e("AlarmPermissionHelper", "Failed to request exact alarm permission", e)
                // Fallback to general alarm settings
                openAlarmSettings()
            }
        }
    }
    
    fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d("AlarmPermissionHelper", "Requested battery optimization exemption")
            } catch (e: Exception) {
                Log.e("AlarmPermissionHelper", "Failed to request battery optimization exemption", e)
                // Fallback to battery optimization settings
                openBatteryOptimizationSettings()
            }
        }
    }
    
    private fun openAlarmSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AlarmPermissionHelper", "Failed to open app settings", e)
        }
    }
    
    private fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AlarmPermissionHelper", "Failed to open battery optimization settings", e)
            openAlarmSettings()
        }
    }
}

@Composable
fun AlarmPermissionDialog(
    onDismiss: () -> Unit,
    onGrantPermissions: () -> Unit
) {
    val context = LocalContext.current
    val permissionHelper = remember { AlarmPermissionHelper(context) }
    val permissionStatus = remember { mutableStateOf(permissionHelper.checkAllPermissions()) }
    
    LaunchedEffect(Unit) {
        permissionStatus.value = permissionHelper.checkAllPermissions()
    }
    
    if (!permissionStatus.value.allPermissionsGranted) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Alarm Permissions Required") },
            text = {
                Text(
                    "To ensure your alarms ring on time even when the app is closed or your device is sleeping, " +
                    "please grant the following permissions:\n\n" +
                    "${if (!permissionStatus.value.exactAlarmGranted) "• Schedule exact alarms\n" else ""}" +
                    "${if (!permissionStatus.value.batteryOptimizationDisabled) "• Disable battery optimization\n" else ""}" +
                    "\nWithout these permissions, alarms may be delayed or not ring at all."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!permissionStatus.value.exactAlarmGranted) {
                            permissionHelper.requestExactAlarmPermission()
                        } else if (!permissionStatus.value.batteryOptimizationDisabled) {
                            permissionHelper.requestBatteryOptimizationExemption()
                        }
                        onGrantPermissions()
                    }
                ) {
                    Text("Grant Permissions")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        )
    }
}