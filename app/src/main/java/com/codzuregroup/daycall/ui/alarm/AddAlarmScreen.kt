package com.codzuregroup.daycall.ui.alarm

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codzuregroup.daycall.data.AlarmEntity
import com.codzuregroup.daycall.ui.components.DayCallCard
import com.codzuregroup.daycall.ui.components.FloatingCard
import com.codzuregroup.daycall.ui.components.GradientCard
import com.codzuregroup.daycall.ui.theme.*
import com.codzuregroup.daycall.ui.challenges.ChallengeType
import com.codzuregroup.daycall.ui.vibes.VibeDefaults
import com.codzuregroup.daycall.ui.vibes.VibeManager
import com.codzuregroup.daycall.ui.vibes.VibeManager.getSelectedVibeForAlarm
import com.codzuregroup.daycall.audio.AudioManager
import com.codzuregroup.daycall.audio.AudioCategory
import com.codzuregroup.daycall.ui.settings.SettingsManager
import com.codzuregroup.daycall.ui.settings.TimeFormat
import com.codzuregroup.daycall.vibration.VibrationManager
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    onBackPressed: () -> Unit,
    onSaveAlarm: (AlarmEntity) -> Unit,
    onTestSound: (String) -> Unit = {},
    audioManager: AudioManager? = null
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val vibrationManager = remember { VibrationManager(context) }
    val timeFormat by settingsManager.timeFormat.collectAsStateWithLifecycle()
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var alarmLabel by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(emptySet<DayOfWeek>()) }
    var selectedChallenge by remember { mutableStateOf(ChallengeType.MATH) }
    var selectedSound by remember { mutableStateOf("Ascent Braam") }
    var selectedVibe by remember { 
        mutableStateOf(getSelectedVibeForAlarm().id) 
    }
    
    // Update selected vibe when VibeManager changes
    LaunchedEffect(Unit) {
        selectedVibe = getSelectedVibeForAlarm().id
    }

    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Alarm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { /* Handle preview */ }) {
                        Text("Preview", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            FloatingCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val alarm = AlarmEntity(
                            hour = selectedTime.hour,
                            minute = selectedTime.minute,
                            label = alarmLabel,
                            repeatDays = selectedDays.toBitmask(),
                            challengeType = selectedChallenge.name,
                            vibe = selectedVibe,
                            sound = selectedSound,
                            enabled = true
                        )
                        onSaveAlarm(alarm)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Alarm", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Time Picker Card
            TimePickerCard(
                time = selectedTime, 
                onTimeClick = { 
                    vibrationManager.vibrateButtonPress()
                    showTimePicker = true 
                },
                timeFormat = timeFormat
            )

            // Label Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                OutlinedTextField(
                    value = alarmLabel,
                    onValueChange = { alarmLabel = it },
                    label = { Text("Alarm Label") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Label, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Repeat Days
            Section(title = "Repeat", icon = Icons.Outlined.Repeat) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Select days to repeat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        DaySelector(selectedDays, onDaySelected = { day ->
                            selectedDays = if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                        })
                    }
                }
            }

            // Challenge Type
            Section(title = "Challenge", icon = Icons.Outlined.Extension) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Choose a challenge to dismiss alarm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        ChallengeSelector(selectedChallenge, onChallengeSelected = { selectedChallenge = it })
                    }
                }
            }

            // Vibe Selection
            Section(title = "Vibe", icon = Icons.Outlined.Star) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Choose the mood for your alarm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        VibeSelector(selectedVibe, onVibeSelected = { selectedVibe = it })
                        
                        // Show current selection
                        val currentVibe = VibeDefaults.availableVibes.find { it.id == selectedVibe }
                        currentVibe?.let { vibe ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selected: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = vibe.icon,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = vibe.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Sound Picker
            Section(title = "Sound", icon = Icons.Outlined.MusicNote) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Choose your alarm sound",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        SoundSelector(
                            selectedSound, 
                            onSoundSelected = { selectedSound = it }, 
                            onTestSound = onTestSound,
                            audioManager = audioManager
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showTimePicker) {
            TimePickerDialog(
                initialTime = selectedTime,
                onDismiss = { showTimePicker = false },
                onConfirm = { time ->
                    selectedTime = time
                    showTimePicker = false
                    vibrationManager.vibrateButtonPress()
                },
                timeFormat = timeFormat
            )
        }
    }
}

