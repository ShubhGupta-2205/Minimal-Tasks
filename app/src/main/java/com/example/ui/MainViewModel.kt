package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmSchedulerEngine
import com.example.data.AlarmItem
import com.example.data.AlarmType
import com.example.data.AppDatabase
import com.example.data.AppSettingsState
import com.example.data.AppThemeMode
import com.example.data.BackgroundThemeType
import com.example.data.SettingsPreferences
import com.example.data.TaskItem
import com.example.data.TaskRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val settingsPreferences = SettingsPreferences(context)
    private val repository = TaskRepository(database.taskDao(), settingsPreferences)

    val tasks: StateFlow<List<TaskItem>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val alarms: StateFlow<List<AlarmItem>> = database.alarmDao().getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings: StateFlow<AppSettingsState> = settingsPreferences.settings

    init {
        NotificationHelper.createNotificationChannels(context)
        checkAndInitialize()
    }

    private fun checkAndInitialize() {
        viewModelScope.launch {
            // Check auto-untick for morning reset
            repository.checkAndPerformAutoUntick()

            // Prepopulate initial tasks if database is empty on first run
            val currentTasks = database.taskDao().getMaxOrderIndex()
            if (currentTasks == null) {
                repository.insertTask("Welcome to Minimal Tasks")
                repository.insertTask("Hold any task to set reminders or delete")
                repository.insertTask("Tap Settings at bottom right to change theme", autoUntickTomorrow = true)
                repository.insertTask("Swipe right for Alarms & Habit Interval Hub")
            }

            // Sync daily reminder schedules
            syncDailyReminders()

            // Reschedule active alarms
            AlarmSchedulerEngine.rescheduleAllActiveAlarms(context, database.alarmDao())
        }
    }

    fun addAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            val nextTrigger = if (alarm.isEnabled) {
                AlarmSchedulerEngine.calculateNextOccurrence(alarm)
            } else 0L
            val alarmWithTrigger = alarm.copy(nextTriggerTimeMillis = nextTrigger)
            val id = database.alarmDao().insertAlarm(alarmWithTrigger)
            if (alarmWithTrigger.isEnabled) {
                val scheduled = AlarmSchedulerEngine.scheduleAlarm(context, alarmWithTrigger.copy(id = id), forceRecalculate = false)
                database.alarmDao().updateNextTriggerTime(id, scheduled)
            }
        }
    }

    fun updateAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            if (alarm.isEnabled) {
                val nextTrigger = if (alarm.type == AlarmType.HABIT && alarm.nextTriggerTimeMillis > System.currentTimeMillis()) {
                    alarm.nextTriggerTimeMillis
                } else {
                    AlarmSchedulerEngine.calculateNextOccurrence(alarm)
                }
                val updated = alarm.copy(nextTriggerTimeMillis = nextTrigger)
                database.alarmDao().updateAlarm(updated)
                AlarmSchedulerEngine.scheduleAlarm(context, updated, forceRecalculate = false)
            } else {
                val updated = alarm.copy(nextTriggerTimeMillis = 0L)
                database.alarmDao().updateAlarm(updated)
                AlarmSchedulerEngine.cancelAlarm(context, alarm.id)
            }
        }
    }

    fun toggleAlarm(alarm: AlarmItem, isEnabled: Boolean) {
        viewModelScope.launch {
            if (isEnabled) {
                val nextTrigger = if (alarm.type == AlarmType.HABIT && alarm.nextTriggerTimeMillis > System.currentTimeMillis()) {
                    alarm.nextTriggerTimeMillis
                } else {
                    AlarmSchedulerEngine.calculateNextOccurrence(alarm)
                }
                val updated = alarm.copy(isEnabled = true, nextTriggerTimeMillis = nextTrigger)
                database.alarmDao().updateAlarm(updated)
                AlarmSchedulerEngine.scheduleAlarm(context, updated, forceRecalculate = false)
            } else {
                val updated = alarm.copy(isEnabled = false, nextTriggerTimeMillis = 0L)
                database.alarmDao().updateAlarm(updated)
                AlarmSchedulerEngine.cancelAlarm(context, alarm.id)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmItem) {
        viewModelScope.launch {
            AlarmSchedulerEngine.cancelAlarm(context, alarm.id)
            database.alarmDao().deleteAlarm(alarm)
        }
    }

    private fun syncDailyReminders() {
        val currentSettings = settingsPreferences.settings.value
        if (currentSettings.morningReminderEnabled) {
            AlarmScheduler.scheduleDailyReminder(
                context = context,
                requestCode = AlarmScheduler.REQUEST_CODE_MORNING_DAILY,
                hour = currentSettings.morningReminderHour,
                minute = currentSettings.morningReminderMinute,
                title = "🌅 Morning Task Briefing",
                message = "Start your morning focused. Here are your tasks for today."
            )
        } else {
            AlarmScheduler.cancelDailyReminder(context, AlarmScheduler.REQUEST_CODE_MORNING_DAILY)
        }

        if (currentSettings.eveningReminderEnabled) {
            AlarmScheduler.scheduleDailyReminder(
                context = context,
                requestCode = AlarmScheduler.REQUEST_CODE_EVENING_DAILY,
                hour = currentSettings.eveningReminderHour,
                minute = currentSettings.eveningReminderMinute,
                title = "🌙 Evening Task Wrap-up",
                message = "Check off what you completed today before winding down."
            )
        } else {
            AlarmScheduler.cancelDailyReminder(context, AlarmScheduler.REQUEST_CODE_EVENING_DAILY)
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            repository.insertTask(title)
        }
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            AlarmScheduler.cancelTaskReminder(context, task.id)
            repository.deleteTask(task)
        }
    }

    fun deleteTasks(taskIds: Set<Long>) {
        viewModelScope.launch {
            taskIds.forEach { id ->
                AlarmScheduler.cancelTaskReminder(context, id)
            }
            repository.deleteTasks(taskIds)
        }
    }

    fun completeTasks(taskIds: Set<Long>, isCompleted: Boolean = true) {
        viewModelScope.launch {
            repository.markTasksCompleted(taskIds, isCompleted)
        }
    }

    fun deleteAlarms(alarmIds: Set<Long>) {
        viewModelScope.launch {
            alarmIds.forEach { id ->
                AlarmSchedulerEngine.cancelAlarm(context, id)
            }
            database.alarmDao().deleteAlarmsByIds(alarmIds.toList())
        }
    }

    fun toggleAlarmsEnabled(alarmIds: Set<Long>, isEnabled: Boolean) {
        viewModelScope.launch {
            database.alarmDao().updateAlarmsStatus(alarmIds.toList(), isEnabled)
            alarmIds.forEach { id ->
                val alarm = database.alarmDao().getAlarmById(id)
                if (alarm != null) {
                    if (isEnabled) {
                        val nextTrigger = AlarmSchedulerEngine.scheduleAlarm(context, alarm)
                        database.alarmDao().updateNextTriggerTime(id, nextTrigger)
                    } else {
                        AlarmSchedulerEngine.cancelAlarm(context, id)
                    }
                }
            }
        }
    }

    fun setTaskReminder(task: TaskItem, reminderTimeMillis: Long, customToneName: String? = null) {
        viewModelScope.launch {
            repository.updateReminder(task.id, reminderTimeMillis, customToneName)
            AlarmScheduler.scheduleTaskReminder(
                context = context,
                taskId = task.id,
                taskTitle = task.title,
                triggerAtMillis = reminderTimeMillis,
                customToneName = customToneName
            )
        }
    }

    fun clearTaskReminder(task: TaskItem) {
        viewModelScope.launch {
            AlarmScheduler.cancelTaskReminder(context, task.id)
            repository.updateReminder(task.id, null)
        }
    }

    fun toggleAutoUntickTomorrow(task: TaskItem, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateAutoUntick(task.id, enabled)
        }
    }

    fun updateTaskTitle(task: TaskItem, newTitle: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(title = newTitle))
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        settingsPreferences.setThemeMode(mode)
    }

    fun setCustomThemeSettings(
        bgColor: Long,
        taskColor: Long,
        normalAlarmColor: Long,
        habitAlarmColor: Long,
        calendarAlarmColor: Long,
        accentColor: Long,
        transparency: Float,
        cornerRadius: Float,
        borderWidth: Float
    ) {
        settingsPreferences.setCustomThemeSettings(
            bgColor = bgColor,
            taskColor = taskColor,
            normalAlarmColor = normalAlarmColor,
            habitAlarmColor = habitAlarmColor,
            calendarAlarmColor = calendarAlarmColor,
            accentColor = accentColor,
            transparency = transparency,
            cornerRadius = cornerRadius,
            borderWidth = borderWidth
        )
    }

    fun resetCustomThemeToDefaults() {
        settingsPreferences.resetCustomThemeToDefaults()
    }

    fun setBackgroundType(type: BackgroundThemeType, customUri: String?) {
        settingsPreferences.setBackgroundType(type, customUri)
    }

    fun setMorningReminder(enabled: Boolean, hour: Int, minute: Int) {
        settingsPreferences.setMorningReminder(enabled, hour, minute)
        if (enabled) {
            AlarmScheduler.scheduleDailyReminder(
                context = context,
                requestCode = AlarmScheduler.REQUEST_CODE_MORNING_DAILY,
                hour = hour,
                minute = minute,
                title = "🌅 Morning Task Briefing",
                message = "Start your morning focused. Here are your tasks for today."
            )
        } else {
            AlarmScheduler.cancelDailyReminder(context, AlarmScheduler.REQUEST_CODE_MORNING_DAILY)
        }
    }

    fun setEveningReminder(enabled: Boolean, hour: Int, minute: Int) {
        settingsPreferences.setEveningReminder(enabled, hour, minute)
        if (enabled) {
            AlarmScheduler.scheduleDailyReminder(
                context = context,
                requestCode = AlarmScheduler.REQUEST_CODE_EVENING_DAILY,
                hour = hour,
                minute = minute,
                title = "🌙 Evening Task Wrap-up",
                message = "Check off what you completed today before winding down."
            )
        } else {
            AlarmScheduler.cancelDailyReminder(context, AlarmScheduler.REQUEST_CODE_EVENING_DAILY)
        }
    }

    fun sendTestNotification() {
        NotificationHelper.showTestNotification(context)
    }

    fun triggerAutoUntickCheck() {
        viewModelScope.launch {
            repository.checkAndPerformAutoUntick()
        }
    }
}
