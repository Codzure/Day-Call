package com.codzuregroup.daycall.ui.alarm

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codzuregroup.daycall.audio.AudioManager
import com.codzuregroup.daycall.ui.challenges.Challenge
import com.codzuregroup.daycall.ui.challenges.ChallengeGenerator
import com.codzuregroup.daycall.ui.challenges.ChallengeType
import com.codzuregroup.daycall.ui.challenges.MathChallengeUI
import com.codzuregroup.daycall.ui.challenges.QRScanChallengeUI
import com.codzuregroup.daycall.ui.challenges.MemoryMatchChallengeUI
import com.codzuregroup.daycall.ui.challenges.ShakeChallengeUI
import com.codzuregroup.daycall.ui.challenges.MemoryChallengeUI
import com.codzuregroup.daycall.ui.challenges.PatternChallengeUI
import com.codzuregroup.daycall.ui.challenges.WordChallengeUI
import com.codzuregroup.daycall.ui.challenges.LogicChallengeUI
import com.codzuregroup.daycall.vibration.VibrationManager
import com.codzuregroup.daycall.audio.TextToSpeechManager
import com.codzuregroup.daycall.ui.settings.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import android.util.Log

data class ConfettiParticle(
    val id: Int,
    val x: Float,
    val y: Float,
    val color: Color,
    val rotation: Float,
    val scale: Float
)

