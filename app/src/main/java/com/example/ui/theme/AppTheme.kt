package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AppSettingsState
import com.example.data.AppThemeMode

data class AppThemePalette(
    val mode: AppThemeMode,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val cardBgColor: Color,
    val cardBorderColor: Color,
    val cardGlowColor: Color,
    val cardCornerRadius: Dp,
    val cardBorderWidth: Dp,
    val cardTransparency: Float,
    val accentColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val subtleTextColor: Color,
    val taskCardColor: Color,
    val normalAlarmCardColor: Color,
    val habitAlarmCardColor: Color,
    val calendarAlarmCardColor: Color,
    val normalAlarmAccentColor: Color,
    val habitAccentColor: Color,
    val calendarAccentColor: Color,
    val dialogBgColor: Color,
    val dialogBorderColor: Color,
    val dialogGlowColor: Color,
    val switchTrackColor: Color,
    val switchThumbColor: Color,
    val buttonBgColor: Color,
    val buttonTextColor: Color,
    val outerFrameBorderColor: Color,
    val headerBorderColor: Color,
    val headerGlowColor: Color,
    val isCyberpunk: Boolean,
    val isMonochrome: Boolean,
    val isCustom: Boolean
)

fun getContrastTextColor(color: Color): Color {
    // Relative luminance calculation
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return if (luminance > 0.55f) Color(0xFF0A0A0A) else Color(0xFFFFFFFF)
}

fun getContrastSecondaryTextColor(color: Color): Color {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return if (luminance > 0.55f) Color(0xFF4A4A4A) else Color(0xFFCCCCCC)
}

fun getContrastBorderColor(color: Color): Color {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return if (luminance > 0.55f) Color(0x33000000) else Color(0x33FFFFFF)
}

val MonochromePalette = AppThemePalette(
    mode = AppThemeMode.MONOCHROME,
    backgroundColor = Color(0xFF000000), // Pure Black canvas
    surfaceColor = Color(0xFF161616), // Dark Charcoal Dialog/Sheet
    cardBgColor = Color(0xFF1E1E1E), // Distinct Charcoal Grey Tiles
    cardBorderColor = Color(0xFF383838), // Clean slate border
    cardGlowColor = Color.Transparent,
    cardCornerRadius = 14.dp,
    cardBorderWidth = 1.dp,
    cardTransparency = 1.0f,
    accentColor = Color(0xFFFFFFFF), // Clean White
    primaryTextColor = Color(0xFFFFFFFF),
    secondaryTextColor = Color(0xFFCCCCCC), // Sharp Light Gray
    subtleTextColor = Color(0xFF888888),
    taskCardColor = Color(0xFF1E1E1E), // Charcoal Grey for Tasks
    normalAlarmCardColor = Color(0xFF1E1E1E), // Charcoal Grey for Normal Alarms
    habitAlarmCardColor = Color(0xFF1E1E1E), // Charcoal Grey for Habit Alarms
    calendarAlarmCardColor = Color(0xFF1E1E1E), // Charcoal Grey for Calendar Alarms
    normalAlarmAccentColor = Color(0xFFFFFFFF),
    habitAccentColor = Color(0xFFE0E0E0), // No yellow in monochrome
    calendarAccentColor = Color(0xFFE0E0E0), // No purple in monochrome
    dialogBgColor = Color(0xFF141414),
    dialogBorderColor = Color(0xFF383838),
    dialogGlowColor = Color.Transparent,
    switchTrackColor = Color(0xFF444444),
    switchThumbColor = Color(0xFFFFFFFF),
    buttonBgColor = Color(0xFFFFFFFF),
    buttonTextColor = Color(0xFF000000),
    outerFrameBorderColor = Color(0xFF282828),
    headerBorderColor = Color(0xFF333333),
    headerGlowColor = Color.Transparent,
    isCyberpunk = false,
    isMonochrome = true,
    isCustom = false
)

