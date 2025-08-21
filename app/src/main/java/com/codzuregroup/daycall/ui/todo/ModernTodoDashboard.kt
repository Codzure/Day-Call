package com.codzuregroup.daycall.ui.todo

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import com.codzuregroup.daycall.ui.components.DayCallCard


@Composable
fun ModernTodoDashboard(
    stats: TodoStats,
    streakData: StreakData,
    todayProgress: Float,
    modifier: Modifier = Modifier
) {
    DayCallCard(
        modifier = modifier.fillMaxWidth(),
        background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        elevation = 8
    ) {
            // Header with greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = getGreeting(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Let's make today productive!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Streak fire emoji with animation
                if (streakData.currentStreak > 0) {
                    StreakIndicator(streak = streakData.currentStreak)
                }
            }
            
            // Progress Ring and Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated Progress Ring
                AnimatedProgressRing(
                    progress = todayProgress,
                    completedTasks = stats.completed,
                    totalTasks = stats.total
                )
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Quick Stats
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickStatItem(
                        icon = Icons.Outlined.Assignment,
                        label = "Active",
                        value = stats.pending.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    QuickStatItem(
                        icon = Icons.Outlined.CheckCircle,
                        label = "Done",
                        value = stats.completed.toString(),
                        color = Color(0xFF4CAF50)
                    )
                    
                    if (stats.overdue > 0) {
                        QuickStatItem(
                            icon = Icons.Outlined.Schedule,
                            label = "Overdue",
                            value = stats.overdue.toString(),
                            color = Color(0xFFFF5722)
                        )
                    }
                }
            }
            
            // Motivational message based on progress
            MotivationalMessage(progress = todayProgress, stats = stats)
        }
}

@Composable
fun AnimatedProgressRing(
    progress: Float,
    completedTasks: Int,
    totalTasks: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "progress_animation"
    )
    
    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = center
            
            // Background circle
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
        
        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = completedTasks.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "of $totalTasks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StreakIndicator(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_scale"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔥",
            fontSize = (24 * scale).sp,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
        )
        Text(
            text = "$streak day${if (streak != 1) "s" else ""}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MotivationalMessage(
    progress: Float,
    stats: TodoStats,
    modifier: Modifier = Modifier
) {
    val message = when {
        progress >= 1f -> "🎉 Amazing! You've completed all your tasks today!"
        progress >= 0.8f -> "🚀 You're almost there! Just a few more tasks to go!"
        progress >= 0.5f -> "💪 Great progress! Keep up the momentum!"
        progress >= 0.2f -> "🌟 Good start! You're on the right track!"
        stats.total == 0 -> "📝 Ready to add your first task of the day?"
        else -> "☀️ A new day, a fresh start! Let's tackle those tasks!"
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun getGreeting(): String {
    return com.codzuregroup.daycall.utils.TimeBasedMessaging.getGreeting()
}

