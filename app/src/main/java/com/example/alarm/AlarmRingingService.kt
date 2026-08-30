package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.AlarmItem
import com.example.data.AlarmType
import com.example.data.AppDatabase
import com.example.ui.alarm.AlarmAlertActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AlarmRingingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private var currentAlarm: AlarmItem? = null
    private var failsafeJob: kotlinx.coroutines.Job? = null
    private var currentSnoozeCount: Int = 0

    companion object {
        const val CHANNEL_ALARM_RINGING = "channel_alarm_ringing"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_ALARM = "ACTION_START_ALARM"
        const val ACTION_DISMISS = "ACTION_DISMISS"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"

        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
        const val EXTRA_IS_SNOOZE = "EXTRA_IS_SNOOZE"
        const val EXTRA_SNOOZE_COUNT = "EXTRA_SNOOZE_COUNT"

        const val FAILSAFE_TIMEOUT_MS = 2 * 60 * 1000L // 2 minutes ringing timeout
        const val FAILSAFE_AUTO_SNOOZE_MINUTES = 5 // Auto-snooze for 5 minutes
        const val MAX_SNOOZE_COUNT = 3 // Max 3 snoozes before auto-dismiss

        @Volatile
        var isRinging: Boolean = false
            private set

        @Volatile
        var activeRingingAlarm: AlarmItem? = null
            private set

        @Volatile
        var activeSnoozeCount: Int = 0
            private set
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "MinimalTasks:AlarmWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(10 * 60 * 1000L) // 10 minutes safety timeout
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ALARM
        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        val isSnooze = intent?.getBooleanExtra(EXTRA_IS_SNOOZE, false) ?: false
        val snoozeCount = intent?.getIntExtra(EXTRA_SNOOZE_COUNT, 0) ?: 0
        currentSnoozeCount = snoozeCount
        activeSnoozeCount = snoozeCount

        when (action) {
            ACTION_DISMISS -> {
                failsafeJob?.cancel()
                handleDismiss(alarmId)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_SNOOZE -> {
                failsafeJob?.cancel()
                handleSnooze(alarmId, snoozeCount + 1)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START_ALARM -> {
                if (alarmId != -1L) {
                    serviceScope.launch {
                        val db = AppDatabase.getDatabase(applicationContext)
                        val alarm = db.alarmDao().getAlarmById(alarmId)
                        if (alarm != null) {
                            currentAlarm = alarm
                            activeRingingAlarm = alarm
                            isRinging = true
                            startAlarmRinging(alarm, isSnooze, snoozeCount)
                        } else {
                            // Fallback default alarm
                            val fallback = AlarmItem(
                                id = alarmId,
                                title = if (isSnooze) "Snoozed Alarm" else "Alarm",
                                type = AlarmType.NORMAL
                            )
                            currentAlarm = fallback
                            activeRingingAlarm = fallback
                            isRinging = true
                            startAlarmRinging(fallback, isSnooze, snoozeCount)
                        }
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun startAlarmRinging(alarm: AlarmItem, isSnooze: Boolean, snoozeCount: Int) {
        val notification = buildAlarmNotification(alarm, isSnooze, snoozeCount)
        startForeground(NOTIFICATION_ID, notification)

        // Start ringtone sound
        AudioToneGenerator.startPlayingRingtone(
            context = applicationContext,
            ringtoneName = alarm.ringtoneName,
            ringtoneUriStr = alarm.ringtoneUri
        )

        // Start vibration
        if (alarm.vibrate) {
            val pattern = longArrayOf(0, 500, 300, 500, 300, 800, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }

        // Start 2-minute failsafe timer
        failsafeJob?.cancel()
        failsafeJob = serviceScope.launch {
            kotlinx.coroutines.delay(FAILSAFE_TIMEOUT_MS)
            // If still ringing after 2 minutes uninterrupted:
            Log.d("AlarmRingingService", "2-minute failsafe triggered. Current snooze count: $snoozeCount")
            if (snoozeCount < MAX_SNOOZE_COUNT) {
                val nextCount = snoozeCount + 1
                Log.d("AlarmRingingService", "Auto-snoozing alarm ${alarm.id} for $FAILSAFE_AUTO_SNOOZE_MINUTES min (Snooze $nextCount/$MAX_SNOOZE_COUNT)")
                AlarmSchedulerEngine.scheduleSnooze(
                    applicationContext,
                    alarm.id,
                    FAILSAFE_AUTO_SNOOZE_MINUTES,
                    nextCount
                )
            } else {
                Log.d("AlarmRingingService", "Max snoozes ($MAX_SNOOZE_COUNT) reached. Auto-dismissing alarm ${alarm.id}")
                handleDismiss(alarm.id)
            }
            stopSelf()
        }

        // Launch full-screen alert activity
        try {
            val fullScreenIntent = Intent(applicationContext, AlarmAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_ALARM_ID, alarm.id)
                putExtra(EXTRA_IS_SNOOZE, isSnooze)
                putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
            }
            startActivity(fullScreenIntent)
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Failed to start full screen activity", e)
        }
    }

    private fun buildAlarmNotification(alarm: AlarmItem, isSnooze: Boolean, snoozeCount: Int): Notification {
        val titleText = when (alarm.type) {
            AlarmType.HABIT -> "⚡ Habit Reminder: ${alarm.title}"
            AlarmType.CALENDAR -> "📅 Calendar Alarm: ${alarm.title}"
            AlarmType.NORMAL -> if (isSnooze) "⏰ Snooze ($snoozeCount/3): ${alarm.title}" else "⏰ Alarm: ${alarm.title}"
        }

        val contentText = when (alarm.type) {
            AlarmType.HABIT -> alarm.habitMessage.ifBlank { "Stay consistent with your daily habit!" }
            AlarmType.CALENDAR -> "Scheduled annual or event reminder (Failsafe 2m timeout)"
            AlarmType.NORMAL -> if (snoozeCount > 0) "Failsafe auto-snoozed $snoozeCount/3 • Tap to dismiss" else "Failsafe active (2m auto-snooze) • Tap to dismiss"
        }

        val fullScreenIntent = Intent(this, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_IS_SNOOZE, isSnooze)
            putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarm.id.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action
        val dismissIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            (alarm.id + 50000).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(this, AlarmRingingService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            (alarm.id + 60000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ALARM_RINGING)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titleText)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF00FFFF.toInt())
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "DISMISS", dismissPendingIntent)

        if (alarm.snoozeEnabled && snoozeCount < MAX_SNOOZE_COUNT) {
            builder.addAction(android.R.drawable.ic_popup_reminder, "SNOOZE (${alarm.snoozeDurationMinutes}m)", snoozePendingIntent)
        }

        return builder.build()
    }

    private fun handleDismiss(alarmId: Long) {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val alarm = db.alarmDao().getAlarmById(alarmId)
            if (alarm != null) {
                when (alarm.type) {
                    AlarmType.HABIT -> {
                        // Reschedule next habit occurrence from the DISMISS time
                        val dismissTime = System.currentTimeMillis()
                        val nextTrigger = AlarmSchedulerEngine.scheduleNextHabitAfterDismiss(applicationContext, alarm, dismissTime)
                        db.alarmDao().updateNextTriggerTime(alarm.id, nextTrigger)
                    }
                    AlarmType.CALENDAR -> {
                        if (alarm.calendarRepeatAnnually) {
                            val nextTrigger = AlarmSchedulerEngine.scheduleAlarm(applicationContext, alarm, forceRecalculate = true)
                            db.alarmDao().updateNextTriggerTime(alarm.id, nextTrigger)
                        } else {
                            // Single one-off calendar alarm completed
                            db.alarmDao().updateAlarmStatus(alarm.id, false)
                        }
                    }
                    AlarmType.NORMAL -> {
                        // Keep next day scheduled or active
                        val nextTrigger = AlarmSchedulerEngine.scheduleAlarm(applicationContext, alarm, forceRecalculate = true)
                        db.alarmDao().updateNextTriggerTime(alarm.id, nextTrigger)
                    }
                }
            }
        }
    }

    private fun handleSnooze(alarmId: Long, nextSnoozeCount: Int) {
        val snoozeMins = currentAlarm?.snoozeDurationMinutes ?: 5
        AlarmSchedulerEngine.scheduleSnooze(applicationContext, alarmId, snoozeMins, nextSnoozeCount)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ALARM_RINGING,
                "Alarm Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority full-screen alarm ringing channel"
                enableLights(true)
                lightColor = Color.CYAN
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        isRinging = false
        activeRingingAlarm = null
        AudioToneGenerator.stopPlaying()
        vibrator?.cancel()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
