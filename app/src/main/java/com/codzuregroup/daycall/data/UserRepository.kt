package com.codzuregroup.daycall.data

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    
    fun getCurrentUser(): Flow<UserEntity?> {
        return userDao.getCurrentUser()
    }
    
    suspend fun saveUser(name: String): Long {
        val user = UserEntity(name = name)
        return userDao.insertUser(user)
    }
    
    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }
} 