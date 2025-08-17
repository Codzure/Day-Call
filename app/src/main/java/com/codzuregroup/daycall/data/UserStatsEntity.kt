package com.codzuregroup.daycall.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Long = 1, // Single row for user stats
    @ColumnInfo(name = "total_points") val totalPoints: Int = 0,
    @ColumnInfo(name = "level") val level: Int = 1,
    @ColumnInfo(name = "current_streak") val currentStreak: Int = 0,
    @ColumnInfo(name = "best_streak") val bestStreak: Int = 0,
    @ColumnInfo(name = "tasks_completed_today") val tasksCompletedToday: Int = 0,
    @ColumnInfo(name = "last_activity_date") val lastActivityDate: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "achievements_unlocked") val achievementsUnlocked: String = "", // JSON string
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime = LocalDateTime.now()
)