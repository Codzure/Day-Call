package com.codzuregroup.daycall.ui.todo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codzuregroup.daycall.data.DayCallDatabase
import com.codzuregroup.daycall.data.TodoEntity
import com.codzuregroup.daycall.data.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import androidx.compose.ui.graphics.Color

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository
    
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<TodoCategory?>(null)
    private val _selectedPriority = MutableStateFlow<TodoPriority?>(null)
    private val _showCompleted = MutableStateFlow(false)
    
    init {
        val database = DayCallDatabase.getInstance(application)
        repository = TodoRepository(database.todoDao())
        loadTodos()
    }
    
    fun handleEvent(event: TodoEvent) {
        when (event) {
            is TodoEvent.AddTodo -> addTodo(event.todo)
            is TodoEvent.UpdateTodo -> updateTodo(event.todo)
            is TodoEvent.DeleteTodo -> deleteTodo(event.todoId)
            is TodoEvent.ToggleComplete -> toggleComplete(event.todoId)
            is TodoEvent.SetCategory -> setCategory(event.category)
            is TodoEvent.SetPriority -> setPriority(event.priority)
            is TodoEvent.SetShowCompleted -> setShowCompleted(event.show)
            is TodoEvent.SetSearchQuery -> setSearchQuery(event.query)
            is TodoEvent.LoadTodos -> loadTodos()
            is TodoEvent.DeleteAllCompleted -> deleteAllCompleted()
            is TodoEvent.MarkAllCompleted -> markAllCompleted()
        }
    }
    
    private fun addTodo(todo: TodoItem) {
        viewModelScope.launch {
            try {
                val todoEntity = TodoEntity(
                    title = todo.title,
                    description = todo.description,
                    priority = todo.priority.name,
                    category = todo.category.name,
                    dueDate = todo.dueDate,
                    reminderTime = todo.reminderTime,
                    color = todo.color?.toString(),
                    tags = todo.tags.joinToString(","),
                    isRecurring = todo.isRecurring,
                    recurrencePattern = todo.recurrencePattern?.name ?: "",
                    parentTodoId = todo.parentTodoId
                )
                
                val id = repository.insertTodo(todoEntity)
                scheduleReminderIfNeeded(todoEntity.copy(id = id))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to add todo: ${e.message}") }
            }
        }
    }
    
    private fun updateTodo(todo: TodoItem) {
        viewModelScope.launch {
            try {
                val todoEntity = TodoEntity(
                    id = todo.id,
                    title = todo.title,
                    description = todo.description,
                    isCompleted = todo.isCompleted,
                    priority = todo.priority.name,
                    category = todo.category.name,
                    dueDate = todo.dueDate,
                    reminderTime = todo.reminderTime,
                    color = todo.color?.toString(),
                    tags = todo.tags.joinToString(","),
                    isRecurring = todo.isRecurring,
                    recurrencePattern = todo.recurrencePattern?.name ?: "",
                    parentTodoId = todo.parentTodoId,
                    completedAt = todo.completedAt
                )
                
                repository.updateTodo(todoEntity)
                scheduleReminderIfNeeded(todoEntity)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update todo: ${e.message}") }
            }
        }
    }
    
    private fun deleteTodo(todoId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTodoById(todoId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete todo: ${e.message}") }
            }
        }
    }
    
    private fun toggleComplete(todoId: Long) {
        viewModelScope.launch {
            try {
                val todo = repository.getTodoById(todoId)
                todo?.let {
                    val completedAt = if (!it.isCompleted) LocalDateTime.now() else null
                    repository.updateTodoCompletion(todoId, !it.isCompleted, completedAt)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to toggle todo: ${e.message}") }
            }
        }
    }
    
    private fun setCategory(category: TodoCategory?) {
        _selectedCategory.value = category
        updateFilteredTodos()
    }
    
    private fun setPriority(priority: TodoPriority?) {
        _selectedPriority.value = priority
        updateFilteredTodos()
    }
    
    private fun setShowCompleted(show: Boolean) {
        _showCompleted.value = show
        updateFilteredTodos()
    }
    
    private fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredTodos()
    }
    
    private fun loadTodos() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                combine(
                    repository.getAllTodos(),
                    _searchQuery,
                    _selectedCategory,
                    _selectedPriority,
                    _showCompleted
                ) { todos, searchQuery, selectedCategory, selectedPriority, showCompleted ->
                    var filteredTodos = todos
                    
                    // Filter by search query
                    if (searchQuery.isNotEmpty()) {
                        filteredTodos = filteredTodos.filter { todo ->
                            todo.title.contains(searchQuery, ignoreCase = true) ||
                            todo.description.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    
                    // Filter by category
                    selectedCategory?.let { category ->
                        filteredTodos = filteredTodos.filter { it.category == category.name }
                    }
                    
                    // Filter by priority
                    selectedPriority?.let { priority ->
                        filteredTodos = filteredTodos.filter { it.priority == priority.name }
                    }
                    
                    // Filter by completion status
                    if (!showCompleted) {
                        filteredTodos = filteredTodos.filter { !it.isCompleted }
                    }
                    
                    // Convert to TodoItem
                    filteredTodos.map { entity ->
                        TodoItem(
                            id = entity.id,
                            title = entity.title,
                            description = entity.description,
                            isCompleted = entity.isCompleted,
                            priority = TodoPriority.valueOf(entity.priority),
                            category = TodoCategory.valueOf(entity.category),
                            dueDate = entity.dueDate,
                            reminderTime = entity.reminderTime,
                            color = entity.color?.let { Color(android.graphics.Color.parseColor(it)) },
                            tags = entity.tags.split(",").filter { it.isNotEmpty() },
                            isRecurring = entity.isRecurring,
                            recurrencePattern = entity.recurrencePattern.takeIf { it.isNotEmpty() }?.let { 
                                RecurrencePattern.valueOf(it) 
                            },
                            parentTodoId = entity.parentTodoId,
                            createdAt = entity.createdAt,
                            completedAt = entity.completedAt
                        )
                    }.sortedWith(
                        compareBy<TodoItem> { it.priority.ordinal }
                            .thenBy { it.dueDate }
                            .thenBy { it.createdAt }
                    )
                }.collect { todos ->
                    _uiState.update { 
                        it.copy(
                            todos = todos,
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load todos: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun deleteAllCompleted() {
        viewModelScope.launch {
            try {
                repository.deleteAllCompletedTodos()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete completed todos: ${e.message}") }
            }
        }
    }
    
    private fun markAllCompleted() {
        viewModelScope.launch {
            try {
                repository.markAllTodosAsCompleted(LocalDateTime.now())
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to mark all todos as completed: ${e.message}") }
            }
        }
    }
    
    private fun updateFilteredTodos() {
        // This will be handled by the combine operator in loadTodos()
    }
    
    private fun scheduleReminderIfNeeded(todo: TodoEntity) {
        // TODO: Implement reminder scheduling using WorkManager
        // This would schedule a notification for the reminder time
    }
    
    fun getFilteredTodos(): List<TodoItem> {
        return _uiState.value.todos
    }
    
    fun getStats(): TodoStats {
        val todos = _uiState.value.todos
        return TodoStats(
            total = todos.size,
            completed = todos.count { it.isCompleted },
            pending = todos.count { !it.isCompleted },
            overdue = todos.count { 
                it.dueDate != null && it.dueDate.isBefore(LocalDateTime.now()) && !it.isCompleted 
            }
        )
    }
}

data class TodoStats(
    val total: Int,
    val completed: Int,
    val pending: Int,
    val overdue: Int
)

enum class RecurrencePattern {
    DAILY, WEEKLY, MONTHLY, YEARLY
} 