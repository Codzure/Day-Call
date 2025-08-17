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
                val entity = repository.getTodoById(todoId)
                entity?.let { current ->
                    val now = LocalDateTime.now()
                    val newCompleted = !current.isCompleted

                    if (newCompleted && current.isRecurring) {
                        val nextDue = when (RecurrencePattern.valueOf(current.recurrencePattern.ifEmpty { "DAILY" })) {
                            RecurrencePattern.DAILY -> (current.dueDate ?: now).plusDays(1)
                            RecurrencePattern.WEEKLY -> (current.dueDate ?: now).plusWeeks(1)
                            RecurrencePattern.MONTHLY -> (current.dueDate ?: now).plusMonths(1)
                            RecurrencePattern.YEARLY -> (current.dueDate ?: now).plusYears(1)
                        }
                        val nextReminder = current.reminderTime?.let { r ->
                            when (RecurrencePattern.valueOf(current.recurrencePattern.ifEmpty { "DAILY" })) {
                                RecurrencePattern.DAILY -> r.plusDays(1)
                                RecurrencePattern.WEEKLY -> r.plusWeeks(1)
                                RecurrencePattern.MONTHLY -> r.plusMonths(1)
                                RecurrencePattern.YEARLY -> r.plusYears(1)
                            }
                        }
                        val rolled = current.copy(
                            isCompleted = false,
                            completedAt = null,
                            dueDate = nextDue,
                            reminderTime = nextReminder
                        )
                        repository.updateTodo(rolled)
                        scheduleReminderIfNeeded(rolled)
                    } else {
                        val completedAt = if (newCompleted) now else null
                        repository.updateTodoCompletion(todoId, newCompleted, completedAt)

                        val context = getApplication<Application>().applicationContext
                        val workManager = androidx.work.WorkManager.getInstance(context)
                        val tag = "todo_reminder_${todoId}"
                        workManager.cancelAllWorkByTag(tag)
                        if (!newCompleted) scheduleReminderIfNeeded(current)
                    }
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
                    
                    // Do not filter out completed here; keep all so UI can present both active and completed
                    
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
        try {
            val context = getApplication<Application>().applicationContext
            val workManager = androidx.work.WorkManager.getInstance(context)
            val uniqueTag = "todo_reminder_${todo.id}"
            workManager.cancelAllWorkByTag(uniqueTag)

            val reminderTime = todo.reminderTime
            if (reminderTime != null) {
                val delayMillis = java.time.Duration.between(LocalDateTime.now(), reminderTime).toMillis()
                if (delayMillis > 0) {
                    val input = androidx.work.Data.Builder()
                        .putLong("todo_id", todo.id)
                        .putString("title", todo.title)
                        .putString("description", todo.description)
                        .build()

                    val request = androidx.work.OneTimeWorkRequestBuilder<com.codzuregroup.daycall.ui.todo.TodoReminderWorker>()
                        .setInputData(input)
                        .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .addTag(uniqueTag)
                        .build()

                    workManager.enqueue(request)
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to schedule reminder: ${e.message}") }
        }
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