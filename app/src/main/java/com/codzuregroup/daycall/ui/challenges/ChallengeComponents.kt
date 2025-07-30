package com.codzuregroup.daycall.ui.challenges

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MathChallengeUI(
    challenge: Challenge,
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    onAnswerSubmit: () -> Unit,
    showError: Boolean,
    timeRemaining: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer
        Text(
            text = "Time: ${timeRemaining}s",
            style = MaterialTheme.typography.bodyLarge,
            color = if (timeRemaining <= 10) Color.Red else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Math problems
        Text(
            text = challenge.question,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Multiple choice options
        challenge.options.forEach { option ->
            Button(
                onClick = {
                    onAnswerChange(option)
                    onAnswerSubmit()
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

@Composable
fun QRScanChallengeUI(
    challenge: Challenge,
    onScanSuccess: () -> Unit,
    showError: Boolean,
    timeRemaining: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer
        Text(
            text = "Time: ${timeRemaining}s",
            style = MaterialTheme.typography.bodyLarge,
            color = if (timeRemaining <= 10) Color.Red else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Instructions
        Text(
            text = challenge.question,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Place the QR code in another room to force yourself out of bed!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // QR Scanner Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { /* TODO: Launch QR scanner */ },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "QR Scanner",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Tap to Scan QR Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Expected: ${challenge.correctAnswer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (showError) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Wrong QR code! Try again.",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MemoryMatchChallengeUI(
    challenge: Challenge,
    onMatchComplete: () -> Unit,
    showError: Boolean,
    timeRemaining: Int
) {
    var selectedCards by remember { mutableStateOf(listOf<Int>()) }
    var matchedPairs by remember { mutableStateOf(setOf<String>()) }
    var flippedCards by remember { mutableStateOf(setOf<Int>()) }
    
    val symbols = challenge.question.split(": ")[1].split(" ")
    val symbolPairs = symbols.chunked(2).mapIndexed { index, pair ->
        MemoryCard(index, pair[0], pair[1])
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer
        Text(
            text = "Time: ${timeRemaining}s",
            style = MaterialTheme.typography.bodyLarge,
            color = if (timeRemaining <= 10) Color.Red else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Instructions
        Text(
            text = "Match the pairs to dismiss the alarm",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Memory grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(300.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(symbolPairs.size) { index ->
                val card = symbolPairs[index]
                val isFlipped = flippedCards.contains(index)
                val isMatched = matchedPairs.contains(card.symbol1) && matchedPairs.contains(card.symbol2)
                
                MemoryCardUI(
                    card = card,
                    isFlipped = isFlipped,
                    isMatched = isMatched,
                    onClick = {
                        if (!isFlipped && !isMatched) {
                            flippedCards = flippedCards + index
                            selectedCards = selectedCards + index
                            
                            if (selectedCards.size == 2) {
                                // Check for match
                                val firstCard = symbolPairs[selectedCards[0]]
                                val secondCard = symbolPairs[selectedCards[1]]
                                
                                if (firstCard.symbol1 == secondCard.symbol1 || 
                                    firstCard.symbol1 == secondCard.symbol2) {
                                    // Match found
                                    matchedPairs = matchedPairs + firstCard.symbol1
                                    if (matchedPairs.size == 4) {
                                        onMatchComplete()
                                    }
                                } else {
                                    // No match, flip back after delay
                                    // TODO: Add delay and flip back animation
                                }
                                selectedCards = emptyList()
                            }
                        }
                    }
                )
            }
        }
        
        if (showError) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Wrong match! Try again.",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MemoryCardUI(
    card: MemoryCard,
    isFlipped: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isMatched -> Color.Green.copy(alpha = 0.3f)
                isFlipped -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isFlipped || isMatched) {
                Text(
                    text = if (isFlipped) card.symbol1 else card.symbol2,
                    fontSize = 24.sp
                )
            } else {
                Text(
                    text = "?",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class MemoryCard(
    val index: Int,
    val symbol1: String,
    val symbol2: String
) 