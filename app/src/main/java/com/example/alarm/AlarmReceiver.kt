package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val alarmId = intent.getLongExtra(AlarmSchedulerEngine.EXTRA_ALARM_ID, -1L)
        val isSnooze = intent.getBooleanExtra(AlarmSchedulerEngine.EXTRA_IS_SNOOZE, false)
        val snoozeCount = intent.getIntExtra(AlarmSchedulerEngine.EXTRA_SNOOZE_COUNT, 0)

        Log.d("AlarmReceiver", "Alarm received: id=$alarmId, isSnooze=$isSnooze, snoozeCount=$snoozeCount")

        // Acquire screen wake lock to immediately turn on display over lock screen
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "MinimalTasks:AlarmReceiverWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(60 * 1000L) // 1 minute safety
        }

        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_START_ALARM
            putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmRingingService.EXTRA_IS_SNOOZE, isSnooze)
            putExtra(AlarmRingingService.EXTRA_SNOOZE_COUNT, snoozeCount)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start AlarmRingingService", e)
        }

        // Also launch AlarmAlertActivity directly for instant lock screen wakeup
        try {
            val alertIntent = Intent(context, com.example.ui.alarm.AlarmAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra(AlarmSchedulerEngine.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmSchedulerEngine.EXTRA_IS_SNOOZE, isSnooze)
                putExtra(AlarmSchedulerEngine.EXTRA_SNOOZE_COUNT, snoozeCount)
            }
            context.startActivity(alertIntent)
        } catch (e: Exception) {
            Log.d("AlarmReceiver", "Direct activity start handled by foreground service notification", e)
        }

        try {
            pendingResult?.finish()
        } catch (_: Exception) {}
    }
}
