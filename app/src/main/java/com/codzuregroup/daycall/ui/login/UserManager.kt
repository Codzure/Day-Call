package com.codzuregroup.daycall.ui.login

import android.content.Context
import com.codzuregroup.daycall.data.DayCallDatabase
import com.codzuregroup.daycall.data.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object UserManager {
    private var userRepository: UserRepository? = null
    
    fun initialize(context: Context) {
        if (userRepository == null) {
            val database = DayCallDatabase.getInstance(context)
            userRepository = UserRepository(database.userDao())
        }
    }
    
    fun getCurrentUser(): Flow<String?> {
        return userRepository?.getCurrentUser()?.map { it?.name } ?: kotlinx.coroutines.flow.flowOf(null)
    }
    
    suspend fun isLoginRequired(): Boolean {
        val currentUser = userRepository?.getCurrentUser()?.first()
        return currentUser == null || currentUser.name.isBlank()
    }
    
    suspend fun hasExistingUser(): Boolean {
        val currentUser = userRepository?.getCurrentUser()?.first()
        return currentUser != null && currentUser.name.isNotBlank()
    }
    
    suspend fun getCurrentUserName(): String? {
        val currentUser = userRepository?.getCurrentUser()?.first()
        return currentUser?.name
    }
} 