fun createConfettiParticles(): List<ConfettiParticle> {
    val colors = listOf(
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Teal
        Color(0xFF45B7D1), // Blue
        Color(0xFFFFA07A), // Light Orange
        Color(0xFF98D8C8), // Mint
        Color(0xFFF7DC6F), // Yellow
        Color(0xFFBB8FCE), // Purple
        Color(0xFF85C1E9)  // Light Blue
    )
    
    return List(30) { index ->
        ConfettiParticle(
            id = index,
            x = Random.nextFloat() * 1000f,
            y = -50f - Random.nextFloat() * 200f,
            color = colors[Random.nextInt(colors.size)],
            rotation = Random.nextFloat() * 360f,
            scale = 0.5f + Random.nextFloat() * 0.5f
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmRingingScreen(
    alarmLabel: String = "Alarm",
    audioFile: String? = null,
    audioManager: AudioManager? = null,
    onDismiss: () -> Unit = {},
    onSnooze: () -> Unit = {},
    onChallengeSolved: () -> Unit = {},
    onTTSStateChanged: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val actualAudioManager = audioManager ?: remember { AudioManager(context) }
    var currentChallenge by remember { mutableStateOf<Challenge?>(null) }
    var userAnswer by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(30) }
    var volume by remember { mutableStateOf(0.3f) }
    var showVolumeWarning by remember { mutableStateOf(false) }
    
    // New state variables for challenge management
    var showNewChallengeButton by remember { mutableStateOf(false) }
    var challengeAttempts by remember { mutableStateOf(0) }
    var canDismiss by remember { mutableStateOf(false) }
    var isTTSSpeaking by remember { mutableStateOf(false) }
    
    // Feedback states
    var showCorrectFeedback by remember { mutableStateOf(false) }
    var showIncorrectFeedback by remember { mutableStateOf(false) }
    var confettiParticles by remember { mutableStateOf(listOf<ConfettiParticle>()) }
    var shakeOffset by remember { mutableStateOf(0f) }
    
    val haptic = LocalHapticFeedback.current
    val vibrationManager = remember { VibrationManager(context) }
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val vibrationEnabled by settingsManager.vibrationEnabled.collectAsStateWithLifecycle()
    val vibrationIntensity by settingsManager.vibrationIntensity.collectAsStateWithLifecycle()
    
    val textToSpeechManager = remember { TextToSpeechManager(context) }
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Feedback animations
    val shakeAnimation by animateFloatAsState(
        targetValue = shakeOffset,
        animationSpec = tween(100, easing = EaseInOut),
        label = "shake"
    )
    
    val glowAnimation by animateFloatAsState(
        targetValue = if (showCorrectFeedback) 1f else 0f,
        animationSpec = tween(500, easing = EaseInOut),
        label = "glow"
    )

    // Start playing audio when screen loads
    LaunchedEffect(Unit) {
        actualAudioManager.playAudio(audioFile)
        currentChallenge = ChallengeGenerator.getRandomChallenge()
        
        // Start urgency vibration
        if (vibrationEnabled) {
            vibrationManager.vibrateAlarmStart()
        }
    }

    // Timer countdown
    LaunchedEffect(currentChallenge) {
        timeRemaining = 30
        while (timeRemaining > 0 && !isCorrect) {
            delay(1000)
            timeRemaining--
        }
        if (timeRemaining <= 0 && !isCorrect) {
            showError = true
            challengeAttempts++
            if (vibrationEnabled) {
                vibrationManager.vibrateChallengeTimeout()
            }
            // Show new challenge button instead of auto-generating
            showNewChallengeButton = true
        }
    }

    // Volume increase over time - more gradual and responsive
    LaunchedEffect(Unit) {
        var volumeIncreaseInterval = 3000L // Start with 3 seconds
        var urgencyLevel = 0
        
        while (!canDismiss) {
            delay(volumeIncreaseInterval)
            if (!canDismiss) { // Double check before increasing volume
                actualAudioManager.increaseVolume()
                urgencyLevel++
                
                // Increase urgency and frequency over time
                if (urgencyLevel >= 5) {
                    volumeIncreaseInterval = 2000L // Every 2 seconds after 15 seconds
                }
                if (urgencyLevel >= 10) {
                    volumeIncreaseInterval = 1000L // Every 1 second after 25 seconds
                }
                if (urgencyLevel >= 15) {
                    volumeIncreaseInterval = 500L // Every 0.5 seconds after 30 seconds
                }
                
                if (vibrationEnabled) {
                    vibrationManager.vibrateVolumeIncrease()
                }
                
                Log.d("AlarmRingingScreen", "Volume increased - Level: $urgencyLevel, Interval: ${volumeIncreaseInterval}ms")
            }
        }
    }
    
    // Urgency vibration that increases over time - more gradual
    LaunchedEffect(Unit) {
        var urgencyLevel = 0
        var vibrationInterval = 8000L // Start with 8 seconds
        
        while (!canDismiss) {
            delay(vibrationInterval)
            if (!canDismiss) {
                urgencyLevel++
                
                // Increase vibration frequency over time
                if (urgencyLevel >= 3) {
                    vibrationInterval = 6000L // Every 6 seconds after 24 seconds
                }
                if (urgencyLevel >= 6) {
                    vibrationInterval = 4000L // Every 4 seconds after 36 seconds
                }
                if (urgencyLevel >= 9) {
                    vibrationInterval = 2000L // Every 2 seconds after 48 seconds
                }
                
                if (vibrationEnabled) {
                    val intensity = (0.3f + (urgencyLevel * 0.05f)).coerceAtMost(1.0f)
                    vibrationManager.vibrateAlarmUrgency(intensity)
                    Log.d("AlarmRingingScreen", "Urgency vibration - Level: $urgencyLevel, Intensity: $intensity")
                }
            }
        }
    }
    
    // Update volume state when audio manager volume changes
    LaunchedEffect(actualAudioManager.volume) {
        actualAudioManager.volume.collect { newVolume ->
            volume = newVolume
        }
    }

    // Stop audio when challenge is solved
    LaunchedEffect(canDismiss) {
        if (canDismiss) {
            actualAudioManager.stopAudio()
        }
    }
    
    // Feedback functions
    fun showCorrectAnswerFeedback(onComplete: (() -> Unit)? = null) {
        showCorrectFeedback = true
        confettiParticles = createConfettiParticles()
        isTTSSpeaking = true
        onTTSStateChanged?.invoke(true)
        
        // Speak celebration message and wait for completion
        val userName = settingsManager.userName.value
        textToSpeechManager.speakCelebration(userName) {
            // TTS completed, now we can proceed with dismissal
            isTTSSpeaking = false
            onTTSStateChanged?.invoke(false)
            scope.launch {
                // Keep confetti visible for a bit longer after TTS completes
                delay(1000)
                showCorrectFeedback = false
                confettiParticles = emptyList()
                onComplete?.invoke()
            }
        }
        
        if (vibrationEnabled) {
            vibrationManager.vibrateCorrectAnswer()
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    fun showIncorrectAnswerFeedback() {
        showIncorrectFeedback = true
        
        if (vibrationEnabled) {
            vibrationManager.vibrateIncorrectAnswer()
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        
        // Shake animation
        scope.launch {
            repeat(3) {
                shakeOffset = 10f
                delay(50)
                shakeOffset = -10f
                delay(50)
            }
            shakeOffset = 0f
            showIncorrectFeedback = false
        }
    }

    // Function to generate new challenge
    fun generateNewChallenge() {
        currentChallenge = ChallengeGenerator.getRandomChallenge()
        userAnswer = ""
        showError = false
        // Don't hide the button - keep it visible until challenge is solved
        // showNewChallengeButton = false
        timeRemaining = 30
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    // Cleanup when leaving
    DisposableEffect(Unit) {
        onDispose {
            actualAudioManager.stopAudio()
            vibrationManager.stopVibration()
            textToSpeechManager.stop()
            onTTSStateChanged?.invoke(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Deep blue
                        Color(0xFF0D47A1), // Darker blue
                        Color(0xFF1565C0)  // Medium blue
                    )
                )
            )
            // Prevent clicking outside by making the entire screen clickable
            .clickable(enabled = false) { }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Alarm Label
            Text(
                text = alarmLabel,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // TTS Speaking Indicator
            if (isTTSSpeaking) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Animated speaking icon
                        val speakingScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = EaseInOut),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "speaking_scale"
                        )
                        
                        Text(
                            text = "🔊",
                            fontSize = 16.sp,
                            modifier = Modifier.scale(speakingScale)
                        )
                        
                        Text(
                            text = "Speaking celebration message...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Volume indicator with restriction message
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (volume > 0.5f) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = null,
                        tint = if (volume > 0.8f) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Volume: ${(volume * 100).toInt()}%",
                        color = if (volume > 0.8f) Color.Red else Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (volume > 0.8f) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(volume)
                                .background(
                                    color = if (volume > 0.8f) Color.Red else Color(0xFFFF9800), // Orange color
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
                
                if (!canDismiss) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (volume > 0.8f) 
                            "⚠️ URGENT: Volume will keep increasing!" 
                        else 
                            "Volume will increase until you solve the challenge!",
                        color = if (volume > 0.8f) Color.Red else Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontWeight = if (volume > 0.8f) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Challenge Card
            currentChallenge?.let { challenge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .graphicsLayer {
                            translationX = shakeAnimation
                            shadowElevation = glowAnimation * 20f
                        }
                        .shadow(
                            elevation = if (showCorrectFeedback) 20.dp else 4.dp,
                            spotColor = if (showCorrectFeedback) Color(0xFF4CAF50) else Color.Transparent
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (showCorrectFeedback) 
                            Color(0xFFE8F5E8) 
                        else if (showIncorrectFeedback) 
                            Color(0xFFFFEBEE) 
                        else Color.White.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Challenge type indicator
                        Text(
                            text = challenge.type.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Challenge-specific UI
                        when (challenge.type) {
                            ChallengeType.MATH -> {
                                MathChallengeUI(
                                    challenge = challenge,
                                    userAnswer = userAnswer,
                                    onAnswerChange = { userAnswer = it },
                                    onAnswerSubmit = {
                                        isCorrect = userAnswer == challenge.correctAnswer
                                        if (isCorrect) {
                                            canDismiss = true
                                            onChallengeSolved()
                                            showCorrectAnswerFeedback {
                                                onDismiss()
                                            }
                                        } else {
                                            showIncorrectAnswerFeedback()
                                            showError = true
                                            scope.launch {
                                                delay(1000)
                                                showError = false
                                            }
                                        }
                                    },
                                    showError = showError,
                                    timeRemaining = timeRemaining
                                )
                            }
                            ChallengeType.QR_SCAN -> {
                                QRScanChallengeUI(
                                    challenge = challenge,
                                    onScanSuccess = {
                                        canDismiss = true
                                        showNewChallengeButton = false
                                        onChallengeSolved()
                                        showCorrectAnswerFeedback {
                                            onDismiss()
                                        }
                                    },
                                    showError = showError,
                                    timeRemaining = timeRemaining
                                )
                            }
                            ChallengeType.MEMORY_MATCH -> {
                                MemoryMatchChallengeUI(
                                    challenge = challenge,
                                    onMatchComplete = {
                                        canDismiss = true
                                        showNewChallengeButton = false
                                        onChallengeSolved()
                                        showCorrectAnswerFeedback {
                                            onDismiss()
                                        }
                                    },
                                    showError = showError,
                                    timeRemaining = timeRemaining
                                )
                            }
                            ChallengeType.SHAKE -> {
                                ShakeChallengeUI(
                                    challenge = challenge,
                                    onShakeComplete = {
                                        canDismiss = true
                                        showNewChallengeButton = false
                                        onChallengeSolved()
                                        showCorrectAnswerFeedback {
                                            onDismiss()
                                        }
                                    },
                                    showError = showError,
                                    timeRemaining = timeRemaining
                                )
                            }
                            ChallengeType.MEMORY -> {
                                MemoryChallengeUI(
                                    challenge = challenge,
                                    onMemoryComplete = {
                                        canDismiss = true
                                        showNewChallengeButton = false
                                        onChallengeSolved()
                                        showCorrectAnswerFeedback {
                                            onDismiss()
                                        }
                                    },
                                    showError = showError,
                                    timeRemaining = timeRemaining
                                )
                            }
                            ChallengeType.PATTERN -> {
                                PatternChallengeUI(
                                    challenge = challenge,
                                    userAnswer = userAnswer,
                                    onAnswerChange = { userAnswer = it },
                                    onAnswerSubmit = {
                                        isCorrect = userAnswer == challenge.correctAnswer
                                        if (isCorrect) {
                                            canDismiss = true
                                            onChallengeSolved()
                                            showCorrectAnswerFeedback {
                                                onDismiss()
                                            }
                                        } else {
                                            showIncorrectAnswerFeedback()
                                            showError = true
                                            scope.launch {
                                                delay(1000)
                                                showError = false
                                            }
                                        }
                                    },
                                    showError = showError,
                                    timeRemaining = timeRemaining
                                )
                            }
                            ChallengeType.WORD -> {
                                WordChallengeUI(
                                    challenge = challenge,
                                    userAnswer = userAnswer,
                                    onAnswerChange = { userAnswer = it },
                                    onAnswerSubmit = {
                                        isCorrect = userAnswer == challenge.correctAnswer
if (isCorrect) {
    canDismiss = true
    onChallengeSolved()
    showCorrectAnswerFeedback {
        onDismiss()
    }
} else {
                                            showIncorrectAnswerFeedback()
                                            showError = true
                                            scope.launch {
                                                delay(1000)
                                                showError = false
                                            }
                                        }
                                    },
                                    showError = showError,
                                    timeRemaining = timeRemaining
                                )
                            }
                            ChallengeType.LOGIC -> {
                                LogicChallengeUI(
                                    challenge = challenge,
                                    userAnswer = userAnswer,
                                    onAnswerChange = { userAnswer = it },
                                    onAnswerSubmit = {
                                        isCorrect = userAnswer == challenge.correctAnswer
                                        if (isCorrect) {
                                            canDismiss = true
                                            onChallengeSolved()
                                            showCorrectAnswerFeedback {
                                                onDismiss()
                                            }
                                        } else {
                                            showIncorrectAnswerFeedback()
                                            showError = true
                                            scope.launch {
                                                delay(1000)
                                                showError = false
                                            }
                                        }
                                    },
                                    showError = showError,
                                    timeRemaining = timeRemaining
                                )
                            }
                            else -> {
                                // Fallback to original challenge UI
                                Text(
                                    text = challenge.question,
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Time: ${timeRemaining}s",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (timeRemaining <= 10) Color.Red else MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                if (challenge.options.isNotEmpty()) {
                                    challenge.options.forEach { option ->
                                        Button(
                                            onClick = {
                                                userAnswer = option
                                                isCorrect = option == challenge.correctAnswer
                                                if (isCorrect) {
                                                    canDismiss = true
                                                    onChallengeSolved()
                                                    showCorrectAnswerFeedback {
                                                        onDismiss()
                                                    }
                                                } else {
                                                    showIncorrectAnswerFeedback()
                                                    showError = true
                                                    scope.launch {
                                                        delay(1000)
                                                        showError = false
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (showError && userAnswer == option) 
                                                    Color.Red.copy(alpha = 0.2f) 
                                                else MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }

                        if (showError) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Incorrect! Try again or get a new challenge.",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // New Challenge Button
                        if (showNewChallengeButton) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { generateNewChallenge() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "New Challenge",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Try Different Challenge",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        // Manual new challenge hint (always visible)
                        if (!canDismiss) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "💡 Tap the refresh button in the top-left corner for a new challenge",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Instructions
            Text(
                text = if (showNewChallengeButton) 
                    "Time's up! Try a different challenge or solve this one.\nVolume will keep increasing until you succeed."
                else
                    "Solve the challenge to stop the alarm!\nVolume will keep increasing until you succeed.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }

        // Confetti overlay for correct answers
        if (showCorrectFeedback) {
            Box(modifier = Modifier.fillMaxSize()) {
                confettiParticles.forEach { particle ->
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (particle.x).dp,
                                y = (particle.y).dp
                            )
                            .size(8.dp)
                            .graphicsLayer {
                                rotationZ = particle.rotation
                                scaleX = particle.scale
                                scaleY = particle.scale
                            }
                            .background(
                                color = particle.color,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
        
        // Snooze button (small, in corner) - only show when challenge is solved
        if (canDismiss) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = onSnooze,
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ) {
                    Text("Snooze", fontSize = 12.sp)
                }
            }
        }
        
        // New Challenge button (small, in top-left corner) - always visible
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { generateNewChallenge() },
                containerColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Challenge",
                    modifier = Modifier.size(20.dp)
                )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            textToSpeechManager.shutdown()
        }
    }
}
} 