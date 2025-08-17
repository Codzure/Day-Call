# Design Document

## Overview

This design transforms the existing DayCall todo system into a more engaging, intuitive, and visually appealing experience that resonates with Gen Z users. The redesign maintains the robust Room database foundation while introducing modern UI patterns, gamification elements, and seamless integration with the DayCall ecosystem.

## Architecture

The redesigned todo system will build upon the existing architecture with these enhancements:

### Current Architecture (Maintained)
- **Data Layer**: TodoEntity, TodoDao, TodoRepository (Room database)
- **UI Layer**: Jetpack Compose screens with Material 3 design
- **Business Logic**: TodoViewModel with reactive state management

### New Architecture Components
- **Gamification Engine**: Points, achievements, and streak tracking
- **Smart Suggestions**: AI-powered task categorization and scheduling
- **Animation System**: Coordinated animations and micro-interactions
- **Integration Layer**: Connections to alarm system and daily vibes

## Components and Interfaces

### Enhanced UI Components

#### 1. Hero Dashboard
```kotlin
@Composable
fun TodoHeroDashboard(
    stats: TodoStats,
    streakData: StreakData,
    todayProgress: Float
) {
    // Animated progress ring showing daily completion
    // Streak counter with fire emoji animations
    // Quick stats with smooth number animations
    // Motivational message based on time of day
}
```

#### 2. Smart Quick Add
```kotlin
@Composable
fun SmartQuickAddModal(
    onAddTask: (TodoItem) -> Unit,
    suggestions: List<TaskSuggestion>
) {
    // Bottom sheet with smart defaults
    // Voice input capability
    // Template suggestions based on history
    // One-tap category and priority selection
}
```

#### 3. Enhanced Task Cards
```kotlin
@Composable
fun ModernTaskCard(
    task: TodoItem,
    onComplete: () -> Unit,
    onSwipeAction: (SwipeAction) -> Unit
) {
    // Swipe-to-action gestures (complete, edit, delete, reschedule)
    // Priority-based color coding with gradients
    // Smooth check animation with confetti effect
    // Contextual action buttons that appear on hover/long press
}
```

#### 4. Gamification Elements
```kotlin
@Composable
fun AchievementBadge(
    achievement: Achievement,
    isUnlocked: Boolean,
    animateUnlock: Boolean
) {
    // Animated badge reveals
    // Progress indicators for achievements in progress
    // Celebration animations when unlocked
}

@Composable
fun StreakTracker(
    currentStreak: Int,
    bestStreak: Int,
    streakType: StreakType
) {
    // Fire emoji animations for active streaks
    // Progress visualization
    // Motivational messages
}
```

### Data Models Enhancement

#### Gamification Models
```kotlin
data class UserStats(
    val totalPoints: Int,
    val level: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val achievementsUnlocked: List<Achievement>
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val pointsRequired: Int,
    val isUnlocked: Boolean
)

enum class StreakType {
    DAILY_COMPLETION,
    EARLY_BIRD,
    NIGHT_OWL,
    CATEGORY_FOCUS
}
```

#### Enhanced Task Models
```kotlin
data class TaskSuggestion(
    val title: String,
    val category: TodoCategory,
    val priority: TodoPriority,
    val estimatedDuration: Int,
    val confidence: Float
)

data class SwipeAction(
    val type: SwipeActionType,
    val icon: ImageVector,
    val color: Color,
    val action: () -> Unit
)

enum class SwipeActionType {
    COMPLETE, EDIT, DELETE, RESCHEDULE, DUPLICATE
}
```

### Animation System

#### Core Animation Components
```kotlin
object TodoAnimations {
    val taskCompleteAnimation = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val cardEnterAnimation = slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = tween(300, easing = EaseOutCubic)
    )
    
    val confettiAnimation = keyframes<Float> {
        durationMillis = 1000
        0f at 0
        1f at 300
        0f at 1000
    }
}
```

## User Experience Flow

### 1. Dashboard Experience
- **Hero Section**: Large progress ring showing daily completion percentage
- **Quick Stats**: Animated counters for active, completed, and overdue tasks
- **Streak Display**: Current streak with fire animations
- **Quick Actions**: One-tap access to common actions (Add Task, View Today, etc.)

