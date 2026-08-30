package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: AlarmType = AlarmType.NORMAL,
    val title: String = "Alarm",
    val isEnabled: Boolean = true,
    
    // Normal & General Alarm settings
    val hour: Int = 8,
    val minute: Int = 0,
    val repeatDaysOfWeek: Int = 127, // 7-bit mask (Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64). 127 = every day, 0 = once
    val ringtoneName: String = "Cyber Pulse",
    val ringtoneUri: String? = null,
    val vibrate: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val snoozeDurationMinutes: Int = 5,
    val customBackgroundUri: String? = null,
    val customBackgroundPreset: String? = null,
    
    // Habit Alarm settings
    val habitIntervalMinutes: Int = 45, // Duration interval between rings (e.g. 15, 30, 45, 60, 90 mins)
    val habitDaysOfWeek: Int = 127,     // 7-bit mask (Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64). 127 = every day
    val habitSleepHour: Int = 23,      // Sleep window start (23:00)
    val habitSleepMinute: Int = 0,
    val habitWakeHour: Int = 7,        // Wake window start (07:00)
    val habitWakeMinute: Int = 0,
    val habitMessage: String = "Time to stay consistent with your habit!",
    
    // Calendar Alarm settings
    val calendarYear: Int = 2026,
    val calendarMonth: Int = 1,        // 1-12
    val calendarDay: Int = 1,          // 1-31
    val calendarRepeatAnnually: Boolean = false, // "Ring only this year" vs "Repeat forever annually"
    
    // Engine metadata
    val nextTriggerTimeMillis: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis()
)
