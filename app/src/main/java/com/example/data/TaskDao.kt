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

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, orderIndex ASC, id ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskItem?

    @Query("SELECT * FROM tasks WHERE reminderTimeMillis IS NOT NULL AND reminderTimeMillis > :now")
    suspend fun getTasksWithUpcomingReminders(now: Long): List<TaskItem>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND autoUntickTomorrow = 1")
    suspend fun getCompletedTasksWithAutoUntick(): List<TaskItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks WHERE id IN (:ids)")
    suspend fun deleteTasksByIds(ids: List<Long>)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAtMillis = :completedAt WHERE id IN (:ids)")
    suspend fun updateTasksCompletion(ids: List<Long>, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET isCompleted = 0, completedAtMillis = NULL WHERE id = :id")
    suspend fun untickTask(id: Long)

    @Query("SELECT MAX(orderIndex) FROM tasks")
    suspend fun getMaxOrderIndex(): Int?
}
