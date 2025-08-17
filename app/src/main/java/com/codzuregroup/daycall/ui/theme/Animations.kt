package com.codzuregroup.daycall.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Comprehensive animation system for smooth transitions across the app
 */
object DayCallAnimations {
    
    // Standard durations
    const val FAST_DURATION = 200
    const val STANDARD_DURATION = 300
    const val SLOW_DURATION = 500
    const val EXTRA_SLOW_DURATION = 800
    
    // Standard easing curves
    val FastOutSlowIn = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val LinearOutSlowIn = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val FastOutLinearIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val StandardEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    
    /**
     * Standard fade in/out animation
     */
    fun fadeAnimation(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): EnterTransition {
        return fadeIn(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun fadeOutAnimation(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): ExitTransition {
        return fadeOut(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    /**
     * Slide animations for navigation
     */
    fun slideInFromRight(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(duration, easing = easing)
        ) + fadeIn(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun slideOutToLeft(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(duration, easing = easing)
        ) + fadeOut(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun slideInFromLeft(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(duration, easing = easing)
        ) + fadeIn(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun slideOutToRight(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(duration, easing = easing)
        ) + fadeOut(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    /**
     * Vertical slide animations
     */
    fun slideInFromBottom(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): EnterTransition {
        return slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(duration, easing = easing)
        ) + fadeIn(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun slideOutToBottom(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(duration, easing = easing)
        ) + fadeOut(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun slideInFromTop(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): EnterTransition {
        return slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(duration, easing = easing)
        ) + fadeIn(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun slideOutToTop(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(duration, easing = easing)
        ) + fadeOut(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    /**
     * Scale animations
     */
    fun scaleInAnimation(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing,
        initialScale: Float = 0.8f
    ): EnterTransition {
        return scaleIn(
            initialScale = initialScale,
            animationSpec = tween(duration, easing = easing)
        ) + fadeIn(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun scaleOutAnimation(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing,
        targetScale: Float = 0.8f
    ): ExitTransition {
        return scaleOut(
            targetScale = targetScale,
            animationSpec = tween(duration, easing = easing)
        ) + fadeOut(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    /**
     * Expand/Collapse animations
     */
    fun expandVertically(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): EnterTransition {
        return expandVertically(
            animationSpec = tween(duration, easing = easing)
        ) + fadeIn(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    fun shrinkVertically(
        duration: Int = STANDARD_DURATION,
        easing: Easing = StandardEasing
    ): ExitTransition {
        return shrinkVertically(
            animationSpec = tween(duration, easing = easing)
        ) + fadeOut(
            animationSpec = tween(duration, easing = easing)
        )
    }
    
    /**
     * Staggered list animations
     */
    fun staggeredListItemAnimation(
        index: Int,
        duration: Int = STANDARD_DURATION,
        delayBetweenItems: Int = 50
    ): EnterTransition {
        val delay = index * delayBetweenItems
        return slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = StandardEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = StandardEasing
            )
        )
    }
    
    /**
     * Floating Action Button animations
     */
    fun fabScaleAnimation(): EnterTransition {
        return scaleIn(
            initialScale = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    fun fabScaleOutAnimation(): ExitTransition {
        return scaleOut(
            targetScale = 0f,
            animationSpec = tween(FAST_DURATION, easing = FastOutLinearIn)
        )
    }
    
    /**
     * Card hover/press animations
     */
    @Composable
    fun animatedCardScale(
        targetScale: Float = 0.95f,
        duration: Int = FAST_DURATION
    ): State<Float> {
        return animateFloatAsState(
            targetValue = targetScale,
            animationSpec = tween(duration, easing = FastOutSlowIn),
            label = "card_scale"
        )
    }
    
    /**
     * Shimmer loading animation
     */
    @Composable
    fun shimmerAnimation(): State<Float> {
        val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_alpha"
        )
    }
    
    /**
     * Pulse animation for notifications/alerts
     */
    @Composable
    fun pulseAnimation(
        minScale: Float = 1f,
        maxScale: Float = 1.1f,
        duration: Int = 1000
    ): State<Float> {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        return infiniteTransition.animateFloat(
            initialValue = minScale,
            targetValue = maxScale,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    }
    
    /**
     * Rotation animation
     */
    @Composable
    fun rotationAnimation(
        duration: Int = 2000
    ): State<Float> {
        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation_degrees"
        )
    }
    
    /**
     * Bounce animation for success states
     */
    @Composable
    fun bounceAnimation(
        targetScale: Float = 1.2f,
        duration: Int = 600
    ): State<Float> {
        return animateFloatAsState(
            targetValue = targetScale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "bounce_scale"
        )
    }
    
    /**
     * Shake animation for errors
     */
    @Composable
    fun shakeAnimation(
        trigger: Boolean,
        strength: Float = 10f,
        duration: Int = 400
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (trigger) strength else 0f,
            animationSpec = if (trigger) {
                keyframes {
                    durationMillis = duration
                    0f at 0
                    strength at duration / 8
                    -strength at duration / 4
                    strength at duration * 3 / 8
                    -strength at duration / 2
                    strength at duration * 5 / 8
                    -strength at duration * 3 / 4
                    strength at duration * 7 / 8
                    0f at duration
                }
            } else {
                tween(0)
            },
            label = "shake_offset"
        )
    }
}

/**
 * Modifier extensions for common animations
 */
fun Modifier.animatedScale(
    scale: Float,
    duration: Int = DayCallAnimations.STANDARD_DURATION
): Modifier = this.graphicsLayer {
    scaleX = scale
    scaleY = scale
}

fun Modifier.animatedRotation(
    rotation: Float
): Modifier = this.graphicsLayer {
    rotationZ = rotation
}

fun Modifier.animatedTranslation(
    translationX: Float = 0f,
    translationY: Float = 0f
): Modifier = this.graphicsLayer {
    this.translationX = translationX
    this.translationY = translationY
}