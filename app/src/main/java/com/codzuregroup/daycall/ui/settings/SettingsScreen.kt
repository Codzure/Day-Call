package com.codzuregroup.daycall.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codzuregroup.daycall.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    settingsManager: SettingsManager = SettingsManager.getInstance(LocalContext.current)
) {
    val vibrationEnabled by settingsManager.vibrationEnabled.collectAsStateWithLifecycle()
    val vibrationIntensity by settingsManager.vibrationIntensity.collectAsStateWithLifecycle()
    val soundEnabled by settingsManager.soundEnabled.collectAsStateWithLifecycle()
    val soundVolume by settingsManager.soundVolume.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Vibration Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Vibration",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Vibration Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { settingsManager.setVibrationEnabled(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (vibrationEnabled) {
                        Column {
                            Text(
                                text = "Vibration Intensity",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Slider(
                                value = vibrationIntensity,
                                onValueChange = { settingsManager.setVibrationIntensity(it) },
                                valueRange = 0f..1f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Intensity: ${(vibrationIntensity * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Sound Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Sound",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Sound Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { settingsManager.setSoundEnabled(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (soundEnabled) {
                        Column {
                            Text(
                                text = "Sound Volume",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Slider(
                                value = soundVolume,
                                onValueChange = { settingsManager.setSoundVolume(it) },
                                valueRange = 0f..1f,
                                steps = 19,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Volume: ${(soundVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // App Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "App Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "App Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Version Information
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow("Version Name", BuildConfig.VERSION_NAME)
                        InfoRow("Version Code", BuildConfig.VERSION_CODE.toString())
                        InfoRow("Build Number", BuildConfig.BUILD_NUMBER)
                        InfoRow("Build Date", formatBuildDate(BuildConfig.BUILD_DATE))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatBuildDate(timestamp: String): String {
    return try {
        val date = java.time.Instant.ofEpochMilli(timestamp.toLong())
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        date.atZone(java.time.ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        "Unknown"
    }
} 