val CyberpunkPalette = AppThemePalette(
    mode = AppThemeMode.CYBERPUNK,
    backgroundColor = VoidBlack,
    surfaceColor = DarkBlueDeep,
    cardBgColor = FrostedBackground,
    cardBorderColor = CyanNeon,
    cardGlowColor = CyanNeonGlow,
    cardCornerRadius = 14.dp,
    cardBorderWidth = 1.dp,
    cardTransparency = 0.4f,
    accentColor = CyanNeon,
    primaryTextColor = TextWhite,
    secondaryTextColor = TextMuted,
    subtleTextColor = TextCyanSubtle,
    taskCardColor = FrostedBackground,
    normalAlarmCardColor = FrostedBackground,
    habitAlarmCardColor = FrostedBackground,
    calendarAlarmCardColor = FrostedBackground,
    normalAlarmAccentColor = CyanNeon,
    habitAccentColor = WarningGold,
    calendarAccentColor = PurpleNeon,
    dialogBgColor = DarkBlueDeep.copy(alpha = 0.96f),
    dialogBorderColor = CyanNeon,
    dialogGlowColor = CyanNeonGlow,
    switchTrackColor = CyanNeon,
    switchThumbColor = VoidBlack,
    buttonBgColor = CyanNeon,
    buttonTextColor = VoidBlack,
    outerFrameBorderColor = CyanNeon.copy(alpha = 0.35f),
    headerBorderColor = CyanNeon,
    headerGlowColor = CyanNeonGlow,
    isCyberpunk = true,
    isMonochrome = false,
    isCustom = false
)

fun buildCustomPalette(settings: AppSettingsState): AppThemePalette {
    val bg = Color(settings.customSolidBgColor)
    val taskCard = Color(settings.customTaskCardColor)
    val normalAlarm = Color(settings.customNormalAlarmCardColor)
    val habitAlarm = Color(settings.customHabitAlarmCardColor)
    val calendarAlarm = Color(settings.customCalendarAlarmCardColor)
    val accent = Color(settings.customAccentColor)
    val radius = settings.customCornerRadius.dp
    val borderW = settings.customBorderWidth.dp
    val alpha = settings.customCardTransparency.coerceIn(0.1f, 1.0f)

    val primaryText = getContrastTextColor(bg)
    val secondaryText = getContrastSecondaryTextColor(bg)
    val subtleText = if (primaryText == Color.White) Color(0xFF8E8E8E) else Color(0xFF666666)

    return AppThemePalette(
        mode = AppThemeMode.CUSTOM,
        backgroundColor = bg,
        surfaceColor = taskCard.copy(alpha = 0.95f),
        cardBgColor = taskCard.copy(alpha = alpha),
        cardBorderColor = accent.copy(alpha = 0.6f),
        cardGlowColor = Color.Transparent,
        cardCornerRadius = radius,
        cardBorderWidth = borderW,
        cardTransparency = alpha,
        accentColor = accent,
        primaryTextColor = primaryText,
        secondaryTextColor = secondaryText,
        subtleTextColor = subtleText,
        taskCardColor = taskCard.copy(alpha = alpha),
        normalAlarmCardColor = normalAlarm.copy(alpha = alpha),
        habitAlarmCardColor = habitAlarm.copy(alpha = alpha),
        calendarAlarmCardColor = calendarAlarm.copy(alpha = alpha),
        normalAlarmAccentColor = accent,
        habitAccentColor = if (habitAlarm != taskCard) habitAlarm else accent,
        calendarAccentColor = if (calendarAlarm != taskCard) calendarAlarm else accent,
        dialogBgColor = taskCard.copy(alpha = 0.98f),
        dialogBorderColor = accent.copy(alpha = 0.8f),
        dialogGlowColor = Color.Transparent,
        switchTrackColor = accent.copy(alpha = 0.4f),
        switchThumbColor = accent,
        buttonBgColor = accent,
        buttonTextColor = getContrastTextColor(accent),
        outerFrameBorderColor = accent.copy(alpha = 0.4f),
        headerBorderColor = accent.copy(alpha = 0.5f),
        headerGlowColor = Color.Transparent,
        isCyberpunk = false,
        isMonochrome = false,
        isCustom = true
    )
}

val LocalAppTheme = staticCompositionLocalOf { MonochromePalette }

object AppTheme {
    val current: AppThemePalette
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTheme.current
}

@Composable
fun AppThemeWrapper(
    settings: AppSettingsState,
    content: @Composable () -> Unit
) {
    val palette = when (settings.themeMode) {
        AppThemeMode.MONOCHROME -> MonochromePalette
        AppThemeMode.CYBERPUNK -> CyberpunkPalette
        AppThemeMode.CUSTOM -> buildCustomPalette(settings)
    }

    CompositionLocalProvider(LocalAppTheme provides palette) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = palette.accentColor,
                onPrimary = getContrastTextColor(palette.accentColor),
                background = palette.backgroundColor,
                onBackground = palette.primaryTextColor,
                surface = palette.surfaceColor,
                onSurface = palette.primaryTextColor,
                error = DangerRed,
                onError = Color.White
            ),
            typography = Typography,
            content = content
        )
    }
}
