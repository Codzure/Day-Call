package com.codzuregroup.daycall.ui.alarm

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import com.codzuregroup.daycall.vibration.VibrationManager
import com.codzuregroup.daycall.ui.settings.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    onSnooze: () -> Unit = {}
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
            if (vibrationEnabled) {
                vibrationManager.vibrateChallengeTimeout()
            }
            // Generate new challenge after failure
            delay(2000)
            currentChallenge = ChallengeGenerator.getRandomChallenge()
            userAnswer = ""
            showError = false
            timeRemaining = 30
        }
    }

    // Volume increase over time
    LaunchedEffect(Unit) {
        while (!isCorrect) {
            delay(5000) // Increase volume every 5 seconds
            if (!isCorrect) { // Double check before increasing volume
                actualAudioManager.increaseVolume()
                if (vibrationEnabled) {
                    vibrationManager.vibrateVolumeIncrease()
                }
            }
        }
    }
    
    // Urgency vibration that increases over time
    LaunchedEffect(Unit) {
        var urgencyLevel = 0
        while (!isCorrect) {
            delay(10000) // Increase urgency every 10 seconds
            if (!isCorrect) {
                urgencyLevel++
                if (vibrationEnabled) {
                    val intensity = (0.5f + (urgencyLevel * 0.1f)).coerceAtMost(1.0f)
                    vibrationManager.vibrateAlarmUrgency(intensity)
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
    LaunchedEffect(isCorrect) {
        if (isCorrect) {
            actualAudioManager.stopAudio()
        }
    }
    
    // Feedback functions
    fun showCorrectAnswerFeedback() {
        showCorrectFeedback = true
        confettiParticles = createConfettiParticles()
        
        if (vibrationEnabled) {
            vibrationManager.vibrateCorrectAnswer()
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        
        scope.launch {
            delay(2000)
            showCorrectFeedback = false
            confettiParticles = emptyList()
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

    // Cleanup when leaving
    DisposableEffect(Unit) {
        onDispose {
            actualAudioManager.stopAudio()
            vibrationManager.stopVibration()
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
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Volume: ${(volume * 100).toInt()}%",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(volume)
                                .background(
                                    color = Color.Red,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
                
                if (!isCorrect) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Volume will increase until you solve the challenge!",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
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
                                            showCorrectAnswerFeedback()
                                            scope.launch {
                                                delay(500)
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
                                        showCorrectAnswerFeedback()
                                        scope.launch {
                                            delay(500)
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
                                        showCorrectAnswerFeedback()
                                        scope.launch {
                                            delay(500)
                                            onDismiss()
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
                                                    showCorrectAnswerFeedback()
                                                    scope.launch {
                                                        delay(500)
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Instructions
            Text(
                text = "Solve the challenge to stop the alarm!\nVolume will keep increasing until you succeed.",
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
        if (isCorrect) {
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
    }
} 