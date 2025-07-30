package com.codzuregroup.daycall.ui.social

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class SocialViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()
    
    init {
        loadMockData()
    }
    
    fun handleEvent(event: SocialEvent) {
        when (event) {
            is SocialEvent.CreateCircle -> createCircle(event.name, event.description, event.alarmTime)
            is SocialEvent.JoinCircle -> joinCircle(event.circleId)
            is SocialEvent.LeaveCircle -> leaveCircle(event.circleId)
            is SocialEvent.SendMessage -> sendMessage(event.message, event.type)
            is SocialEvent.ReactToMessage -> reactToMessage(event.messageId, event.emoji)
            is SocialEvent.SelectCircle -> selectCircle(event.circle)
            is SocialEvent.RefreshCircles -> refreshCircles()
            is SocialEvent.LoadUserProfile -> loadUserProfile()
        }
    }
    
    private fun createCircle(name: String, description: String, alarmTime: String) {
        val currentUser = _uiState.value.currentUser ?: return
        
        val newCircle = WakeCircle(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            alarmTime = alarmTime,
            members = listOf(currentUser),
            createdBy = currentUser.id,
            createdAt = LocalDateTime.now()
        )
        
        _uiState.update { currentState ->
            currentState.copy(
                circles = currentState.circles + newCircle,
                selectedCircle = newCircle
            )
        }
    }
    
    private fun joinCircle(circleId: String) {
        val currentUser = _uiState.value.currentUser ?: return
        val circle = _uiState.value.circles.find { it.id == circleId } ?: return
        
        if (circle.members.size >= circle.maxMembers) return
        
        val updatedCircle = circle.copy(
            members = circle.members + currentUser
        )
        
        _uiState.update { currentState ->
            currentState.copy(
                circles = currentState.circles.map { 
                    if (it.id == circleId) updatedCircle else it 
                },
                selectedCircle = updatedCircle
            )
        }
    }
    
    private fun leaveCircle(circleId: String) {
        val currentUser = _uiState.value.currentUser ?: return
        
        _uiState.update { currentState ->
            currentState.copy(
                circles = currentState.circles.map { circle ->
                    if (circle.id == circleId) {
                        circle.copy(members = circle.members.filter { it.id != currentUser.id })
                    } else circle
                },
                selectedCircle = if (currentState.selectedCircle?.id == circleId) null else currentState.selectedCircle
            )
        }
    }
    
    private fun sendMessage(message: String, type: MessageType) {
        val currentUser = _uiState.value.currentUser ?: return
        val selectedCircle = _uiState.value.selectedCircle ?: return
        
        val newMessage = MessageDrop(
            id = UUID.randomUUID().toString(),
            senderId = currentUser.id,
            senderName = currentUser.name,
            circleId = selectedCircle.id,
            message = message,
            type = type,
            createdAt = LocalDateTime.now()
        )
        
        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + newMessage
            )
        }
    }
    
    private fun reactToMessage(messageId: String, emoji: String) {
        val currentUser = _uiState.value.currentUser ?: return
        
        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages.map { message ->
                    if (message.id == messageId) {
                        val existingReaction = message.reactions.find { it.userId == currentUser.id }
                        val updatedReactions = if (existingReaction != null) {
                            message.reactions.map { 
                                if (it.userId == currentUser.id) it.copy(emoji = emoji) else it 
                            }
                        } else {
                            message.reactions + Reaction(
                                userId = currentUser.id,
                                userName = currentUser.name,
                                emoji = emoji
                            )
                        }
                        message.copy(reactions = updatedReactions)
                    } else message
                }
            )
        }
    }
    
    private fun selectCircle(circle: WakeCircle?) {
        _uiState.update { currentState ->
            currentState.copy(selectedCircle = circle)
        }
    }
    
    private fun refreshCircles() {
        // In a real app, this would fetch from API
        loadMockData()
    }
    
    private fun loadUserProfile() {
        val mockUser = SocialUser(
            id = "user_1",
            name = "Alex",
            avatar = "👨‍💻",
            isOnline = true,
            wakeStreak = 7,
            totalWakes = 42
        )
        
        _uiState.update { currentState ->
            currentState.copy(currentUser = mockUser)
        }
    }
    
    private fun loadMockData() {
        val mockUser = SocialUser(
            id = "user_1",
            name = "Alex",
            avatar = "👨‍💻",
            isOnline = true,
            wakeStreak = 7,
            totalWakes = 42
        )
        
        val mockCircles = listOf(
            WakeCircle(
                id = "circle_1",
                name = "7AM Club",
                description = "Early birds catch the worm! Rise and shine with us.",
                alarmTime = "07:00",
                members = listOf(
                    mockUser,
                    SocialUser("user_2", "Sarah", "👩‍🎨", true, wakeStreak = 5, totalWakes = 28),
                    SocialUser("user_3", "Mike", "👨‍💼", false, wakeStreak = 12, totalWakes = 89)
                ),
                createdBy = "user_1",
                createdAt = LocalDateTime.now().minusDays(5)
            ),
            WakeCircle(
                id = "circle_2",
                name = "Study Squad",
                description = "Morning study sessions for the ambitious.",
                alarmTime = "06:30",
                members = listOf(
                    mockUser,
                    SocialUser("user_4", "Emma", "👩‍🎓", true, wakeStreak = 3, totalWakes = 15),
                    SocialUser("user_5", "David", "👨‍🔬", true, wakeStreak = 8, totalWakes = 56)
                ),
                createdBy = "user_4",
                createdAt = LocalDateTime.now().minusDays(2)
            )
        )
        
        val mockMessages = listOf(
            MessageDrop(
                id = "msg_1",
                senderId = "user_2",
                senderName = "Sarah",
                circleId = "circle_1",
                message = "Good morning everyone! ☀️ Ready for another productive day!",
                type = MessageType.TEXT,
                createdAt = LocalDateTime.now().minusHours(2),
                reactions = listOf(
                    Reaction("user_1", "Alex", "👍"),
                    Reaction("user_3", "Mike", "❤️")
                )
            ),
            MessageDrop(
                id = "msg_2",
                senderId = "user_1",
                senderName = "Alex",
                circleId = "circle_1",
                message = "Let's crush today! 💪",
                type = MessageType.TEXT,
                createdAt = LocalDateTime.now().minusHours(1),
                reactions = listOf(
                    Reaction("user_2", "Sarah", "🔥"),
                    Reaction("user_3", "Mike", "💪")
                )
            ),
            MessageDrop(
                id = "msg_3",
                senderId = "user_3",
                senderName = "Mike",
                circleId = "circle_1",
                message = "🔥",
                type = MessageType.EMOJI,
                createdAt = LocalDateTime.now().minusMinutes(30),
                reactions = listOf(
                    Reaction("user_1", "Alex", "💪"),
                    Reaction("user_2", "Sarah", "🔥")
                )
            ),
            MessageDrop(
                id = "msg_4",
                senderId = "user_4",
                senderName = "Emma",
                circleId = "circle_2",
                message = "Study session starting in 10 minutes! 📚",
                type = MessageType.TEXT,
                createdAt = LocalDateTime.now().minusMinutes(15),
                reactions = listOf(
                    Reaction("user_1", "Alex", "📚"),
                    Reaction("user_5", "David", "💡")
                )
            ),
            MessageDrop(
                id = "msg_5",
                senderId = "user_5",
                senderName = "David",
                circleId = "circle_2",
                message = "Perfect timing! Let's get this done 💪",
                type = MessageType.TEXT,
                createdAt = LocalDateTime.now().minusMinutes(10),
                reactions = listOf(
                    Reaction("user_4", "Emma", "🚀"),
                    Reaction("user_1", "Alex", "💯")
                )
            )
        )
        
        _uiState.update { currentState ->
            currentState.copy(
                circles = mockCircles,
                currentUser = mockUser,
                selectedCircle = mockCircles.firstOrNull(),
                messages = mockMessages
            )
        }
    }
} 