### 2. Task Management Flow
- **Smart Grouping**: Tasks automatically grouped by "Today", "Tomorrow", "This Week", "Later"
- **Visual Hierarchy**: Different card heights and colors based on priority and urgency
- **Contextual Actions**: Swipe gestures reveal contextual actions
- **Bulk Operations**: Multi-select mode for batch operations

### 3. Task Creation Flow
- **Quick Add Modal**: Bottom sheet with smart defaults
- **Voice Input**: Tap-to-speak functionality for quick task entry
- **Smart Suggestions**: AI-powered category and priority suggestions
- **Template System**: Quick access to frequently used task templates

### 4. Gamification Integration
- **Point System**: Points awarded for task completion, streaks, and achievements
- **Level Progression**: User levels with unlockable themes and features
- **Achievement System**: Badges for various accomplishments
- **Social Elements**: Shareable achievement cards

## Visual Design System

### Color Palette
```kotlin
object TodoColors {
    val primaryGradient = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6)  // Purple
    )
    
    val priorityColors = mapOf(
        TodoPriority.HIGH to Color(0xFFEF4444),    // Red
        TodoPriority.MEDIUM to Color(0xFFF59E0B),  // Amber
        TodoPriority.LOW to Color(0xFF10B981)      // Emerald
    )
    
    val categoryGradients = mapOf(
        TodoCategory.WORK to listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
        TodoCategory.PERSONAL to listOf(Color(0xFF10B981), Color(0xFF059669)),
        TodoCategory.HEALTH to listOf(Color(0xFFEC4899), Color(0xFFBE185D))
    )
}
```

### Typography
```kotlin
object TodoTypography {
    val heroTitle = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    )
    
    val taskTitle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp
    )
    
    val statsNumber = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFeatureSettings = "tnum"
    )
}
```

### Spacing and Layout
```kotlin
object TodoSpacing {
    val cardPadding = 16.dp
    val sectionSpacing = 24.dp
    val itemSpacing = 12.dp
    val cornerRadius = 16.dp
    val heroHeight = 200.dp
}
```

## Integration Points

### 1. Alarm System Integration
- Morning tasks can be attached to alarms
- Completed morning tasks influence wake-up streak
- Task reminders integrate with alarm notifications

### 2. Daily Vibes Integration
- Task completion affects daily mood tracking
- Vibe cards can include task completion status
- Achievement unlocks can influence daily vibe selection

### 3. Social Features Integration
- Achievement sharing through social wake-up circles
- Group challenges based on task completion
- Leaderboards for task completion streaks

## Performance Considerations

### 1. Animation Performance
- Use `remember` for animation states to prevent recomposition
- Implement lazy loading for large task lists
- Optimize confetti animations with particle pooling

### 2. Database Optimization
- Implement pagination for large task lists
- Use database triggers for automatic streak calculations
- Cache frequently accessed data in memory

### 3. Memory Management
- Dispose of animation resources properly
- Use weak references for callback listeners
- Implement proper lifecycle management for ViewModels

## Accessibility Features

### 1. Screen Reader Support
- Comprehensive content descriptions for all interactive elements
- Semantic markup for task status and priority
- Audio feedback for task completion

### 2. Motor Accessibility
- Large touch targets (minimum 48dp)
- Alternative input methods for swipe gestures
- Voice commands for common actions

### 3. Visual Accessibility
- High contrast mode support
- Customizable text sizes
- Color-blind friendly priority indicators

## Testing Strategy

### 1. Unit Testing
- ViewModel logic for gamification calculations
- Animation state management
- Smart suggestion algorithms
- Database operations with Room testing utilities

### 2. UI Testing
- Task creation and completion flows
- Swipe gesture interactions
- Animation completion verification
- Accessibility compliance testing

### 3. Integration Testing
- Alarm system integration
- Daily vibes data synchronization
- Achievement unlock triggers
- Cross-feature data consistency

### 4. Performance Testing
- Animation frame rate monitoring
- Memory usage during heavy task loads
- Database query performance
- Battery usage optimization