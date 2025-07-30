package com.codzuregroup.daycall.ui.social

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

data class WakeCircle(
    val id: String,
    val name: String,
    val description: String,
    val alarmTime: String,
    val members: List<SocialUser>,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val isActive: Boolean = true,
    val maxMembers: Int = 10
)

data class SocialUser(
    val id: String,
    val name: String,
    val avatar: String = "👤",
    val isOnline: Boolean = false,
    val lastSeen: LocalDateTime? = null,
    val wakeStreak: Int = 0,
    val totalWakes: Int = 0
)

data class MessageDrop(
    val id: String,
    val senderId: String,
    val senderName: String,
    val circleId: String,
    val message: String,
    val type: MessageType,
    val createdAt: LocalDateTime,
    val reactions: List<Reaction> = emptyList()
)

enum class MessageType {
    TEXT,
    AUDIO,
    VIDEO,
    EMOJI
}

data class Reaction(
    val userId: String,
    val userName: String,
    val emoji: String
)

data class SocialUiState(
    val circles: List<WakeCircle> = emptyList(),
    val currentUser: SocialUser? = null,
    val selectedCircle: WakeCircle? = null,
    val messages: List<MessageDrop> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SocialEvent {
    data class CreateCircle(val name: String, val description: String, val alarmTime: String) : SocialEvent()
    data class JoinCircle(val circleId: String) : SocialEvent()
    data class LeaveCircle(val circleId: String) : SocialEvent()
    data class SendMessage(val message: String, val type: MessageType = MessageType.TEXT) : SocialEvent()
    data class ReactToMessage(val messageId: String, val emoji: String) : SocialEvent()
    data class SelectCircle(val circle: WakeCircle?) : SocialEvent()
    object RefreshCircles : SocialEvent()
    object LoadUserProfile : SocialEvent()
} 