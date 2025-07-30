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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.codzuregroup.daycall.data.AlarmEntity
import com.codzuregroup.daycall.ui.components.DayCallCard
import com.codzuregroup.daycall.ui.components.FloatingCard
import com.codzuregroup.daycall.ui.components.GradientCard
import com.codzuregroup.daycall.ui.theme.*
import com.codzuregroup.daycall.ui.challenges.ChallengeType
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    onBackPressed: () -> Unit,
    onSaveAlarm: (AlarmEntity) -> Unit,
    onTestSound: (String) -> Unit
) {
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var alarmLabel by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(emptySet<DayOfWeek>()) }
    var selectedChallenge by remember { mutableStateOf(ChallengeType.MATH) }
    var selectedSound by remember { mutableStateOf("Ascent Braam") }

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
            TimePickerCard(selectedTime, onTimeClick = { showTimePicker = true })

            // Label Card
            Card(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = alarmLabel,
                    onValueChange = { alarmLabel = it },
                    label = { Text("Alarm Label") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Label, contentDescription = null) }
                )
            }

            // Repeat Days
            Section(title = "Repeat", icon = Icons.Outlined.Repeat) {
                DaySelector(selectedDays, onDaySelected = { day ->
                    selectedDays = if (selectedDays.contains(day)) {
                        selectedDays - day
                    } else {
                        selectedDays + day
                    }
                })
            }

            // Challenge Type
            Section(title = "Challenge", icon = Icons.Outlined.Extension) {
                ChallengeSelector(selectedChallenge, onChallengeSelected = { selectedChallenge = it })
            }

            // Sound Picker
            Section(title = "Sound", icon = Icons.Outlined.MusicNote) {
                SoundSelector(selectedSound, onSoundSelected = { selectedSound = it }, onTestSound = onTestSound)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showTimePicker) {
            TimePickerDialog(
                initialTime = selectedTime,
                onDismiss = {/* showTimeKicker = false*/ },
                onConfirm = { time ->
                    selectedTime = time
                    showTimePicker = false
                }
            )
        }
    }
}

@Composable
fun TimePickerCard(time: LocalTime, onTimeClick: () -> Unit) {
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
                text = time.format(DateTimeFormatter.ofPattern("hh:mm")),
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold, fontSize = 80.sp),
                color = Color.White
            )
            Text(
                text = time.format(DateTimeFormatter.ofPattern("a")),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DaySelector(selectedDays: Set<DayOfWeek>, onDaySelected: (DayOfWeek) -> Unit) {
    val haptic = LocalHapticFeedback.current
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(DayOfWeek.values()) { day ->
            val isSelected = selectedDays.contains(day)
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
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChallengeSelector(selectedChallenge: ChallengeType, onChallengeSelected: (ChallengeType) -> Unit) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChallengeType.values().forEach { challenge ->
            val isSelected = selectedChallenge == challenge
            FilterChip(
                selected = isSelected,
                onClick = { onChallengeSelected(challenge) },
                label = { Text(challenge.displayName) },
                leadingIcon = {
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null)
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SoundSelector(selectedSound: String, onSoundSelected: (String) -> Unit, onTestSound: (String) -> Unit) {
    val sounds = listOf(
        "Ascent Braam", "Astral Creepy", "Cinematic Whoosh", "Dark Future", "Downfall", "Elemental Magic",
        "Labyrinth", "Large Underwater", "Rainy Day", "Relaxing Guitar", "Reliable Safe", "Riser Hit",
        "Riser Wildfire", "Sci-Fi Circuits Hum", "Stab F", "Traimory Mega Horn"
    )

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
                    IconButton(onClick = { onTestSound(sound) }) {
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
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(initialTime.hour, initialTime.minute, false)

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


