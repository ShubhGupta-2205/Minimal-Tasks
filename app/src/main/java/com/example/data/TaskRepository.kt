package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskRepository(
    private val taskDao: TaskDao,
    private val settingsPreferences: SettingsPreferences
) {
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Long): TaskItem? = taskDao.getTaskById(id)

    suspend fun insertTask(
        title: String,
        reminderTimeMillis: Long? = null,
        autoUntickTomorrow: Boolean = false
    ): Long {
        val maxOrder = taskDao.getMaxOrderIndex() ?: 0
        val newTask = TaskItem(
            title = title.trim(),
            isCompleted = false,
            orderIndex = maxOrder + 1,
            reminderTimeMillis = reminderTimeMillis,
            autoUntickTomorrow = autoUntickTomorrow,
            createdAtMillis = System.currentTimeMillis()
        )
        return taskDao.insertTask(newTask)
    }

    suspend fun toggleTaskCompletion(task: TaskItem): TaskItem {
        val newCompleted = !task.isCompleted
        val maxOrder = taskDao.getMaxOrderIndex() ?: 0
        val updated = task.copy(
            isCompleted = newCompleted,
            orderIndex = if (newCompleted) maxOrder + 1 else 0,
            completedAtMillis = if (newCompleted) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)
        return updated
    }

    suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskItem) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        taskDao.deleteTaskById(id)
    }

    suspend fun deleteTasks(ids: Set<Long>) {
        if (ids.isNotEmpty()) {
            taskDao.deleteTasksByIds(ids.toList())
        }
    }

    suspend fun markTasksCompleted(ids: Set<Long>, isCompleted: Boolean) {
        if (ids.isNotEmpty()) {
            val completedAt = if (isCompleted) System.currentTimeMillis() else null
            taskDao.updateTasksCompletion(ids.toList(), isCompleted, completedAt)
        }
    }

    suspend fun updateReminder(taskId: Long, reminderTimeMillis: Long?, customToneName: String? = null) {
        val task = taskDao.getTaskById(taskId) ?: return
        taskDao.updateTask(task.copy(reminderTimeMillis = reminderTimeMillis, customToneName = customToneName))
    }

    suspend fun updateAutoUntick(taskId: Long, autoUntick: Boolean) {
        val task = taskDao.getTaskById(taskId) ?: return
        taskDao.updateTask(task.copy(autoUntickTomorrow = autoUntick))
    }

    suspend fun checkAndPerformAutoUntick(): Int {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val lastDay = settingsPreferences.settings.value.lastAutoUntickDay

        if (lastDay != currentDay) {
            val completedWithAutoUntick = taskDao.getCompletedTasksWithAutoUntick()
            var count = 0
            for (task in completedWithAutoUntick) {
                // If it was completed before today (or has auto-untick set)
                taskDao.updateTask(
                    task.copy(
                        isCompleted = false,
                        completedAtMillis = null,
                        orderIndex = 0
                    )
                )
                count++
            }
            settingsPreferences.setLastAutoUntickDay(currentDay)
            return count
        }
        return 0
    }

    suspend fun getUpcomingReminderTasks(): List<TaskItem> {
        return taskDao.getTasksWithUpcomingReminders(System.currentTimeMillis())
    }
}
