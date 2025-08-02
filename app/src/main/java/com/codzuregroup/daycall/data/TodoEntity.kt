package com.codzuregroup.daycall.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "priority") val priority: String = "MEDIUM",
    @ColumnInfo(name = "category") val category: String = "PERSONAL",
    @ColumnInfo(name = "due_date") val dueDate: LocalDateTime? = null,
    @ColumnInfo(name = "reminder_time") val reminderTime: LocalDateTime? = null,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "completed_at") val completedAt: LocalDateTime? = null,
    @ColumnInfo(name = "color") val color: String? = null,
    @ColumnInfo(name = "tags") val tags: String = "", // Comma-separated tags
    @ColumnInfo(name = "is_recurring") val isRecurring: Boolean = false,
    @ColumnInfo(name = "recurrence_pattern") val recurrencePattern: String = "", // DAILY, WEEKLY, MONTHLY
    @ColumnInfo(name = "parent_todo_id") val parentTodoId: Long? = null, // For subtasks
    @ColumnInfo(name = "order_index") val orderIndex: Int = 0
) 