package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.SettingsPreferences
import com.example.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val settings = SettingsPreferences(context)
                    val repo = TaskRepository(db.taskDao(), settings)

                    // Reschedule task reminders
                    val upcoming = repo.getUpcomingReminderTasks()
                    for (task in upcoming) {
                        task.reminderTimeMillis?.let { triggerTime ->
                            AlarmScheduler.scheduleTaskReminder(
                                context,
                                task.id,
                                task.title,
                                triggerTime
                            )
                        }
                    }

                    // Reschedule daily reminders
                    val prefs = settings.settings.value
                    if (prefs.morningReminderEnabled) {
                        AlarmScheduler.scheduleDailyReminder(
                            context = context,
                            requestCode = AlarmScheduler.REQUEST_CODE_MORNING_DAILY,
                            hour = prefs.morningReminderHour,
                            minute = prefs.morningReminderMinute,
                            title = "🌅 Morning Task Briefing",
                            message = "Start your morning focused. Here are your tasks for today."
                        )
                    }
                    if (prefs.eveningReminderEnabled) {
                        AlarmScheduler.scheduleDailyReminder(
                            context = context,
                            requestCode = AlarmScheduler.REQUEST_CODE_EVENING_DAILY,
                            hour = prefs.eveningReminderHour,
                            minute = prefs.eveningReminderMinute,
                            title = "🌙 Evening Task Wrap-up",
                            message = "Check off what you completed today before winding down."
                        )
                    }

                    // Reschedule active alarms (Normal, Habit, Calendar)
                    com.example.alarm.AlarmSchedulerEngine.rescheduleAllActiveAlarms(context, db.alarmDao())

                    // Check auto-untick
                    repo.checkAndPerformAutoUntick()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
