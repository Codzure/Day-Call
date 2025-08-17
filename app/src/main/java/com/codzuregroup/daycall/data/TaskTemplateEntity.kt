package com.codzuregroup.daycall.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "task_templates")
data class TaskTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "priority") val priority: String = "MEDIUM",
    @ColumnInfo(name = "category") val category: String = "PERSONAL",
    @ColumnInfo(name = "tags") val tags: String = "", // Comma-separated tags
    @ColumnInfo(name = "estimated_duration") val estimatedDuration: Int = 30, // minutes
    @ColumnInfo(name = "use_count") val useCount: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "last_used") val lastUsed: LocalDateTime? = null
)