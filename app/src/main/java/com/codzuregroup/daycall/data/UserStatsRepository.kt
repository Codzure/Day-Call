package com.codzuregroup.daycall.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class UserStatsRepository(private val userStatsDao: UserStatsDao) {
    
    fun getUserStats(): Flow<UserStatsEntity?> = userStatsDao.getUserStats()
    
    suspend fun getUserStatsSync(): UserStatsEntity? = userStatsDao.getUserStatsSync()
    
    suspend fun initializeUserStats() {
        val existingStats = getUserStatsSync()
        if (existingStats == null) {
            val initialStats = UserStatsEntity()
            userStatsDao.insertUserStats(initialStats)
        }
    }
    
    suspend fun addPoints(points: Int) {
        userStatsDao.addPoints(points)
        
        // Check for level up
        val stats = getUserStatsSync()
        stats?.let {
            val newLevel = calculateLevel(it.totalPoints + points)
            if (newLevel > it.level) {
                userStatsDao.updateLevel(newLevel)
            }
        }
    }
    
    suspend fun updateStreak(streak: Int) {
        userStatsDao.updateStreak(streak)
    }
    
    suspend fun incrementDailyTasks() {
        userStatsDao.incrementDailyTasks(LocalDateTime.now())
    }
    
    suspend fun resetDailyTasksIfNeeded() {
        val stats = getUserStatsSync()
        stats?.let {
            val lastActivity = it.lastActivityDate
            val today = LocalDateTime.now()
            
            // Reset if it's a new day
            if (lastActivity.toLocalDate() != today.toLocalDate()) {
                userStatsDao.resetDailyTasks()
            }
        }
    }
    
    suspend fun updateAchievements(achievements: List<String>) {
        val achievementsJson = achievements.joinToString(",")
        userStatsDao.updateAchievements(achievementsJson)
    }
    
    private fun calculateLevel(totalPoints: Int): Int {
        // Level calculation: Level 1 = 0-99 points, Level 2 = 100-299, Level 3 = 300-599, etc.
        return when {
            totalPoints < 100 -> 1
            totalPoints < 300 -> 2
            totalPoints < 600 -> 3
            totalPoints < 1000 -> 4
            totalPoints < 1500 -> 5
            totalPoints < 2100 -> 6
            totalPoints < 2800 -> 7
            totalPoints < 3600 -> 8
            totalPoints < 4500 -> 9
            else -> 10 + (totalPoints - 4500) / 1000
        }
    }
    
    fun getPointsForNextLevel(currentPoints: Int): Int {
        val currentLevel = calculateLevel(currentPoints)
        return when (currentLevel) {
            1 -> 100 - currentPoints
            2 -> 300 - currentPoints
            3 -> 600 - currentPoints
            4 -> 1000 - currentPoints
            5 -> 1500 - currentPoints
            6 -> 2100 - currentPoints
            7 -> 2800 - currentPoints
            8 -> 3600 - currentPoints
            9 -> 4500 - currentPoints
            else -> ((currentLevel - 9) * 1000 + 4500) - currentPoints
        }
    }
}