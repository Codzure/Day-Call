package com.codzuregroup.daycall.ui.alarm

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
            DelightfulFloatingActionButton(
                onClick = onAddAlarm,
                alarmCount = alarms.size
            )
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
                    DelightfulEmptyState(
                        onAddAlarm = onAddAlarm,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                if (enabledAlarms.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Active Alarms (${enabledAlarms.size})")
                    }

                    itemsIndexed(enabledAlarms) { index, alarm ->
                        DelightfulAlarmItem(
                            alarm = alarm,
                            index = index,
                            onToggle = { viewModel.toggleEnabled(alarm, !alarm.enabled) },
                            onClick = { onEditAlarm(alarm.id) }
                        )
                    }
                }

                if (disabledAlarms.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Disabled Alarms (${disabledAlarms.size})")
                    }

                    itemsIndexed(disabledAlarms) { index, alarm ->
                        DelightfulAlarmItem(
                            alarm = alarm,
                            index = index + enabledAlarms.size,
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
fun DelightfulFloatingActionButton(
    onClick: () -> Unit,
    alarmCount: Int
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    
    // Enhanced floating animation with more natural movement
    val floatAnimation by rememberInfiniteTransition(label = "float").animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    // Enhanced scale animation for press with better feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    // Gentle rotation animation for empty state
    val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    // Pulse animation for the icon
    val iconPulse by rememberInfiniteTransition(label = "icon_pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_pulse"
    )
    
    FloatingActionButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        containerColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .graphicsLayer {
                translationY = -floatAnimation
                scaleX = scale
                scaleY = scale
                rotationZ = if (alarmCount == 0) rotation else 0f
            }
            .shadow(
                elevation = 20.dp,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (alarmCount == 0) 8.dp else 0.dp),
            modifier = Modifier.padding(horizontal = if (alarmCount == 0) 16.dp else 8.dp)
        ) {
            // Enhanced icon with pulse animation
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Alarm",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(if (alarmCount == 0) 28.dp else 24.dp)
                    .scale(iconPulse)
            )
            
            // Enhanced text for first alarm with better legibility
            if (alarmCount == 0) {
                Text(
                    text = "Start",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.sp,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun HomeAppBar(viewModel: AlarmViewModel) {
    var userName by remember { mutableStateOf("User") }
    var greeting by remember { mutableStateOf(getGreeting()) }
    
    // Update greeting every 30 minutes to keep it fresh but stable
    LaunchedEffect(Unit) {
        while (true) {
            greeting = getGreeting()
            delay(30 * 60 * 1000L) // 30 minutes
        }
    }

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
                text = greeting,
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
                    // Animated time display
                    val timeScale by rememberInfiniteTransition(label = "time").animateFloat(
                        initialValue = 1f,
                        targetValue = 1.02f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "time_scale"
                    )
                    
                    Text(
                        text = currentTime.format(timeFormatter),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.scale(timeScale)
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
                        // Animated vibe icon
                        val vibeRotation by rememberInfiniteTransition(label = "vibe_rotation").animateFloat(
                            initialValue = -10f,
                            targetValue = 10f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "vibe_rotation"
                        )
                        
                        Text(
                            text = vibe.icon,
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.rotate(vibeRotation)
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
fun DelightfulEmptyState(onAddAlarm: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    
    // Floating animation for the icon
    val floatOffset by rememberInfiniteTransition(label = "float").animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    // Scale animation for the icon
    val iconScale by rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }
    
    val entranceScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "entrance"
    )
    
    val entranceAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "alpha"
    )
    
    Column(
        modifier = modifier
            .padding(vertical = 32.dp)
            .graphicsLayer {
                scaleX = entranceScale
                scaleY = entranceScale
                alpha = entranceAlpha
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated empty state messages
        val emptyMessages = listOf(
            "🌅 Ready to Start Your Day?",
            "⏰ Time for Your First Alarm!",
            "✨ Let's Create Some Magic!",
            "🚀 Your Journey Begins Here!",
            "💫 Time to Wake Up Your Dreams!"
        )
        
        var currentMessageIndex by remember { mutableStateOf(0) }
        
        LaunchedEffect(Unit) {
            while (true) {
                delay(3000)
                currentMessageIndex = (currentMessageIndex + 1) % emptyMessages.size
            }
        }
        
        // Floating alarm icon
        Icon(
            Icons.Outlined.AlarmOff,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    translationY = -floatOffset
                    scaleX = iconScale
                    scaleY = iconScale
                },
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Animated message
        androidx.compose.animation.Crossfade(
            targetState = currentMessageIndex,
            animationSpec = tween(500)
        ) { messageIndex ->
            Text(
                text = emptyMessages[messageIndex],
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create your first alarm and transform your mornings into something extraordinary. Every great day starts with the perfect wake-up call!",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Delightful action button
        var isPressed by remember { mutableStateOf(false) }
        
        val buttonScale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "button"
        )
        
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAddAlarm()
            },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .scale(buttonScale)
                .shadow(
                    elevation = 16.dp,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Create My First Alarm",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.sp,
                        fontSize = 16.sp
                    )
                )
                Text(
                    "✨",
                    fontSize = 16.sp
                )
            }
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
fun DelightfulAlarmItem(
    alarm: AlarmEntity,
    index: Int,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isToggling by remember { mutableStateOf(false) }
    
    // Staggered entrance animation
    val visibilityDelay = (index * 150).toLong()
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(visibilityDelay)
        isVisible = true
    }
    
    // Slide in animation
    val slideOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 300f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slide"
    )
    
    // Gentle floating animation
    val floatOffset by rememberInfiniteTransition(label = "float").animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = maxOf(3000 + (index * 200), 100), // Ensure minimum duration
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    // Toggle animation
    val toggleScale by animateFloatAsState(
        targetValue = if (isToggling) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { isToggling = false },
        label = "toggle"
    )
    
    // Celebration animation for enabled alarms
    val celebrationRotation by rememberInfiniteTransition(label = "celebration").animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (alarm.enabled) 1000 else 100, // Prevent zero duration
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "celebration"
    )
    
    DelightfulAlarmItemContent(
        alarm = alarm,
        modifier = Modifier
            .graphicsLayer {
                translationX = slideOffset
                translationY = -floatOffset
                scaleX = toggleScale
                scaleY = toggleScale
                rotationZ = celebrationRotation
                alpha = if (isVisible) 1f else 0f
            },
        onToggle = {
            isToggling = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onToggle()
        },
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
    )
}

@Composable
fun DelightfulAlarmItemContent(
    alarm: AlarmEntity,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
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
    
    // Animated background color
    val backgroundColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = if (alarm.enabled) 1f else 0.6f
        ),
        animationSpec = tween(500),
        label = "background"
    )
    
    DayCallCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        background = backgroundColor,
        elevation = if (alarm.enabled) 8 else 2
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated alarm icon - smaller on narrow devices
            val iconScale by rememberInfiniteTransition(label = "icon").animateFloat(
                initialValue = 1f,
                targetValue = if (alarm.enabled) 1.1f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "icon_scale"
            )
            
            Text(
                text = if (alarm.enabled) "⏰" else "⏰",
                fontSize = 24.sp,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = if (alarm.enabled) 0.2f else 0.1f),
                        CircleShape
                    )
                    .padding(6.dp)
                    .scale(iconScale)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 20.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (timeFormat == TimeFormat.HOUR_12) {
                        Text(
                            text = period,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = "${alarm.label ?: "Alarm"} • ${formatRepeatDays(alarm.repeatDays)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Show vibe information with animation - more compact
                val vibe = VibeDefaults.availableVibes.find { it.id == alarm.vibe }
                if (vibe != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        val vibeScale by rememberInfiniteTransition(label = "vibe").animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2500, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "vibe_scale"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .scale(vibeScale)
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
                                fontSize = 6.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = vibe.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Delightful toggle switch
            DelightfulSwitch(
                checked = alarm.enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
fun DelightfulSwitch(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    // Thumb animation
    val thumbScale by animateFloatAsState(
        targetValue = if (checked) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumb"
    )
    
    // Track color animation
    val trackColor by animateColorAsState(
        targetValue = if (checked) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(300),
        label = "track"
    )
    
    Switch(
        checked = checked,
        onCheckedChange = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onCheckedChange() 
        },
        modifier = Modifier
            .scale(thumbScale)
            .size(width = 48.dp, height = 32.dp),
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = trackColor,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = trackColor
        )
    )
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
    val now = LocalTime.now()
    val hour = now.hour
    val minute = now.minute
    
    // Create deterministic index based on time (changes every 30 minutes)
    val timeSlot = (hour * 2) + (minute / 30)
    
    val playfulGreetings = when (hour) {
        in 0..4 -> listOf(
            "🌙 Sweet Dreams",
            "✨ Night Owl Mode",
            "🌟 Burning the Midnight Oil",
            "🦉 Late Night Vibes",
            "💫 Dreaming Time"
        )
        in 5..6 -> listOf(
            "🌅 Early Bird!",
            "☀️ Rise & Shine",
            "🐓 Sunrise Warrior",
            "⭐ Dawn Breaker",
            "🌄 Morning Glory"
        )
        in 7..11 -> listOf(
            "☕ Good Morning",
            "🌻 Fresh Start",
            "🦋 Morning Magic",
            "🌈 New Day Energy",
            "☀️ Sunshine Time"
        )
        in 12..17 -> listOf(
            "🌞 Good Afternoon",
            "⚡ Midday Power",
            "🌺 Afternoon Vibes",
            "🎯 Peak Performance",
            "🚀 Afternoon Drive"
        )
        else -> listOf(
            "🌅 Good Evening",
            "🌙 Twilight Time",
            "⭐ Evening Glow",
            "🎭 Night Begins",
            "🌃 City Lights"
        )
    }
    
    // Use deterministic selection based on time slot
    return playfulGreetings[timeSlot % playfulGreetings.size]
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
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        ).toSpanStyle().copy(fontSize = 28.sp)
                    ) { append(name.first()) }
                    append(" ")
                    withStyle(
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        ).toSpanStyle().copy(fontSize = 20.sp)
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
