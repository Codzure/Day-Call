package com.codzuregroup.daycall.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codzuregroup.daycall.DayCallApplication
import com.codzuregroup.daycall.alarm.AlarmPermissionHelper
import com.codzuregroup.daycall.alarm.AlarmReliabilityStatus
import com.codzuregroup.daycall.ui.components.DayCallCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmReliabilityScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as DayCallApplication
    val reliabilityManager = application.getAlarmReliabilityManager()
    val permissionHelper = remember { AlarmPermissionHelper(context) }
    
    var reliabilityStatus by remember { mutableStateOf<AlarmReliabilityStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        reliabilityStatus = reliabilityManager.getReliabilityStatus()
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarm Reliability") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    reliabilityStatus?.let { status ->
                        ReliabilityOverviewCard(status)
                    }
                }
            }
            
            item {
                reliabilityStatus?.let { status ->
                    ReliabilityDetailsCard(
                        status = status,
                        onFixExactAlarm = { permissionHelper.requestExactAlarmPermission() },
                        onFixBatteryOptimization = { permissionHelper.requestBatteryOptimizationExemption() },
                        onForceReschedule = { reliabilityManager.forceRescheduleAllAlarms() }
                    )
                }
            }
            
            item {
                ReliabilityTipsCard()
            }
            
            item {
                TestAlarmCard(
                    onTestAlarm = {
                        // TODO: Implement test alarm functionality
                    }
                )
            }
        }
    }
}

@Composable
fun ReliabilityOverviewCard(status: AlarmReliabilityStatus) {
    DayCallCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val (icon, color, text) = when {
                    status.isFullyReliable -> Triple(
                        Icons.Default.CheckCircle,
                        Color(0xFF4CAF50),
                        "Excellent"
                    )
                    status.reliabilityScore >= 0.7f -> Triple(
                        Icons.Default.Warning,
                        Color(0xFFFF9800),
                        "Good"
                    )
                    else -> Triple(
                        Icons.Default.Error,
                        Color(0xFFF44336),
                        "Needs Attention"
                    )
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = "Alarm Reliability",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reliability score bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reliability Score",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${(status.reliabilityScore * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { status.reliabilityScore },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = when {
                        status.reliabilityScore >= 0.8f -> Color(0xFF4CAF50)
                        status.reliabilityScore >= 0.6f -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            
            if (!status.isFullyReliable) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Some settings need attention to ensure alarms ring reliably",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReliabilityDetailsCard(
    status: AlarmReliabilityStatus,
    onFixExactAlarm: () -> Unit,
    onFixBatteryOptimization: () -> Unit,
    onForceReschedule: () -> Unit
) {
    DayCallCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4
    ) {
        Column {
            Text(
                text = "Reliability Checks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ReliabilityCheckItem(
                title = "Exact Alarm Permission",
                description = "Allows scheduling precise alarms",
                isOk = status.exactAlarmPermission,
                onFix = onFixExactAlarm
            )
            
            ReliabilityCheckItem(
                title = "Battery Optimization",
                description = "App excluded from battery optimization",
                isOk = status.batteryOptimizationDisabled,
                onFix = onFixBatteryOptimization
            )
            
            ReliabilityCheckItem(
                title = "System Alarm Capability",
                description = "System allows exact alarm scheduling",
                isOk = status.canScheduleExactAlarms,
                onFix = null
            )
            
            ReliabilityCheckItem(
                title = "Backup Alarms",
                description = "Secondary alarm system enabled",
                isOk = status.backupAlarmsEnabled,
                onFix = null
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onForceReschedule,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Force Reschedule All Alarms")
            }
        }
    }
}

@Composable
fun ReliabilityCheckItem(
    title: String,
    description: String,
    isOk: Boolean,
    onFix: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isOk) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (!isOk && onFix != null) {
            TextButton(onClick = onFix) {
                Text("Fix")
            }
        }
    }
}

@Composable
fun ReliabilityTipsCard() {
    DayCallCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4
    ) {
        Column {
            Text(
                text = "Reliability Tips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val tips = listOf(
                "Keep your device charged or plugged in overnight",
                "Don't force-close the Day Call app",
                "Allow the app to run in the background",
                "Disable battery optimization for Day Call",
                "Grant exact alarm permissions when prompted",
                "Test your alarms before relying on them"
            )
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TestAlarmCard(onTestAlarm: () -> Unit) {
    DayCallCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4
    ) {
        Column {
            Text(
                text = "Test Alarm System",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Test if alarms work properly on your device by setting a test alarm for 1 minute from now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onTestAlarm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Test Alarm")
            }
        }
    }
}