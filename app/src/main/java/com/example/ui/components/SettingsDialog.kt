package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppSettingsState
import com.example.data.AppThemeMode
import com.example.data.BackgroundThemeType
import com.example.ui.theme.AppTheme
import com.example.ui.theme.getContrastTextColor
import com.example.util.PermissionHelper
import java.util.Locale

@Composable
fun SettingsDialog(
    settings: AppSettingsState,
    onDismiss: () -> Unit,
    onSelectThemeMode: (AppThemeMode) -> Unit,
    onSaveCustomTheme: (
        bgColor: Long,
        taskColor: Long,
        normalAlarmColor: Long,
        habitAlarmColor: Long,
        calendarAlarmColor: Long,
        accentColor: Long,
        transparency: Float,
        cornerRadius: Float,
        borderWidth: Float
    ) -> Unit,
    onSelectBackgroundType: (BackgroundThemeType, String?) -> Unit,
    onSetMorningReminder: (Boolean, Int, Int) -> Unit,
    onSetEveningReminder: (Boolean, Int, Int) -> Unit,
    onSendTestNotification: () -> Unit,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onOpenAlarmPermissions: () -> Unit = {}
) {
    val theme = AppTheme.current
    val scrollState = rememberScrollState()

    var showThemeStudio by remember { mutableStateOf(false) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingCropUri = uri
        }
    }

    var morningHour by remember(settings.morningReminderHour) { mutableIntStateOf(settings.morningReminderHour) }
    var morningMinute by remember(settings.morningReminderMinute) { mutableIntStateOf(settings.morningReminderMinute) }
    var morningEnabled by remember(settings.morningReminderEnabled) { mutableStateOf(settings.morningReminderEnabled) }

    var eveningHour by remember(settings.eveningReminderHour) { mutableIntStateOf(settings.eveningReminderHour) }
    var eveningMinute by remember(settings.eveningReminderMinute) { mutableIntStateOf(settings.eveningReminderMinute) }
    var eveningEnabled by remember(settings.eveningReminderEnabled) { mutableStateOf(settings.eveningReminderEnabled) }

    if (showThemeStudio) {
        ThemeStudioDialog(
            settings = settings,
            onDismiss = { showThemeStudio = false },
            onSaveCustomTheme = { bg, task, norm, hab, cal, acc, alpha, radius, borderW ->
                onSaveCustomTheme(bg, task, norm, hab, cal, acc, alpha, radius, borderW)
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .frostedCyanStyle(
                    cornerRadius = 20.dp,
                    borderWidth = theme.cardBorderWidth,
                    backgroundColor = theme.dialogBgColor,
                    borderColor = theme.dialogBorderColor,
                    glowColor = theme.dialogGlowColor,
                    glowRadius = if (theme.isMonochrome) 0.dp else 14.dp
                )
                .padding(18.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Settings",
                            tint = theme.accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "APP SETTINGS & THEMES",
                            color = theme.accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close settings",
                            tint = theme.secondaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = if (theme.isMonochrome) Color(0xFF2C2C2C) else theme.cardBorderColor.copy(alpha = 0.25f)
                )

                // SECTION 1: THEMES (Monochrome, Cyberpunk, Custom)
                Text(
                    text = "THEMES & COLOR PROFILES",
                    color = theme.accentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Select a visual profile. Monochrome is modern dark mode, Cyberpunk features neon glow, or craft your custom palette in Theme Studio:",
                    color = theme.secondaryTextColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                // Option 1: Monochrome Theme (Default)
                ThemeOptionRow(
                    title = "Monochrome Dark (Default)",
                    subtitle = "Pure Black (#000000) canvas with charcoal cards, white text & sharp light gray borders",
                    isSelected = settings.themeMode == AppThemeMode.MONOCHROME,
                    icon = Icons.Default.DarkMode,
                    onClick = {
                        onSelectThemeMode(AppThemeMode.MONOCHROME)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: Cyberpunk Theme
                ThemeOptionRow(
                    title = "Cyberpunk Neon",
                    subtitle = "High-contrast space navy canvas with frosted neon cyan borders & futuristic cyan glow",
                    isSelected = settings.themeMode == AppThemeMode.CYBERPUNK,
                    icon = Icons.Default.Wallpaper,
                    onClick = {
                        onSelectThemeMode(AppThemeMode.CYBERPUNK)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 3: Custom Theme Studio
                val customCardBg = if (settings.themeMode == AppThemeMode.CUSTOM) {
                    theme.accentColor.copy(alpha = if (theme.isMonochrome) 0.2f else 0.18f)
                } else {
                    if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                }
                val customCardBorder = if (settings.themeMode == AppThemeMode.CUSTOM) {
                    theme.accentColor
                } else {
                    if (theme.isMonochrome) Color(0xFF383838) else theme.cardBorderColor.copy(alpha = 0.5f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 14.dp,
                            borderWidth = 1.dp,
                            backgroundColor = customCardBg,
                            borderColor = customCardBorder
                        )
                        .clickable {
                            showThemeStudio = true
                        }
                        .padding(12.dp)
                        .testTag("open_custom_theme_studio_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Custom Theme",
                            tint = theme.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Custom Theme Studio",
                                    color = theme.primaryTextColor,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (settings.themeMode == AppThemeMode.CUSTOM) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(theme.accentColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            color = theme.accentColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Customize solid background, card tints, opacity sliders & corner rounding",
                                color = if (settings.themeMode == AppThemeMode.CUSTOM) theme.accentColor else theme.secondaryTextColor,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(theme.accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, theme.accentColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "STUDIO ➔",
                                color = theme.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = if (theme.isMonochrome) Color(0xFF2C2C2C) else theme.cardBorderColor.copy(alpha = 0.25f)
                )

                // SECTION 2: WALLPAPER & PHOTO LAYER (For Cyberpunk and custom photo modes)
                Text(
                    text = "WALLPAPER & BACKGROUND LAYER",
                    color = theme.accentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Configure wallpaper patterns or frame a custom background photo from your device:",
                    color = theme.secondaryTextColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                // Option 1: Initial Dark Blue
                ThemeOptionRow(
                    title = "Initial Dark Blue Wallpaper",
                    subtitle = "Default minimalist space navy dark background",
                    isSelected = settings.backgroundType == BackgroundThemeType.DARK_BLUE,
                    icon = Icons.Default.DarkMode,
                    onClick = {
                        onSelectBackgroundType(BackgroundThemeType.DARK_BLUE, null)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: Cyber Neon Grid
                ThemeOptionRow(
                    title = "Cyber Neon Grid Wallpaper",
                    subtitle = "Subtle futuristic glowing cyan geometric grid",
                    isSelected = settings.backgroundType == BackgroundThemeType.CYAN_GRID,
                    icon = Icons.Default.Wallpaper,
                    onClick = {
                        onSelectBackgroundType(BackgroundThemeType.CYAN_GRID, null)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 3: Import Background Image
                val importBg = if (settings.backgroundType == BackgroundThemeType.CUSTOM_IMAGE) {
                    theme.accentColor.copy(alpha = if (theme.isMonochrome) 0.2f else 0.15f)
                } else {
                    if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                }
                val importBorder = if (settings.backgroundType == BackgroundThemeType.CUSTOM_IMAGE) {
                    theme.accentColor
                } else {
                    if (theme.isMonochrome) Color(0xFF383838) else theme.cardBorderColor.copy(alpha = 0.5f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 14.dp,
                            borderWidth = 1.dp,
                            backgroundColor = importBg,
                            borderColor = importBorder
                        )
                        .clickable {
                            photoPickerLauncher.launch("image/*")
                        }
                        .padding(12.dp)
                        .testTag("import_background_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Import image",
                            tint = theme.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Frame Custom Gallery Photo",
                                color = theme.primaryTextColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (settings.customImageUri != null)
                                    "Custom image active (tap to re-frame)" else "Select and crop any photo from device gallery",
                                color = if (settings.backgroundType == BackgroundThemeType.CUSTOM_IMAGE)
                                    theme.accentColor else theme.secondaryTextColor,
                                fontSize = 11.sp
                            )
                        }

                        if (settings.backgroundType == BackgroundThemeType.CUSTOM_IMAGE) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = theme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    thickness = 1.dp,
                    color = if (theme.isMonochrome) Color(0xFF2C2C2C) else theme.cardBorderColor.copy(alpha = 0.25f)
                )

                // SECTION 2: DAILY NOTIFICATIONS & SCHEDULES
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DAILY REMINDERS",
                        color = theme.accentColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (!hasNotificationPermission) {
                        Text(
                            text = "⚠️ Needs Permission",
                            color = if (theme.isMonochrome) theme.accentColor else Color(0xFFFFAB00),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Tap on time to open slate input and change morning/evening schedules:",
                    color = theme.secondaryTextColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                // Morning Reminder Row
                DailyReminderConfigRow(
                    title = "🌅 Morning Briefing",
                    dialogTitle = "SET MORNING REMINDER",
                    hour = morningHour,
                    minute = morningMinute,
                    isEnabled = morningEnabled,
                    onToggle = { enabled ->
                        morningEnabled = enabled
                        onSetMorningReminder(enabled, morningHour, morningMinute)
                    },
                    onChangeTime = { h, m ->
                        morningHour = h
                        morningMinute = m
                        onSetMorningReminder(morningEnabled, h, m)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Evening Reminder Row
                DailyReminderConfigRow(
                    title = "🌙 Evening Check-in",
                    dialogTitle = "SET EVENING REMINDER",
                    hour = eveningHour,
                    minute = eveningMinute,
                    isEnabled = eveningEnabled,
                    onToggle = { enabled ->
                        eveningEnabled = enabled
                        onSetEveningReminder(enabled, eveningHour, eveningMinute)
                    },
                    onChangeTime = { h, m ->
                        eveningHour = h
                        eveningMinute = m
                        onSetEveningReminder(eveningEnabled, h, m)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    thickness = 1.dp,
                    color = if (theme.isMonochrome) Color(0xFF2C2C2C) else theme.cardBorderColor.copy(alpha = 0.25f)
                )

                // SECTION 3: NOTIFICATION SYSTEM & LOCK SCREEN SETUP
                Text(
                    text = "NOTIFICATION SYSTEM",
                    color = theme.accentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (!hasNotificationPermission) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRequestNotificationPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (theme.isMonochrome) Color.White else Color(0xFFFFAB00),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "GRANT NOTIFICATION PERMISSION",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lock Screen & Battery Optimization Permissions Button
                val permCardBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                val permCardBorder = if (theme.isMonochrome) Color(0xFF383838) else theme.cardBorderColor.copy(alpha = 0.5f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(permCardBg, RoundedCornerShape(14.dp))
                        .border(1.dp, permCardBorder, RoundedCornerShape(14.dp))
                        .clickable { onOpenAlarmPermissions() }
                        .padding(12.dp)
                        .testTag("settings_open_alarm_permissions")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = theme.accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LOCK SCREEN & BATTERY SETUP",
                                color = theme.primaryTextColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configure overlay & disable battery optimization",
                                color = theme.secondaryTextColor,
                                fontSize = 10.5.sp
                            )
                        }
                        Text(
                            text = "CHECK ➔",
                            color = theme.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onSendTestNotification,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("send_test_notification_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accentColor,
                        contentColor = getContrastTextColor(theme.accentColor)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Test",
                        tint = getContrastTextColor(theme.accentColor),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEST NOTIFICATION NOW",
                        color = getContrastTextColor(theme.accentColor),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Helpful Info Tip
                val tipBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                val tipBorder = if (theme.isMonochrome) Color(0xFF383838) else theme.cardBorderColor.copy(alpha = 0.35f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 14.dp,
                            borderWidth = 1.dp,
                            backgroundColor = tipBg,
                            borderColor = tipBorder
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💡 Tip: Long press any task on your list to set exact individual reminders or multi-select items.",
                        color = theme.secondaryTextColor,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Background Crop & Position Modal
        pendingCropUri?.let { uri ->
            ImageCropFitModal(
                sourceUri = uri,
                title = "FRAME APP WALLPAPER",
                targetAspect = 9f / 16f,
                storagePrefix = "app_bg",
                onDismiss = { pendingCropUri = null },
                onCropCompleted = { croppedUri ->
                    onSelectBackgroundType(BackgroundThemeType.CUSTOM_IMAGE, croppedUri.toString())
                    pendingCropUri = null
                }
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val theme = AppTheme.current
    val rowBg = if (isSelected) {
        theme.accentColor.copy(alpha = if (theme.isMonochrome) 0.2f else 0.15f)
    } else {
        if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
    }
    val rowBorder = if (isSelected) {
        theme.accentColor
    } else {
        if (theme.isMonochrome) Color(0xFF383838) else theme.cardBorderColor.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .frostedCyanStyle(
                cornerRadius = 14.dp,
                borderWidth = 1.dp,
                backgroundColor = rowBg,
                borderColor = rowBorder
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) theme.accentColor else theme.secondaryTextColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = theme.primaryTextColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = if (isSelected) theme.accentColor else theme.secondaryTextColor,
                    fontSize = 11.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = theme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DailyReminderConfigRow(
    title: String,
    dialogTitle: String,
    hour: Int,
    minute: Int,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onChangeTime: (Int, Int) -> Unit
) {
    val theme = AppTheme.current
    var showTimeDialog by remember { mutableStateOf(false) }

    if (showTimeDialog) {
        BlackSlateTimeDialog(
            title = dialogTitle,
            currentHour = hour,
            currentMinute = minute,
            onDismiss = { showTimeDialog = false },
            onConfirm = { h, m ->
                onChangeTime(h, m)
                showTimeDialog = false
            }
        )
    }

    val configBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
    val configBorder = if (theme.isMonochrome) Color(0xFF383838) else theme.cardBorderColor.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .frostedCyanStyle(
                cornerRadius = 14.dp,
                borderWidth = 1.dp,
                backgroundColor = configBg,
                borderColor = configBorder
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = theme.primaryTextColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Time chip (clickable to open Black Slate Time Dialog)
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                val amPm = if (hour >= 12) "PM" else "AM"
                val timeStr = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)

                val timeChipBg = if (theme.isMonochrome) Color(0xFF121212) else theme.cardBgColor
                Box(
                    modifier = Modifier
                        .background(timeChipBg, RoundedCornerShape(12.dp))
                        .border(1.dp, theme.accentColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                        .clickable { showTimeDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = timeStr,
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = getContrastTextColor(theme.accentColor),
                        checkedTrackColor = theme.accentColor,
                        uncheckedThumbColor = theme.secondaryTextColor,
                        uncheckedTrackColor = if (theme.isMonochrome) Color(0xFF333333) else theme.surfaceColor
                    )
                )
            }
        }
    }
}
