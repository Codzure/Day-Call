package com.codzuregroup.daycall.ui.update

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.codzuregroup.daycall.update.UpdateInfo
import com.codzuregroup.daycall.update.UpdateState
import com.codzuregroup.daycall.update.UpdateType
import kotlinx.coroutines.delay

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo?,
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onStartUpdate: (UpdateInfo) -> Unit,
    onCompleteUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (updateInfo == null) return
    
    Dialog(
        onDismissRequest = { 
            if (updateState !is UpdateState.Downloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = updateState !is UpdateState.Downloading,
            dismissOnClickOutside = updateState !is UpdateState.Downloading
        )
    ) {
        UpdateDialogContent(
            updateInfo = updateInfo,
            updateState = updateState,
            onDismiss = onDismiss,
            onStartUpdate = onStartUpdate,
            onCompleteUpdate = onCompleteUpdate,
            modifier = modifier
        )
    }
}

@Composable
private fun UpdateDialogContent(
    updateInfo: UpdateInfo,
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onStartUpdate: (UpdateInfo) -> Unit,
    onCompleteUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with icon
            UpdateHeader(updateState)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = when (updateState) {
                    is UpdateState.Idle -> "Update Available"
                    is UpdateState.Starting -> "Starting Update"
                    is UpdateState.Started -> "Update Started"
                    is UpdateState.Downloading -> "Downloading Update"
                    is UpdateState.Downloaded -> "Update Downloaded"
                    is UpdateState.Installed -> "Update Installed"
                    is UpdateState.Canceled -> "Update Canceled"
                    is UpdateState.Pending -> "Update Pending"
                    is UpdateState.Failed -> "Update Failed"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = when (updateState) {
                    is UpdateState.Idle -> {
                        when (updateInfo.updateType) {
                            UpdateType.FLEXIBLE -> "A new version of DayCall is available. You can continue using the app while the update downloads in the background."
                            UpdateType.IMMEDIATE -> "A critical update is available. The app will restart to install the update."
                            else -> "An update is available."
                        }
                    }
                    is UpdateState.Starting -> "Preparing to download the update..."
                    is UpdateState.Started -> "Update process has begun..."
                    is UpdateState.Downloading -> "Downloading the latest version..."
                    is UpdateState.Downloaded -> "Update downloaded successfully! Tap to install."
                    is UpdateState.Installed -> "Update installed successfully!"
                    is UpdateState.Canceled -> "Update was canceled."
                    is UpdateState.Pending -> "Update is pending..."
                    is UpdateState.Failed -> "Update failed: ${updateState.error}"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress indicator for downloading
            if (updateState is UpdateState.Downloading) {
                UpdateProgressIndicator(progress = updateState.progress)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Action buttons
            UpdateActionButtons(
                updateInfo = updateInfo,
                updateState = updateState,
                onDismiss = onDismiss,
                onStartUpdate = onStartUpdate,
                onCompleteUpdate = onCompleteUpdate
            )
        }
    }
}

@Composable
private fun UpdateHeader(updateState: UpdateState) {
    val icon = when (updateState) {
        is UpdateState.Idle -> Icons.Default.SystemUpdate
        is UpdateState.Starting -> Icons.Default.Download
        is UpdateState.Started -> Icons.Default.Download
        is UpdateState.Downloading -> Icons.Default.Download
        is UpdateState.Downloaded -> Icons.Default.CheckCircle
        is UpdateState.Installed -> Icons.Default.CheckCircle
        is UpdateState.Canceled -> Icons.Default.Cancel
        is UpdateState.Pending -> Icons.Default.Schedule
        is UpdateState.Failed -> Icons.Default.Error
    }
    
    val iconColor = when (updateState) {
        is UpdateState.Idle -> MaterialTheme.colorScheme.primary
        is UpdateState.Starting -> MaterialTheme.colorScheme.primary
        is UpdateState.Started -> MaterialTheme.colorScheme.primary
        is UpdateState.Downloading -> MaterialTheme.colorScheme.primary
        is UpdateState.Downloaded -> MaterialTheme.colorScheme.tertiary
        is UpdateState.Installed -> MaterialTheme.colorScheme.tertiary
        is UpdateState.Canceled -> MaterialTheme.colorScheme.error
        is UpdateState.Pending -> MaterialTheme.colorScheme.secondary
        is UpdateState.Failed -> MaterialTheme.colorScheme.error
    }
    
    // Animated icon
    val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (updateState is UpdateState.Downloading) 2000 else 0,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        iconColor.copy(alpha = 0.2f),
                        iconColor.copy(alpha = 0.1f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .rotate(if (updateState is UpdateState.Downloading) rotation else 0f),
            tint = iconColor
        )
    }
}

@Composable
private fun UpdateProgressIndicator(progress: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UpdateActionButtons(
    updateInfo: UpdateInfo,
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onStartUpdate: (UpdateInfo) -> Unit,
    onCompleteUpdate: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (updateState) {
            is UpdateState.Idle -> {
                // Show update buttons
                if (updateInfo.updateType == UpdateType.FLEXIBLE) {
                    // Flexible update - show both options
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Later")
                    }
                    
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStartUpdate(updateInfo)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update")
                    }
                } else {
                    // Immediate update - show only update button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStartUpdate(updateInfo)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Now")
                    }
                }
            }
            
            is UpdateState.Downloaded -> {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCompleteUpdate()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.InstallMobile,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Install Update")
                }
            }
            
            is UpdateState.Installed -> {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Great!")
                }
            }
            
            is UpdateState.Failed -> {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Dismiss")
                }
                
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStartUpdate(updateInfo)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
            
            is UpdateState.Canceled -> {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            }
            
            else -> {
                // For other states, show a loading or status button
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            }
        }
    }
}
