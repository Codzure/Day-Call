package com.codzuregroup.daycall.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY category, rarity, title")
    fun getAllAchievements(): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE is_unlocked = 1 ORDER BY unlocked_at DESC")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE is_unlocked = 0 ORDER BY points_required ASC")
    fun getLockedAchievements(): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY rarity, title")
    fun getAchievementsByCategory(category: String): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): AchievementEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)
    
    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
    
    @Query("UPDATE achievements SET is_unlocked = 1, unlocked_at = :unlockedAt WHERE id = :id")
    suspend fun unlockAchievement(id: String, unlockedAt: LocalDateTime)
    
    @Query("SELECT COUNT(*) FROM achievements WHERE is_unlocked = 1")
    suspend fun getUnlockedCount(): Int
    
    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalCount(): Int
}