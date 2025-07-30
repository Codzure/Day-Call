package com.codzuregroup.daycall.ui.alarm

import com.codzuregroup.daycall.ui.challenges.ChallengeType
import java.time.DayOfWeek

enum class AlarmSound(val displayName: String) {
    GENTLE("Gentle"),
    NATURE("Nature"),
    CLASSICAL("Classical"),
    ELECTRONIC("Electronic"),
    CUSTOM("Custom")
}

data class Alarm(
    val id: Int,
    val time: AlarmTime,
    val isEnabled: Boolean = true,
    val label: String = "Alarm",
    val selectedDays: Set<DayOfWeek> = setOf(),
    val sound: String = "Default",
    val volume: Float = 0.8f,
    val snoozeEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val challengeType: ChallengeType = ChallengeType.MATH, // Default to math challenge
    val audioFile: String? = null
)

data class AlarmTime(
    val hour: Int,
    val minute: Int
) {
    override fun toString(): String {
        val ampm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return String.format("%d:%02d %s", displayHour, minute, ampm)
    }
}

fun formatTime(hour: Int, minute: Int): String {
    val ampm = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return String.format("%d:%02d %s", displayHour, minute, ampm)
}
