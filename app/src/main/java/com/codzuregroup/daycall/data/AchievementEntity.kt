package com.codzuregroup.daycall.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String, // Achievement ID like "first_task", "streak_7", etc.
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "icon") val icon: String, // Emoji or icon name
    @ColumnInfo(name = "points_required") val pointsRequired: Int,
    @ColumnInfo(name = "is_unlocked") val isUnlocked: Boolean = false,
    @ColumnInfo(name = "unlocked_at") val unlockedAt: LocalDateTime? = null,
    @ColumnInfo(name = "category") val category: String = "general", // general, streak, productivity, etc.
    @ColumnInfo(name = "rarity") val rarity: String = "common" // common, rare, epic, legendary
)