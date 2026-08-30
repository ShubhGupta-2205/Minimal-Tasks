package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.AlarmDao
import com.example.data.AlarmItem
import com.example.data.AlarmType
import java.util.Calendar

object AlarmSchedulerEngine {

    const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
    const val EXTRA_IS_SNOOZE = "EXTRA_IS_SNOOZE"
    const val EXTRA_SNOOZE_COUNT = "EXTRA_SNOOZE_COUNT"

    fun scheduleAlarm(context: Context, alarm: AlarmItem, forceRecalculate: Boolean = false): Long {
        if (!alarm.isEnabled) {
            cancelAlarm(context, alarm.id)
            return 0L
        }

        val triggerAtMillis = if (alarm.nextTriggerTimeMillis > System.currentTimeMillis() && !forceRecalculate) {
            alarm.nextTriggerTimeMillis
        } else {
            calculateNextOccurrence(alarm)
        }

        if (triggerAtMillis <= 0L) return 0L

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_IS_SNOOZE, false)
            putExtra(EXTRA_SNOOZE_COUNT, 0)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, com.example.ui.alarm.AlarmAlertActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmSchedulerEngine", "Scheduled alarm ${alarm.id} for $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.w("AlarmSchedulerEngine", "Cannot setAlarmClock, falling back", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (e2: Exception) {
                Log.e("AlarmSchedulerEngine", "Failed to schedule alarm fallback", e2)
            }
        }

