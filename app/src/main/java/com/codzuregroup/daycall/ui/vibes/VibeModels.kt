package com.codzuregroup.daycall.ui.vibes

import androidx.compose.ui.graphics.Color
import com.codzuregroup.daycall.ui.theme.*

data class Vibe(
    val id: String,
    val name: String,
    val description: String,
    val color: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val icon: String,
    val isUnlocked: Boolean = true,
    val isSelected: Boolean = false
)

data class VibeCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val vibe: Vibe,
    val isActive: Boolean = false,
    val lastUsed: String? = null
)

data class VibesUiState(
    val vibes: List<Vibe> = emptyList(),
    val selectedVibe: Vibe? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class VibesEvent {
    data class SelectVibe(val vibe: Vibe) : VibesEvent()
    data class UnlockVibe(val vibeId: String) : VibesEvent()
    object RefreshVibes : VibesEvent()
}

object VibeDefaults {
    val availableVibes = listOf(
        Vibe(
            id = "chill",
            name = "Chill",
            description = "Peaceful mornings with gentle tones",
            color = VibeChill,
            gradientStart = Color(0xFF60A5FA),
            gradientEnd = Color(0xFF3B82F6),
            icon = "🌊"
        ),
        Vibe(
            id = "hustle",
            name = "Hustle",
            description = "Energetic beats to get you moving",
            color = VibeHustle,
            gradientStart = Color(0xFFF59E0B),
            gradientEnd = Color(0xFFD97706),
            icon = "⚡"
        ),
        Vibe(
            id = "cosmic",
            name = "Cosmic",
            description = "Out-of-this-world ambient sounds",
            color = VibeCosmic,
            gradientStart = Color(0xFF8B5CF6),
            gradientEnd = Color(0xFF7C3AED),
            icon = "✨"
        ),
        Vibe(
            id = "nature",
            name = "Nature",
            description = "Organic sounds from the great outdoors",
            color = VibeNature,
            gradientStart = Color(0xFF10B981),
            gradientEnd = Color(0xFF059669),
            icon = "🌿"
        ),
        Vibe(
            id = "chaos",
            name = "Chaos",
            description = "Intense wake-up calls for heavy sleepers",
            color = VibeChaos,
            gradientStart = Color(0xFFEF4444),
            gradientEnd = Color(0xFFDC2626),
            icon = "��"
        )
    )
} 