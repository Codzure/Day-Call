package com.codzuregroup.daycall.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TaskTemplateDao {
    @Query("SELECT * FROM task_templates ORDER BY use_count DESC, title ASC")
    fun getAllTemplates(): Flow<List<TaskTemplateEntity>>
    
    @Query("SELECT * FROM task_templates WHERE category = :category ORDER BY use_count DESC, title ASC")
    fun getTemplatesByCategory(category: String): Flow<List<TaskTemplateEntity>>
    
    @Query("SELECT * FROM task_templates ORDER BY use_count DESC LIMIT :limit")
    fun getMostUsedTemplates(limit: Int = 5): Flow<List<TaskTemplateEntity>>
    
    @Query("SELECT * FROM task_templates WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY use_count DESC")
    fun searchTemplates(query: String): Flow<List<TaskTemplateEntity>>
    
    @Query("SELECT * FROM task_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): TaskTemplateEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TaskTemplateEntity): Long
    
    @Update
    suspend fun updateTemplate(template: TaskTemplateEntity)
    
    @Delete
    suspend fun deleteTemplate(template: TaskTemplateEntity)
    
    @Query("DELETE FROM task_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)
    
    @Query("UPDATE task_templates SET use_count = use_count + 1, last_used = :lastUsed WHERE id = :id")
    suspend fun incrementUseCount(id: Long, lastUsed: LocalDateTime)
}