package com.codzuregroup.daycall.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class TodoRepository(private val todoDao: TodoDao) {
    
    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllTodos()
    
    fun getActiveTodos(): Flow<List<TodoEntity>> = todoDao.getActiveTodos()
    
    fun getCompletedTodos(): Flow<List<TodoEntity>> = todoDao.getCompletedTodos()
    
    fun getTodosByCategory(category: String): Flow<List<TodoEntity>> = todoDao.getTodosByCategory(category)
    
    fun getTodosByPriority(priority: String): Flow<List<TodoEntity>> = todoDao.getTodosByPriority(priority)
    
    fun getTodosWithDueDate(startDate: LocalDateTime): Flow<List<TodoEntity>> = todoDao.getTodosWithDueDate(startDate)
    
    fun getTodosWithReminders(startDate: LocalDateTime): Flow<List<TodoEntity>> = todoDao.getTodosWithReminders(startDate)
    
    fun getSubtasks(parentId: Long): Flow<List<TodoEntity>> = todoDao.getSubtasks(parentId)
    
    fun searchTodos(query: String): Flow<List<TodoEntity>> = todoDao.searchTodos(query)
    
    fun getAllCategories(): Flow<List<String>> = todoDao.getAllCategories()
    
    fun getActiveTodoCount(): Flow<Int> = todoDao.getActiveTodoCount()
    
    fun getCompletedTodoCount(): Flow<Int> = todoDao.getCompletedTodoCount()
    
    fun getTodoCountByCategory(category: String): Flow<Int> = todoDao.getTodoCountByCategory(category)
    
    suspend fun getTodoById(id: Long): TodoEntity? = todoDao.getTodoById(id)
    
    suspend fun insertTodo(todo: TodoEntity): Long = todoDao.insertTodo(todo)
    
    suspend fun updateTodo(todo: TodoEntity) = todoDao.updateTodo(todo)
    
    suspend fun deleteTodo(todo: TodoEntity) = todoDao.deleteTodo(todo)
    
    suspend fun deleteTodoById(id: Long) = todoDao.deleteTodoById(id)
    
    suspend fun updateTodoCompletion(id: Long, completed: Boolean, completedAt: LocalDateTime?) = 
        todoDao.updateTodoCompletion(id, completed, completedAt)
    
    suspend fun updateTodoOrder(id: Long, orderIndex: Int) = todoDao.updateTodoOrder(id, orderIndex)
    
    suspend fun deleteAllCompletedTodos() = todoDao.deleteAllCompletedTodos()
    
    suspend fun markAllTodosAsCompleted(completedAt: LocalDateTime) = todoDao.markAllTodosAsCompleted(completedAt)
    
    fun getOverdueTodos(now: LocalDateTime): Flow<List<TodoEntity>> = todoDao.getOverdueTodos(now)
    
    fun getTodosDueBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TodoEntity>> = 
        todoDao.getTodosDueBetween(startDate, endDate)
} 