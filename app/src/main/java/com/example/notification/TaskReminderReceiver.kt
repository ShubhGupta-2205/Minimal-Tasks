package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.AudioToneGenerator

class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_CUSTOM_TONE = "extra_custom_tone"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Your Task"
        val customTone = intent.getStringExtra(EXTRA_CUSTOM_TONE)

        if (taskId != -1L) {
            AudioToneGenerator.playTaskPing(context, customTone)
            NotificationHelper.showTaskReminderNotification(context, taskId, taskTitle)
        }
    }
}