@Composable
fun TimePickerCard(
    time: LocalTime, 
    onTimeClick: () -> Unit,
    timeFormat: TimeFormat = TimeFormat.HOUR_12
) {
    GradientCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(180.dp)
            .clickable(onClick = onTimeClick),
        gradient = Brush.linearGradient(colors = listOf(GradientStart, GradientEnd))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (timeFormat) {
                    TimeFormat.HOUR_12 -> time.format(DateTimeFormatter.ofPattern("hh:mm"))
                    TimeFormat.HOUR_24 -> time.format(DateTimeFormatter.ofPattern("HH:mm"))
                },
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, fontSize = 80.sp),
                color = Color.White
            )
            if (timeFormat == TimeFormat.HOUR_12) {
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("a")),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun DaySelector(selectedDays: Set<DayOfWeek>, onDaySelected: (DayOfWeek) -> Unit) {
    val haptic = LocalHapticFeedback.current
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(DayOfWeek.values()) { day ->
            val isSelected = selectedDays.contains(day)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable {
                            onDaySelected(day)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.name.substring(0, 1),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = day.name.substring(0, 3),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChallengeSelector(selectedChallenge: ChallengeType, onChallengeSelected: (ChallengeType) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChallengeType.values().forEach { challenge ->
            val isSelected = selectedChallenge == challenge
            FilterChip(
                selected = isSelected,
                onClick = { onChallengeSelected(challenge) },
                label = { 
                    Text(
                        challenge.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    ) 
                },
                leadingIcon = {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun SoundSelector(
    selectedSound: String, 
    onSoundSelected: (String) -> Unit, 
    onTestSound: (String) -> Unit,
    audioManager: AudioManager? = null
) {
    val sounds = AudioManager.availableAudioFiles.map { it.displayName }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        modifier = Modifier.height(200.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sounds) { sound ->
            val isSelected = selectedSound == sound
            DayCallCard(
                modifier = Modifier.clickable { onSoundSelected(sound) },
                background = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sound,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = { 
                            audioManager?.previewAudio(sound, 3)
                            onTestSound(sound) 
                        }
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = "Test sound")
                    }
                }
            }
        }
    }
}

@Composable
fun Section(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

fun Set<DayOfWeek>.toBitmask(): Int {
    var mask = 0
    for (day in this) {
        mask = mask or (1 shl day.ordinal)
    }
    return mask
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    timeFormat: TimeFormat = TimeFormat.HOUR_12
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = timeFormat == TimeFormat.HOUR_24
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(32.dp),
        content = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    )
}

@Composable
fun VibeSelector(selectedVibeId: String, onVibeSelected: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(VibeDefaults.availableVibes) { vibe ->
            val isSelected = selectedVibeId == vibe.id
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .height(80.dp)
                    .clickable { onVibeSelected(vibe.id) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 8.dp else 4.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    vibe.gradientStart,
                                    vibe.gradientEnd
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = vibe.icon,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = vibe.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundPickerDialog(
    onDismiss: () -> Unit,
    onSoundSelected: (String) -> Unit,
    audioManager: AudioManager? = null
) {
    var selectedCategory by remember { mutableStateOf(AudioCategory.WAKE_UP) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Sound", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column {
                // Category tabs
                TabRow(
                    selectedTabIndex = AudioCategory.values().indexOf(selectedCategory),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    AudioCategory.values().forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            text = { 
                                Text(
                                    category.name.replace("_", " ").lowercase().capitalize(),
                                    style = MaterialTheme.typography.bodySmall
                                ) 
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sounds in selected category
                val categorySounds = AudioManager.getAudioFilesByCategory(selectedCategory)
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categorySounds) { audioFile ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onSoundSelected(audioFile.displayName)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = audioFile.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = selectedCategory.name.replace("_", " ").lowercase().capitalize(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { 
                                        audioManager?.previewAudio(audioFile.fileName, 3)
                                    }
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Preview")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


