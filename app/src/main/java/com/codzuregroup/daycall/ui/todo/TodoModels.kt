package com.codzuregroup.daycall.ui.todo

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

data class TodoItem(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: TodoPriority = TodoPriority.MEDIUM,
    val category: TodoCategory = TodoCategory.PERSONAL,
    val dueDate: LocalDateTime? = null,
    val reminderTime: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val color: Color? = null,
    val tags: List<String> = emptyList(),
    val isRecurring: Boolean = false,
    val recurrencePattern: RecurrencePattern? = null,
    val parentTodoId: Long? = null, // For subtasks
    val orderIndex: Int = 0
)

enum class TodoPriority(val displayName: String, val color: Color) {
    LOW("Low", Color(0xFF4CAF50)),
    MEDIUM("Medium", Color(0xFFFF9800)),
    HIGH("High", Color(0xFFF44336)),
    URGENT("Urgent", Color(0xFF9C27B0))
}

enum class TodoCategory(val displayName: String, val icon: String, val color: Color) {
    PERSONAL("Personal", "👤", Color(0xFF2196F3)),
    WORK("Work", "💼", Color(0xFF4CAF50)),
    HEALTH("Health", "🏥", Color(0xFFF44336)),
    FITNESS("Fitness", "💪", Color(0xFFFF9800)),
    STUDY("Study", "📚", Color(0xFF9C27B0)),
    SHOPPING("Shopping", "🛒", Color(0xFF607D8B)),
    BILLS("Bills", "💰", Color(0xFFFFC107)),
    CREATIVE("Creative", "🎨", Color(0xFFE91E63)),
    TRAVEL("Travel", "✈️", Color(0xFF00BCD4)),
    OTHER("Other", "📝", Color(0xFF795548))
}

data class TodoUiState(
    val todos: List<TodoItem> = emptyList(),
    val selectedCategory: TodoCategory? = null,
    val selectedPriority: TodoPriority? = null,
    val showCompleted: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class TodoEvent {
    data class AddTodo(val todo: TodoItem) : TodoEvent()
    data class UpdateTodo(val todo: TodoItem) : TodoEvent()
    data class DeleteTodo(val todoId: Long) : TodoEvent()
    data class ToggleComplete(val todoId: Long) : TodoEvent()
    data class SetCategory(val category: TodoCategory?) : TodoEvent()
    data class SetPriority(val priority: TodoPriority?) : TodoEvent()
    data class SetShowCompleted(val show: Boolean) : TodoEvent()
    data class SetSearchQuery(val query: String) : TodoEvent()
    object LoadTodos : TodoEvent()
    object DeleteAllCompleted : TodoEvent()
    object MarkAllCompleted : TodoEvent()
} 