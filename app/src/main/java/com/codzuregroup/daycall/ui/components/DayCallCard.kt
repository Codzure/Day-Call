package com.codzuregroup.daycall.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.codzuregroup.daycall.ui.theme.DayCallAnimations

@Composable
fun DayCallCard(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    gradient: Brush? = null,
    border: BorderStroke? = null,
    elevation: Int = 8,
    onClick: (() -> Unit)? = null,
    animatePress: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Smooth press animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed && animatePress) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_press_scale"
    )
    
    val elevationAnimation by animateFloatAsState(
        targetValue = if (isPressed && animatePress) elevation * 0.7f else elevation.toFloat(),
        animationSpec = tween(
            durationMillis = DayCallAnimations.FAST_DURATION,
            easing = DayCallAnimations.FastOutSlowIn
        ),
        label = "card_elevation"
    )
    
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            if (animatePress) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick()
        }
    } else Modifier
    
    Card(
        modifier = modifier
            .then(clickableModifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevationAnimation.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = border
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = gradient ?: Brush.verticalGradient(
                        colors = listOf(
                            background,
                            background.copy(alpha = 0.95f)
                        )
                    ),
                    shape = shape
                )
                .clip(shape)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    onClick: (() -> Unit)? = null,
    animatePress: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    DayCallCard(
        modifier = modifier,
        gradient = gradient,
        onClick = onClick,
        animatePress = animatePress,
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    
    Card(
        modifier = modifier
            .then(clickableModifier)
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color.White.copy(alpha = 0.1f),
                spotColor = Color.White.copy(alpha = 0.2f)
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun FloatingCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    
    Card(
        modifier = modifier
            .then(clickableModifier)
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            .zIndex(1f),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
} 