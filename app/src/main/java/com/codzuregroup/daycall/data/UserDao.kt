package com.codzuregroup.daycall.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY created_at DESC LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
} 