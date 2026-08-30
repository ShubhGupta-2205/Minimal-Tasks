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

class DailyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, 1000)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Daily Minimal Tasks"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Check your daily task goals."

        NotificationHelper.showDailyReminderNotification(
            context = context,
            notificationId = reminderId,
            title = title,
            message = message
        )

        // Perform auto-untick check asynchronously
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val settingsPrefs = SettingsPreferences(context)
                val repository = TaskRepository(db.taskDao(), settingsPrefs)
                repository.checkAndPerformAutoUntick()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