        return triggerAtMillis
    }

    fun scheduleSnooze(context: Context, alarmId: Long, snoozeMinutes: Int, snoozeCount: Int = 0): Long {
        val triggerAtMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_IS_SNOOZE, true)
            putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, com.example.ui.alarm.AlarmAlertActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_IS_SNOOZE, true)
            putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            (alarmId + 100000).toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (_: Exception) {}
        }

        return triggerAtMillis
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel standard alarm
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        // Cancel snooze if any
        val snoozeIntent = Intent(context, AlarmReceiver::class.java)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 100000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (snoozePendingIntent != null) {
            alarmManager.cancel(snoozePendingIntent)
            snoozePendingIntent.cancel()
        }
    }

    fun scheduleNextHabitAfterDismiss(context: Context, alarm: AlarmItem, dismissTimeMillis: Long = System.currentTimeMillis()): Long {
        if (!alarm.isEnabled) return 0L
        val nextTrigger = calculateNextHabitOccurrence(alarm, dismissTimeMillis)
        val updatedAlarm = alarm.copy(nextTriggerTimeMillis = nextTrigger)
        return scheduleAlarm(context, updatedAlarm, forceRecalculate = false)
    }

    fun calculateNextOccurrence(alarm: AlarmItem, fromMillis: Long = System.currentTimeMillis()): Long {
        return when (alarm.type) {
            AlarmType.NORMAL -> {
                calculateNextNormalOccurrence(alarm, fromMillis)
            }

            AlarmType.HABIT -> {
                calculateNextHabitOccurrence(alarm, fromMillis)
            }

            AlarmType.CALENDAR -> {
                val currentCal = Calendar.getInstance().apply { timeInMillis = fromMillis }
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, alarm.calendarYear)
                    set(Calendar.MONTH, (alarm.calendarMonth - 1).coerceIn(0, 11))
                    set(Calendar.DAY_OF_MONTH, alarm.calendarDay.coerceIn(1, 31))
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                    set(Calendar.MINUTE, alarm.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (alarm.calendarRepeatAnnually) {
                    cal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR))
                    if (cal.timeInMillis <= fromMillis) {
                        cal.add(Calendar.YEAR, 1)
                    }
                    cal.timeInMillis
                } else {
                    cal.timeInMillis
                }
            }
        }
    }

    private fun calculateNextNormalOccurrence(alarm: AlarmItem, fromMillis: Long): Long {
        val mask = alarm.repeatDaysOfWeek
        // If mask == 0, one-off alarm
        if (mask == 0) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = fromMillis
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis <= fromMillis) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis
        }

        // Repeating alarm on specific days of the week
        for (dayOffset in 0..7) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = fromMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val dayBit = getDayBit(cal.get(Calendar.DAY_OF_WEEK))
            if ((mask and dayBit) != 0) {
                if (cal.timeInMillis > fromMillis) {
                    return cal.timeInMillis
                }
            }
        }

        // Fallback next day
        val cal = Calendar.getInstance().apply {
            timeInMillis = fromMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun calculateNextHabitOccurrence(alarm: AlarmItem, fromMillis: Long): Long {
        val intervalMs = (alarm.habitIntervalMinutes.coerceAtLeast(1) * 60 * 1000L)
        val daysMask = if (alarm.habitDaysOfWeek != 0) alarm.habitDaysOfWeek else 127

        val cal = Calendar.getInstance().apply { timeInMillis = fromMillis }
        val currentDayBit = getDayBit(cal.get(Calendar.DAY_OF_WEEK))
        val isTodayActive = (daysMask and currentDayBit) != 0

        val currentMinuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val wakeMinuteOfDay = alarm.habitWakeHour * 60 + alarm.habitWakeMinute
        val sleepMinuteOfDay = alarm.habitSleepHour * 60 + alarm.habitSleepMinute

        // Case 1: Standard Day Schedule (wake < sleep, e.g., 07:00 to 23:00)
        if (wakeMinuteOfDay < sleepMinuteOfDay) {
            if (isTodayActive) {
                if (currentMinuteOfDay < wakeMinuteOfDay) {
                    // Before wake time today: next trigger is exact wake time today
                    val wakeCal = Calendar.getInstance().apply {
                        timeInMillis = fromMillis
                        set(Calendar.HOUR_OF_DAY, alarm.habitWakeHour)
                        set(Calendar.MINUTE, alarm.habitWakeMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    return wakeCal.timeInMillis
                } else if (currentMinuteOfDay < sleepMinuteOfDay) {
                    // Currently in wake window: step by interval
                    val nextRingMillis = fromMillis + intervalMs
                    val nextCal = Calendar.getInstance().apply { timeInMillis = nextRingMillis }
                    val nextMinuteOfDay = nextCal.get(Calendar.HOUR_OF_DAY) * 60 + nextCal.get(Calendar.MINUTE)

                    if (nextMinuteOfDay < sleepMinuteOfDay && nextCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)) {
                        return nextRingMillis
                    }
                    // If next step reaches or exceeds bedtime, advance to next active day
                }
            }
        } else {
            // Case 2: Inverted/Overnight Schedule (wake >= sleep)
            if (isTodayActive) {
                val isAwake = currentMinuteOfDay >= wakeMinuteOfDay || currentMinuteOfDay < sleepMinuteOfDay
                if (isAwake) {
                    val nextRingMillis = fromMillis + intervalMs
                    val nextCal = Calendar.getInstance().apply { timeInMillis = nextRingMillis }
                    val nextMinuteOfDay = nextCal.get(Calendar.HOUR_OF_DAY) * 60 + nextCal.get(Calendar.MINUTE)
                    val nextIsAwake = nextMinuteOfDay >= wakeMinuteOfDay || nextMinuteOfDay < sleepMinuteOfDay
                    if (nextIsAwake) {
                        return nextRingMillis
                    }
                }
            }
        }

        // Advance to next active day's wake time
        for (dayOffset in 1..14) {
            val nextDayCal = Calendar.getInstance().apply {
                timeInMillis = fromMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, alarm.habitWakeHour)
                set(Calendar.MINUTE, alarm.habitWakeMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val bit = getDayBit(nextDayCal.get(Calendar.DAY_OF_WEEK))
            if ((daysMask and bit) != 0) {
                return nextDayCal.timeInMillis
            }
        }

        // Fallback
        return fromMillis + intervalMs
    }

    private fun getDayBit(calendarDayOfWeek: Int): Int {
        return when (calendarDayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 8
            Calendar.FRIDAY -> 16
            Calendar.SATURDAY -> 32
            Calendar.SUNDAY -> 64
            else -> 1
        }
    }

    suspend fun rescheduleAllActiveAlarms(context: Context, alarmDao: AlarmDao) {
        val activeAlarms = alarmDao.getActiveAlarms()
        for (alarm in activeAlarms) {
            val nextTime = scheduleAlarm(context, alarm)
            alarmDao.updateNextTriggerTime(alarm.id, nextTime)
        }
    }
}
