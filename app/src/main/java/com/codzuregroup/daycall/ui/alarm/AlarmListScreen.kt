package com.codzuregroup.daycall.ui.alarm

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codzuregroup.daycall.data.AlarmEntity
import com.codzuregroup.daycall.ui.AlarmViewModel
import com.codzuregroup.daycall.ui.components.DayCallCard
import com.codzuregroup.daycall.ui.components.GradientCard
import com.codzuregroup.daycall.ui.theme.*
import com.codzuregroup.daycall.ui.vibes.VibeDefaults
import com.codzuregroup.daycall.ui.vibes.VibeManager
import com.codzuregroup.daycall.ui.vibes.Vibe
import com.codzuregroup.daycall.ui.login.UserManager
import com.codzuregroup.daycall.ui.settings.SettingsManager
import com.codzuregroup.daycall.ui.settings.TimeFormat
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.format.TextStyle
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmViewModel,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onAlarmRinging: (String) -> Unit,
    showBottomNavigation: Boolean = true
) {
    val alarms by viewModel.alarms.collectAsState()

    val enabledAlarms = alarms.filter { it.enabled }
    val disabledAlarms = alarms.filter { !it.enabled }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAlarm,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(
                    12.dp,
                    spotColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Alarm",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                HomeAppBar(viewModel = viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                CurrentTimeCard(alarms = enabledAlarms)


            }

            if (alarms.isEmpty()) {
                item {
                    EmptyAlarmState(
                        onAddAlarm = onAddAlarm,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                if (enabledAlarms.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Active Alarms (${enabledAlarms.size})")
                    }

                    items(enabledAlarms) { alarm ->
                        RealAlarmItem(
                            alarm = alarm,
                            onToggle = { viewModel.toggleEnabled(alarm, !alarm.enabled) },
                            onClick = { onEditAlarm(alarm.id) }
                        )
                    }
                }

                if (disabledAlarms.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Disabled Alarms (${disabledAlarms.size})")
                    }

                    items(disabledAlarms) { alarm ->
                        RealAlarmItem(
                            alarm = alarm,
                            onToggle = { viewModel.toggleEnabled(alarm, !alarm.enabled) },
                            onClick = { onEditAlarm(alarm.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeAppBar(viewModel: AlarmViewModel) {
    var userName by remember { mutableStateOf("User") }

    LaunchedEffect(Unit) {
        UserManager.getCurrentUser().collect { name ->
            userName = name ?: "User"
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = getGreeting(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val waveTransition = rememberInfiniteTransition(label = "wave")
            val angle by waveTransition.animateFloat(
                initialValue = -18f,
                targetValue = 18f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "angle"
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Hello, $userName ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "👋",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.rotate(angle),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CurrentTimeCard(alarms: List<AlarmEntity>) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val timeFormat by settingsManager.timeFormat.collectAsStateWithLifecycle()

    val timeFormatter = when (timeFormat) {
        TimeFormat.HOUR_12 -> DateTimeFormatter.ofPattern("hh:mm")
        TimeFormat.HOUR_24 -> DateTimeFormatter.ofPattern("HH:mm")
        else -> DateTimeFormatter.ofPattern("hh:mm")
    }
    val periodFormatter = DateTimeFormatter.ofPattern("a")
    var selectedVibe by remember { mutableStateOf<Vibe?>(null) }

    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000) // Update every second
        }
    }

    LaunchedEffect(Unit) {
        selectedVibe = VibeManager.getSelectedVibeForAlarm()
    }

    // React to vibe changes
    LaunchedEffect(Unit) {
        VibeManager.selectedVibe.collect { vibe ->
            selectedVibe = vibe ?: VibeDefaults.availableVibes.first()
        }
    }

    val nextAlarm = alarms.filter { it.enabled }.minByOrNull { it.toLocalTime() }

    fun getTimeUntilAlarm(alarmTime: LocalTime): String {
        var hours = alarmTime.hour - currentTime.hour
        var minutes = alarmTime.minute - currentTime.minute

        if (minutes < 0) {
            hours -= 1
            minutes += 60
        }
        if (hours < 0) {
            hours += 24
        }

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Now"
        }
    }

    GradientCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        gradient = Brush.linearGradient(
            colors = selectedVibe?.let { vibe ->
                listOf(vibe.gradientStart, vibe.gradientEnd)
            } ?: listOf(GradientStart, GradientEnd)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // Day selection row: full current day on the left, remaining day initials on the right
                DaySelectionRow(
                    modifier = Modifier.fillMaxWidth(),
                    textColor = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = currentTime.format(timeFormatter),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    if (timeFormat == TimeFormat.HOUR_12) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentTime.format(periodFormatter),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    selectedVibe?.let { vibe ->
                        Text(
                            text = vibe.icon,
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (nextAlarm != null) {
                    // Show next alarm info
                    Text(
                        text = "Next: ${nextAlarm.toLocalTime().format(timeFormatter)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "in ${getTimeUntilAlarm(nextAlarm.toLocalTime())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    // Show empty state with current time info
                    Column {
                        Text(
                            text = "Current Time",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "No alarms set",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun HomeBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .shadow(
                elevation = 20.dp,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        val items = listOf(
            "Alarms" to Icons.Outlined.Alarm,
            "Vibes" to Icons.Outlined.Star,
            "Todo" to Icons.Outlined.CheckCircle,
            "Settings" to Icons.Outlined.Settings
        )

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(item.second, contentDescription = item.first) },
                label = { Text(item.first) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun EmptyAlarmState(onAddAlarm: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.AlarmOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Alarms Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the '+' button to create your first alarm and start your day with a vibe.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddAlarm,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text("Add First Alarm")
        }
    }
}

@Composable
fun RealAlarmItem(alarm: AlarmEntity, onToggle: () -> Unit, onClick: () -> Unit) {
    val time = alarm.toLocalTime()
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val timeFormat by settingsManager.timeFormat.collectAsStateWithLifecycle()
    
    val timeFormatter = when (timeFormat) {
        TimeFormat.HOUR_12 -> DateTimeFormatter.ofPattern("hh:mm")
        TimeFormat.HOUR_24 -> DateTimeFormatter.ofPattern("HH:mm")
        else -> DateTimeFormatter.ofPattern("hh:mm")
    }
    val periodFormatter = DateTimeFormatter.ofPattern("a")
    
    val formattedTime = time.format(timeFormatter)
    val period = time.format(periodFormatter)
    
    DayCallCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        background = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = if (alarm.enabled) 1f else 0.6f
        ),
        elevation = if (alarm.enabled) 8 else 2
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏰",
                fontSize = 28.sp,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (timeFormat == TimeFormat.HOUR_12) {
                        Text(
                            text = period,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                }
                
                Text(
                    text = "${alarm.label ?: "Alarm"} • ${formatRepeatDays(alarm.repeatDays)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Show vibe information
                val vibe = VibeDefaults.availableVibes.find { it.id == alarm.vibe }
                if (vibe != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            vibe.gradientStart,
                                            vibe.gradientEnd
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = vibe.icon,
                                fontSize = 8.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = vibe.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
    }
}

fun formatRepeatDays(repeatDays: Int): String {
    if (repeatDays == 0b1111111) return "Every day"
    if (repeatDays == 0b1111100) return "Weekdays"
    if (repeatDays == 0b0000011) return "Weekends"
    if (repeatDays == 0) return "Once"

    val days = DayOfWeek.values()
    val selectedDays = days.filter { (repeatDays and (1 shl it.ordinal)) != 0 }
    return selectedDays.joinToString(", ") { it.name.substring(0, 3) }
}



private fun getGreeting(): String {
    return when (LocalTime.now().hour) {
        in 0..5 -> "Good Night"
        in 6..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@Composable
private fun DaySelectionRow(
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfWeek) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Full day name with emphasized first letter, animated crossfade on change
        androidx.compose.animation.Crossfade(
            targetState = selectedDay,
            animationSpec = tween(durationMillis = 200)
        ) { day ->
            val name = day.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val annotated = buildAnnotatedString {
                if (name.isNotEmpty()) {
                    withStyle(
                        MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        ).toSpanStyle().copy(fontSize = 42.sp)
                    ) { append(name.first()) }
                    append(" ")
                    withStyle(
                        MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        ).toSpanStyle()
                    ) { append(name.drop(1)) }
                }
            }
            Text(text = annotated)
        }

        // Right: Remaining day initials (excluding selected), clickable to switch selection
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            DayOfWeek.values()
                .filter { it != selectedDay }
                .forEach { day ->
                    val label = day.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .border(1.dp, textColor.copy(alpha = 0.5f), CircleShape)
                            .clickable { selectedDay = day },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = textColor,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
        }
    }
}
