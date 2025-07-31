package com.codzuregroup.daycall.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codzuregroup.daycall.data.DayCallDatabase
import com.codzuregroup.daycall.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val name: String = "",
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        val database = DayCallDatabase.getInstance(application)
        userRepository = UserRepository(database.userDao())
    }
    
    fun updateName(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = name.trim(),
                nameError = null
            )
        }
    }
    
    fun saveUser() {
        val name = _uiState.value.name.trim()
        
        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "Please enter your name") }
            return
        }
        
        if (name.length < 2) {
            _uiState.update { it.copy(nameError = "Name must be at least 2 characters") }
            return
        }
        
        if (name.length > 50) {
            _uiState.update { it.copy(nameError = "Name must be less than 50 characters") }
            return
        }
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                userRepository.saveUser(name)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        nameError = "Failed to save name. Please try again."
                    )
                }
            }
        }
    }
} 