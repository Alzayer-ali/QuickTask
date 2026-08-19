package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateMillis IS NULL, dueDateMillis ASC, priority DESC, createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDateMillis IS NULL, dueDateMillis ASC, priority DESC, createdAt DESC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDateMillis IS NULL, dueDateMillis ASC, priority DESC, createdAt DESC")
    fun getPendingTasksDirect(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getPendingTasksCountDirect(): Int

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC, createdAt DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean, completedAt: Long?)
}
