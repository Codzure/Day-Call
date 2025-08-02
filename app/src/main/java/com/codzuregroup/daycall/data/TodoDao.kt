package com.codzuregroup.daycall.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY order_index ASC, created_at DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE is_completed = 0 ORDER BY order_index ASC, created_at DESC")
    fun getActiveTodos(): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE is_completed = 1 ORDER BY completed_at DESC")
    fun getCompletedTodos(): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE category = :category ORDER BY order_index ASC, created_at DESC")
    fun getTodosByCategory(category: String): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE priority = :priority ORDER BY order_index ASC, created_at DESC")
    fun getTodosByPriority(priority: String): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE due_date IS NOT NULL AND due_date >= :startDate ORDER BY due_date ASC")
    fun getTodosWithDueDate(startDate: LocalDateTime): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE reminder_time IS NOT NULL AND reminder_time >= :startDate ORDER BY reminder_time ASC")
    fun getTodosWithReminders(startDate: LocalDateTime): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE parent_todo_id = :parentId ORDER BY order_index ASC")
    fun getSubtasks(parentId: Long): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY order_index ASC, created_at DESC")
    fun searchTodos(query: String): Flow<List<TodoEntity>>
    
    @Query("SELECT DISTINCT category FROM todos WHERE category IS NOT NULL")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT COUNT(*) FROM todos WHERE is_completed = 0")
    fun getActiveTodoCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM todos WHERE is_completed = 1")
    fun getCompletedTodoCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM todos WHERE category = :category")
    fun getTodoCountByCategory(category: String): Flow<Int>
    
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long
    
    @Update
    suspend fun updateTodo(todo: TodoEntity)
    
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
    
    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: Long)
    
    @Query("UPDATE todos SET is_completed = :completed, completed_at = :completedAt WHERE id = :id")
    suspend fun updateTodoCompletion(id: Long, completed: Boolean, completedAt: LocalDateTime?)
    
    @Query("UPDATE todos SET order_index = :orderIndex WHERE id = :id")
    suspend fun updateTodoOrder(id: Long, orderIndex: Int)
    
    @Query("DELETE FROM todos WHERE is_completed = 1")
    suspend fun deleteAllCompletedTodos()
    
    @Query("UPDATE todos SET is_completed = 1, completed_at = :completedAt WHERE is_completed = 0")
    suspend fun markAllTodosAsCompleted(completedAt: LocalDateTime)
    
    @Query("SELECT * FROM todos WHERE due_date < :now AND is_completed = 0 ORDER BY due_date ASC")
    fun getOverdueTodos(now: LocalDateTime): Flow<List<TodoEntity>>
    
    @Query("SELECT * FROM todos WHERE due_date BETWEEN :startDate AND :endDate AND is_completed = 0 ORDER BY due_date ASC")
    fun getTodosDueBetween(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TodoEntity>>
} 