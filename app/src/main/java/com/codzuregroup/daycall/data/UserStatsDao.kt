package com.codzuregroup.daycall.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStatsEntity?>
    
    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsSync(): UserStatsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStatsEntity)
    
    @Update
    suspend fun updateUserStats(userStats: UserStatsEntity)
    
    @Query("UPDATE user_stats SET total_points = total_points + :points WHERE id = 1")
    suspend fun addPoints(points: Int)
    
    @Query("UPDATE user_stats SET current_streak = :streak, best_streak = CASE WHEN :streak > best_streak THEN :streak ELSE best_streak END WHERE id = 1")
    suspend fun updateStreak(streak: Int)
    
    @Query("UPDATE user_stats SET tasks_completed_today = tasks_completed_today + 1, last_activity_date = :date WHERE id = 1")
    suspend fun incrementDailyTasks(date: LocalDateTime)
    
    @Query("UPDATE user_stats SET tasks_completed_today = 0 WHERE id = 1")
    suspend fun resetDailyTasks()
    
    @Query("UPDATE user_stats SET level = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)
    
    @Query("UPDATE user_stats SET achievements_unlocked = :achievements WHERE id = 1")
    suspend fun updateAchievements(achievements: String)
}