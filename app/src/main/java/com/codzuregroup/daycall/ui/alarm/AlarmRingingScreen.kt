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
import com.codzuregroup.daycall.audio.AudioManager
import com.codzuregroup.daycall.ui.challenges.Challenge
import com.codzuregroup.daycall.ui.challenges.ChallengeGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // Start playing audio when screen loads
    LaunchedEffect(Unit) {
        actualAudioManager.playAudio(audioFile)
        currentChallenge = ChallengeGenerator.getRandomChallenge()
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

    // Cleanup when leaving
    DisposableEffect(Unit) {
        onDispose {
            actualAudioManager.stopAudio()
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
                        .scale(scale),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
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

                        // Question
                        Text(
                            text = challenge.question,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Timer
                        Text(
                            text = "Time: ${timeRemaining}s",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (timeRemaining <= 10) Color.Red else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Answer input
                        if (challenge.options.isNotEmpty()) {
                            // Multiple choice
                            challenge.options.forEach { option ->
                                Button(
                                    onClick = {
                                        userAnswer = option
                                        isCorrect = option == challenge.correctAnswer
                                        if (isCorrect) {
                                            onDismiss()
                                        } else {
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
                        } else {
                            // Text input
                            OutlinedTextField(
                                value = userAnswer,
                                onValueChange = { userAnswer = it },
                                label = { Text("Your answer") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        isCorrect = userAnswer.equals(challenge.correctAnswer, ignoreCase = true)
                                        if (isCorrect) {
                                            onDismiss()
                                        } else {
                                            showError = true
                                            scope.launch {
                                                delay(1000)
                                                showError = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Submit")
                                }

                                Button(
                                    onClick = {
                                        currentChallenge = ChallengeGenerator.getRandomChallenge()
                                        userAnswer = ""
                                        showError = false
                                        timeRemaining = 30
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("New Challenge")
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