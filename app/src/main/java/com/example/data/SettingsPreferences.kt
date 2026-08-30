package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    MONOCHROME,
    CYBERPUNK,
    CUSTOM
}

enum class BackgroundThemeType {
    DARK_BLUE,
    CYAN_GRID,
    CUSTOM_IMAGE
}

data class AppSettingsState(
    val themeMode: AppThemeMode = AppThemeMode.MONOCHROME,
    val customSolidBgColor: Long = 0xFF000000,
    val customTaskCardColor: Long = 0xFF141414,
    val customNormalAlarmCardColor: Long = 0xFF161616,
    val customHabitAlarmCardColor: Long = 0xFF181818,
    val customCalendarAlarmCardColor: Long = 0xFF1C1C1C,
    val customAccentColor: Long = 0xFFFFFFFF,
    val customCardTransparency: Float = 0.95f,
    val customCornerRadius: Float = 14f,
    val customBorderWidth: Float = 1.0f,
    val backgroundType: BackgroundThemeType = BackgroundThemeType.DARK_BLUE,
    val customImageUri: String? = null,
    val frostedBlurIntensity: Float = 16f,
    val frostedAlpha: Float = 0.4f,
    val morningReminderEnabled: Boolean = true,
    val morningReminderHour: Int = 8,
    val morningReminderMinute: Int = 0,
    val eveningReminderEnabled: Boolean = true,
    val eveningReminderHour: Int = 20,
    val eveningReminderMinute: Int = 0,
    val lastAutoUntickDay: Int = -1
)

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minimal_tasks_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettingsState> = _settings.asStateFlow()

    private fun loadSettings(): AppSettingsState {
        val themeModeStr = prefs.getString("theme_mode", AppThemeMode.MONOCHROME.name)
        val themeMode = try {
            AppThemeMode.valueOf(themeModeStr ?: AppThemeMode.MONOCHROME.name)
        } catch (_: Exception) {
            AppThemeMode.MONOCHROME
        }

        val bgTypeStr = prefs.getString("bg_type", BackgroundThemeType.DARK_BLUE.name)
        val bgType = try {
            BackgroundThemeType.valueOf(bgTypeStr ?: BackgroundThemeType.DARK_BLUE.name)
        } catch (_: Exception) {
            BackgroundThemeType.DARK_BLUE
        }

        return AppSettingsState(
            themeMode = themeMode,
            customSolidBgColor = prefs.getLong("custom_bg_color", 0xFF000000),
            customTaskCardColor = prefs.getLong("custom_task_color", 0xFF141414),
            customNormalAlarmCardColor = prefs.getLong("custom_normal_alarm_color", 0xFF161616),
            customHabitAlarmCardColor = prefs.getLong("custom_habit_alarm_color", 0xFF181818),
            customCalendarAlarmCardColor = prefs.getLong("custom_calendar_alarm_color", 0xFF1C1C1C),
            customAccentColor = prefs.getLong("custom_accent_color", 0xFFFFFFFF),
            customCardTransparency = prefs.getFloat("custom_card_alpha", 0.95f),
            customCornerRadius = prefs.getFloat("custom_corner_radius", 14f),
            customBorderWidth = prefs.getFloat("custom_border_width", 1.0f),
            backgroundType = bgType,
            customImageUri = prefs.getString("custom_image_uri", null),
            frostedBlurIntensity = prefs.getFloat("frosted_blur", 16f),
            frostedAlpha = prefs.getFloat("frosted_alpha", 0.4f),
            morningReminderEnabled = prefs.getBoolean("morning_reminder_enabled", true),
            morningReminderHour = prefs.getInt("morning_reminder_hour", 8),
            morningReminderMinute = prefs.getInt("morning_reminder_minute", 0),
            eveningReminderEnabled = prefs.getBoolean("evening_reminder_enabled", true),
            eveningReminderHour = prefs.getInt("evening_reminder_hour", 20),
            eveningReminderMinute = prefs.getInt("evening_reminder_minute", 0),
            lastAutoUntickDay = prefs.getInt("last_auto_untick_day", -1)
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settings.value = loadSettings()
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
        prefs.edit().apply {
            putString("theme_mode", AppThemeMode.CUSTOM.name)
            putLong("custom_bg_color", bgColor)
            putLong("custom_task_color", taskColor)
            putLong("custom_normal_alarm_color", normalAlarmColor)
            putLong("custom_habit_alarm_color", habitAlarmColor)
            putLong("custom_calendar_alarm_color", calendarAlarmColor)
            putLong("custom_accent_color", accentColor)
            putFloat("custom_card_alpha", transparency)
            putFloat("custom_corner_radius", cornerRadius)
            putFloat("custom_border_width", borderWidth)
            apply()
        }
        _settings.value = loadSettings()
    }

    fun resetCustomThemeToDefaults() {
        prefs.edit().apply {
            putLong("custom_bg_color", 0xFF000000)
            putLong("custom_task_color", 0xFF141414)
            putLong("custom_normal_alarm_color", 0xFF161616)
            putLong("custom_habit_alarm_color", 0xFF181818)
            putLong("custom_calendar_alarm_color", 0xFF1C1C1C)
            putLong("custom_accent_color", 0xFFFFFFFF)
            putFloat("custom_card_alpha", 0.95f)
            putFloat("custom_corner_radius", 14f)
            putFloat("custom_border_width", 1.0f)
            apply()
        }
        _settings.value = loadSettings()
    }

    fun setBackgroundType(type: BackgroundThemeType, customUri: String? = null) {
        prefs.edit().apply {
            putString("bg_type", type.name)
            if (customUri != null) {
                putString("custom_image_uri", customUri)
            }
            apply()
        }
        _settings.value = loadSettings()
    }

    fun setMorningReminder(enabled: Boolean, hour: Int, minute: Int) {
        prefs.edit().apply {
            putBoolean("morning_reminder_enabled", enabled)
            putInt("morning_reminder_hour", hour)
            putInt("morning_reminder_minute", minute)
            apply()
        }
        _settings.value = loadSettings()
    }

    fun setEveningReminder(enabled: Boolean, hour: Int, minute: Int) {
        prefs.edit().apply {
            putBoolean("evening_reminder_enabled", enabled)
            putInt("evening_reminder_hour", hour)
            putInt("evening_reminder_minute", minute)
            apply()
        }
        _settings.value = loadSettings()
    }

    fun setLastAutoUntickDay(dayOfYear: Int) {
        prefs.edit().putInt("last_auto_untick_day", dayOfYear).apply()
        _settings.value = loadSettings()
    }

    fun resetBackgroundToDefault() {
        prefs.edit().apply {
            putString("bg_type", BackgroundThemeType.DARK_BLUE.name)
            remove("custom_image_uri")
            apply()
        }
        _settings.value = loadSettings()
    }
}
