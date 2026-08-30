package com.example.ui.alarm

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.alarm.AudioToneGenerator
import com.example.data.AlarmItem
import com.example.data.AlarmType
import com.example.ui.components.BlackSlateTimeDialog
import com.example.ui.components.BlackSlateTimeInputs
import com.example.ui.components.ImageCropFitModal
import com.example.ui.components.frostedCyanStyle
import com.example.ui.theme.AppTheme
import com.example.ui.theme.getContrastTextColor
import com.example.util.ImageStorageHelper
import com.example.util.ImportedRingtone
import com.example.util.RingtoneManagerHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmDialog(
    initialAlarm: AlarmItem? = null,
    alarmType: AlarmType = initialAlarm?.type ?: AlarmType.NORMAL,
    onDismiss: () -> Unit,
    onSave: (AlarmItem) -> Unit
) {
    val context = LocalContext.current
    val theme = AppTheme.current
    val isEditing = initialAlarm != null

    // Time states
    var hour by remember { mutableIntStateOf(initialAlarm?.hour ?: 8) }
    var minute by remember { mutableIntStateOf(initialAlarm?.minute ?: 0) }
    var repeatDaysOfWeek by remember { mutableIntStateOf(initialAlarm?.repeatDaysOfWeek ?: 127) }

    // Label & Common
    var title by remember {
        mutableStateOf(
            initialAlarm?.title ?: when (alarmType) {
                AlarmType.HABIT -> "Hydration Habit"
                AlarmType.CALENDAR -> "Special Event"
                AlarmType.NORMAL -> "Wake Up"
            }
        )
    }
    var ringtoneName by remember { mutableStateOf(initialAlarm?.ringtoneName ?: "Default Alarm Sound") }
    var ringtoneUri by remember { mutableStateOf(initialAlarm?.ringtoneUri) }
    var vibrate by remember { mutableStateOf(initialAlarm?.vibrate ?: true) }
    var snoozeEnabled by remember { mutableStateOf(initialAlarm?.snoozeEnabled ?: true) }
    var snoozeMinutes by remember { mutableIntStateOf(initialAlarm?.snoozeDurationMinutes ?: 5) }
    var customBackgroundUri by remember { mutableStateOf(initialAlarm?.customBackgroundUri) }
    var customBackgroundPreset by remember { mutableStateOf(initialAlarm?.customBackgroundPreset ?: "DEFAULT") }

    // Habit specific states
    var habitIntervalMinutes by remember { mutableIntStateOf(initialAlarm?.habitIntervalMinutes ?: 45) }
    var habitDaysOfWeek by remember { mutableIntStateOf(initialAlarm?.habitDaysOfWeek ?: 127) }
    var habitWakeHour by remember { mutableIntStateOf(initialAlarm?.habitWakeHour ?: 7) }
    var habitWakeMinute by remember { mutableIntStateOf(initialAlarm?.habitWakeMinute ?: 0) }
    var habitSleepHour by remember { mutableIntStateOf(initialAlarm?.habitSleepHour ?: 23) }
    var habitSleepMinute by remember { mutableIntStateOf(initialAlarm?.habitSleepMinute ?: 0) }
    var habitMessage by remember {
        mutableStateOf(initialAlarm?.habitMessage ?: "Drink water & take a posture stretch!")
    }

    // Calendar specific states
    val calNow = Calendar.getInstance()
    var calendarYear by remember { mutableIntStateOf(initialAlarm?.calendarYear ?: calNow.get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableIntStateOf(initialAlarm?.calendarMonth ?: (calNow.get(Calendar.MONTH) + 1)) }
    var calendarDay by remember { mutableIntStateOf(initialAlarm?.calendarDay ?: calNow.get(Calendar.DAY_OF_MONTH)) }
    var calendarRepeatAnnually by remember { mutableStateOf(initialAlarm?.calendarRepeatAnnually ?: true) }

    // Image Picker & Crop State
    var pendingCropImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingCropImageUri = uri
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AudioToneGenerator.stopPreview()
        }
    }

    // Time calculation string
    val timeToAlarmStr = remember(hour, minute, alarmType, repeatDaysOfWeek, calendarYear, calendarMonth, calendarDay) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (alarmType == AlarmType.CALENDAR) {
                set(Calendar.YEAR, calendarYear)
                set(Calendar.MONTH, calendarMonth - 1)
                set(Calendar.DAY_OF_MONTH, calendarDay)
            } else if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val diffMs = target.timeInMillis - now.timeInMillis
        if (diffMs <= 0) {
            "Time has passed"
        } else {
            val totalMins = diffMs / (60 * 1000)
            val days = totalMins / (24 * 60)
            val hours = (totalMins % (24 * 60)) / 60
            val mins = totalMins % 60
            when {
                days > 0 -> "Alarm in $days days, $hours hrs $mins mins"
                hours > 0 -> "Alarm in $hours hours $mins minutes"
                else -> "Alarm in $mins minutes"
            }
        }
    }

    val itemCardBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .frostedCyanStyle(
                    cornerRadius = 20.dp,
                    borderWidth = theme.cardBorderWidth,
                    backgroundColor = theme.dialogBgColor,
                    borderColor = theme.dialogBorderColor,
                    glowColor = theme.dialogGlowColor,
                    glowRadius = if (theme.isMonochrome) 0.dp else 14.dp
                ),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // TOP ACTION BAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = theme.secondaryTextColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEditing) "EDIT ALARM" else "ADD ALARM",
                            color = theme.primaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = when (alarmType) {
                                AlarmType.NORMAL -> timeToAlarmStr
                                AlarmType.HABIT -> "Interval: Every $habitIntervalMinutes mins"
                                AlarmType.CALENDAR -> "Date: ${getMonthName(calendarMonth)} $calendarDay, $calendarYear"
                            },
                            color = theme.accentColor,
                            fontSize = 11.5.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            val newAlarm = (initialAlarm ?: AlarmItem()).copy(
                                type = alarmType,
                                title = title.ifBlank { "Alarm" },
                                isEnabled = true,
                                hour = hour,
                                minute = minute,
                                repeatDaysOfWeek = repeatDaysOfWeek,
                                ringtoneName = ringtoneName,
                                ringtoneUri = ringtoneUri,
                                vibrate = vibrate,
                                snoozeEnabled = snoozeEnabled,
                                snoozeDurationMinutes = snoozeMinutes,
                                customBackgroundUri = customBackgroundUri,
                                customBackgroundPreset = customBackgroundPreset,
                                habitIntervalMinutes = habitIntervalMinutes,
                                habitDaysOfWeek = habitDaysOfWeek,
                                habitWakeHour = habitWakeHour,
                                habitWakeMinute = habitWakeMinute,
                                habitSleepHour = habitSleepHour,
                                habitSleepMinute = habitSleepMinute,
                                habitMessage = habitMessage,
                                calendarYear = calendarYear,
                                calendarMonth = calendarMonth,
                                calendarDay = calendarDay,
                                calendarRepeatAnnually = calendarRepeatAnnually
                            )
                            onSave(newAlarm)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(theme.accentColor, RoundedCornerShape(12.dp))
                            .testTag("save_alarm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = getContrastTextColor(theme.accentColor),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TIME SELECTOR (Only for NORMAL and CALENDAR alarms, NOT for habit alarms)
                if (alarmType != AlarmType.HABIT) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .frostedCyanStyle(
                                cornerRadius = 16.dp,
                                borderWidth = 1.dp,
                                backgroundColor = itemCardBg,
                                borderColor = theme.cardBorderColor
                            )
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TIME (CLICK BOX TO TYPE)",
                                color = theme.subtleTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            BlackSlateTimeInputs(
                                initialHour24 = hour,
                                initialMinute = minute,
                                onTimeChanged = { h24, m ->
                                    hour = h24
                                    minute = m
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // LABEL INPUT
                Text(
                    text = "LABEL / TITLE",
                    color = theme.accentColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 14.dp,
                            borderWidth = 1.dp,
                            backgroundColor = itemCardBg,
                            borderColor = theme.cardBorderColor.copy(alpha = 0.5f)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(
                            color = theme.primaryTextColor,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(theme.accentColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alarm_title_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // NORMAL ALARM WEEKLY SCHEDULE SECTION
                if (alarmType == AlarmType.NORMAL) {
                    NormalAlarmWeeklyScheduleSection(
                        repeatDaysOfWeek = repeatDaysOfWeek,
                        onDaysMaskChange = { repeatDaysOfWeek = it }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // HABIT SPECIFIC CONFIGURATION SECTION
                if (alarmType == AlarmType.HABIT) {
                    HabitConfigurationSection(
                        intervalMinutes = habitIntervalMinutes,
                        onIntervalChange = { habitIntervalMinutes = it },
                        daysOfWeekMask = habitDaysOfWeek,
                        onDaysMaskChange = { habitDaysOfWeek = it },
                        wakeHour = habitWakeHour,
                        wakeMinute = habitWakeMinute,
                        onWakeTimeChange = { h, m -> habitWakeHour = h; habitWakeMinute = m },
                        sleepHour = habitSleepHour,
                        sleepMinute = habitSleepMinute,
                        onSleepTimeChange = { h, m -> habitSleepHour = h; habitSleepMinute = m },
                        habitMessage = habitMessage,
                        onMessageChange = { habitMessage = it }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // CALENDAR SPECIFIC CONFIGURATION SECTION
                if (alarmType == AlarmType.CALENDAR) {
                    CalendarConfigurationSection(
                        year = calendarYear,
                        month = calendarMonth,
                        day = calendarDay,
                        onDateChange = { y, m, d -> calendarYear = y; calendarMonth = m; calendarDay = d },
                        repeatAnnually = calendarRepeatAnnually,
                        onRepeatChange = { calendarRepeatAnnually = it }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // RINGTONE SELECTOR
                RingtoneSelectionSection(
                    selectedRingtone = ringtoneName,
                    customRingtoneUri = ringtoneUri,
                    onSelect = { name, uriStr ->
                        ringtoneName = name
                        ringtoneUri = uriStr
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SNOOZE CONTROLS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 14.dp,
                            borderWidth = 1.dp,
                            backgroundColor = itemCardBg,
                            borderColor = theme.cardBorderColor.copy(alpha = 0.4f)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = "Snooze",
                            tint = theme.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Snooze Button & Failsafe",
                                color = theme.primaryTextColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (snoozeEnabled) "Auto-snoozes in 2m (Max 3 times)" else "Disabled",
                                color = theme.secondaryTextColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = snoozeEnabled,
                        onCheckedChange = { snoozeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = getContrastTextColor(theme.accentColor),
                            checkedTrackColor = theme.accentColor,
                            uncheckedThumbColor = theme.subtleTextColor,
                            uncheckedTrackColor = if (theme.isMonochrome) Color(0xFF333333) else Color(0xFF162544)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // VIBRATION TOGGLE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 14.dp,
                            borderWidth = 1.dp,
                            backgroundColor = itemCardBg,
                            borderColor = theme.cardBorderColor.copy(alpha = 0.4f)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Vibration",
                            tint = theme.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Vibration Pattern",
                                color = theme.primaryTextColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (vibrate) "Rhythm pulsing" else "Silent / Audio only",
                                color = theme.secondaryTextColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = getContrastTextColor(theme.accentColor),
                            checkedTrackColor = theme.accentColor,
                            uncheckedThumbColor = theme.subtleTextColor,
                            uncheckedTrackColor = if (theme.isMonochrome) Color(0xFF333333) else Color(0xFF162544)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CUSTOM WALLPAPER / POSTER FOR THIS ALARM
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 14.dp,
                            borderWidth = 1.dp,
                            backgroundColor = if (customBackgroundUri != null) theme.accentColor.copy(alpha = 0.15f) else itemCardBg,
                            borderColor = if (customBackgroundUri != null) theme.accentColor else theme.cardBorderColor.copy(alpha = 0.5f)
                        )
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Alarm Wallpaper",
                                tint = theme.accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (customBackgroundUri != null) "CUSTOM ALARM POSTER SET" else "+ SET ALARM POSTER IMAGE",
                                    color = if (customBackgroundUri != null) theme.accentColor else theme.primaryTextColor,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (customBackgroundUri != null) "Image active for alert screen" else "Shows whole cropped image when alarm rings",
                                    color = theme.secondaryTextColor,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        if (customBackgroundUri != null) {
                            IconButton(
                                onClick = { customBackgroundUri = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Wallpaper",
                                    tint = theme.secondaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Image Crop Modal
        pendingCropImageUri?.let { uri ->
            ImageCropFitModal(
                sourceUri = uri,
                title = "FRAME ALARM POSTER",
                targetAspect = 9f / 16f,
                storagePrefix = "alarm_${initialAlarm?.id ?: System.currentTimeMillis()}",
                onDismiss = { pendingCropImageUri = null },
                onCropCompleted = { croppedUri ->
                    customBackgroundUri = croppedUri.toString()
                    pendingCropImageUri = null
                }
            )
        }
    }
}

@Composable
fun RingtoneSelectionSection(
    selectedRingtone: String,
    customRingtoneUri: String?,
    onSelect: (name: String, uriStr: String?) -> Unit
) {
    val context = LocalContext.current
    val theme = AppTheme.current
    val coroutineScope = rememberCoroutineScope()
    var isPlayingPreview by remember { mutableStateOf(false) }

    var importedTones by remember {
        mutableStateOf(RingtoneManagerHelper.getImportedRingtones(context))
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val copiedAudio = ImageStorageHelper.copyAudioToInternalStorage(context, uri)
                if (copiedAudio != null) {
                    val (displayName, savedUriStr) = copiedAudio
                    RingtoneManagerHelper.addImportedRingtone(context, displayName, savedUriStr)
                    importedTones = RingtoneManagerHelper.getImportedRingtones(context)
                    onSelect(displayName, savedUriStr)
                    AudioToneGenerator.previewTone(context, displayName, savedUriStr)
                    isPlayingPreview = true
                }
            }
        }
    }

    DisposableEffect(selectedRingtone, customRingtoneUri) {
        onDispose {
            AudioToneGenerator.stopPreview()
            isPlayingPreview = false
        }
    }

    val containerBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
    val unselectedItemBg = if (theme.isMonochrome) Color(0xFF282828) else Color(0xFF0F1E3D)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .frostedCyanStyle(
                cornerRadius = 14.dp,
                borderWidth = 1.dp,
                backgroundColor = containerBg,
                borderColor = theme.cardBorderColor.copy(alpha = 0.5f)
            )
            .padding(14.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Ringtone",
                    tint = theme.accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ringtone Sound",
                        color = theme.primaryTextColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedRingtone,
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Test / Preview Audio Button
            Box(
                modifier = Modifier
                    .background(
                        if (isPlayingPreview) theme.accentColor else theme.accentColor.copy(alpha = 0.2f),
                        RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, theme.accentColor, RoundedCornerShape(14.dp))
                    .clickable {
                        if (isPlayingPreview) {
                            AudioToneGenerator.stopPreview()
                            isPlayingPreview = false
                        } else {
                            AudioToneGenerator.previewTone(context, selectedRingtone, customRingtoneUri)
                            isPlayingPreview = true
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = "Preview Tone",
                        tint = if (isPlayingPreview) getContrastTextColor(theme.accentColor) else theme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isPlayingPreview) "STOP" else "TEST",
                        color = if (isPlayingPreview) getContrastTextColor(theme.accentColor) else theme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Default System Tone Option
        val isDefaultSelected = customRingtoneUri == null
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDefaultSelected) theme.accentColor.copy(alpha = 0.18f) else unselectedItemBg,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    if (isDefaultSelected) theme.accentColor else theme.cardBorderColor.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    onSelect("Default Alarm Sound", null)
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = if (isDefaultSelected) theme.accentColor else theme.secondaryTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Default System Alarm Tone",
                        color = if (isDefaultSelected) theme.accentColor else theme.primaryTextColor,
                        fontSize = 12.5.sp,
                        fontWeight = if (isDefaultSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
                if (isDefaultSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = theme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Imported Ringtones List
        Text(
            text = "IMPORTED RINGTONES (${importedTones.size}):",
            color = theme.secondaryTextColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (importedTones.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(unselectedItemBg.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No custom ringtones imported yet.\nImport your audio files below to save into cache.",
                    color = theme.secondaryTextColor,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                importedTones.forEach { ringtone ->
                    val isToneSelected = customRingtoneUri == ringtone.uriString
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isToneSelected) theme.accentColor.copy(alpha = 0.2f) else unselectedItemBg,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isToneSelected) theme.accentColor else theme.cardBorderColor.copy(alpha = 0.35f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelect(ringtone.name, ringtone.uriString)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = if (isToneSelected) theme.accentColor else theme.secondaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ringtone.name,
                                    color = if (isToneSelected) theme.accentColor else theme.primaryTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = if (isToneSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isToneSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = theme.accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                IconButton(
                                    onClick = {
                                        RingtoneManagerHelper.deleteImportedRingtone(context, ringtone.id)
                                        importedTones = RingtoneManagerHelper.getImportedRingtones(context)
                                        if (customRingtoneUri == ringtone.uriString) {
                                            onSelect("Default Alarm Sound", null)
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Ringtone",
                                        tint = theme.secondaryTextColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Direct Audio Import Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(unselectedItemBg, RoundedCornerShape(12.dp))
                .border(1.dp, theme.accentColor, RoundedCornerShape(12.dp))
                .clickable {
                    audioPickerLauncher.launch("audio/*")
                }
                .padding(10.dp)
                .testTag("import_audio_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Import Audio",
                    tint = theme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+ IMPORT RINGTONE FROM DEVICE",
                    color = theme.accentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun HabitConfigurationSection(
    intervalMinutes: Int,
    onIntervalChange: (Int) -> Unit,
    daysOfWeekMask: Int,
    onDaysMaskChange: (Int) -> Unit,
    wakeHour: Int,
    wakeMinute: Int,
    onWakeTimeChange: (Int, Int) -> Unit,
    sleepHour: Int,
    sleepMinute: Int,
    onSleepTimeChange: (Int, Int) -> Unit,
    habitMessage: String,
    onMessageChange: (String) -> Unit
) {
    val theme = AppTheme.current
    val sectionAccent = if (theme.isMonochrome) theme.accentColor else Color(0xFFFFD54F)
    val containerBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
    val chipUnselectedBg = if (theme.isMonochrome) Color(0xFF282828) else Color(0xFF0F1E3D)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .frostedCyanStyle(
                cornerRadius = 14.dp,
                borderWidth = 1.dp,
                backgroundColor = containerBg,
                borderColor = sectionAccent.copy(alpha = 0.7f),
                glowColor = if (theme.isMonochrome) Color.Transparent else sectionAccent.copy(alpha = 0.2f)
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "Habit interval",
                tint = sectionAccent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "HABIT INTERVAL SETTINGS",
                color = sectionAccent,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interval chips
        Text(
            text = "Interval between rings:",
            color = theme.secondaryTextColor,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        var showCustomMinutesDialog by remember { mutableStateOf(false) }

        if (showCustomMinutesDialog) {
            CustomIntervalMinutesDialog(
                currentMinutes = intervalMinutes,
                onDismiss = { showCustomMinutesDialog = false },
                onConfirm = { customMins ->
                    onIntervalChange(customMins)
                    showCustomMinutesDialog = false
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(15, 30, 45, 60, 90, 120).forEach { mins ->
                val isSelected = intervalMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) sectionAccent.copy(alpha = 0.25f) else chipUnselectedBg,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) sectionAccent else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onIntervalChange(mins) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mins}m",
                        color = if (isSelected) sectionAccent else theme.primaryTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Set Custom Time Button (in minutes only)
        val isCustomSelected = intervalMinutes !in listOf(15, 30, 45, 60, 90, 120)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isCustomSelected) sectionAccent.copy(alpha = 0.25f) else chipUnselectedBg,
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isCustomSelected) sectionAccent else sectionAccent.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { showCustomMinutesDialog = true }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = if (isCustomSelected) sectionAccent else theme.primaryTextColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCustomSelected) "SET CUSTOM TIME: ${intervalMinutes}m (SELECTED)" else "SET CUSTOM TIME (MINUTES ONLY)",
                    color = if (isCustomSelected) sectionAccent else theme.primaryTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sleep / Wake Boundaries
        Text(
            text = "Active Wake Window (Won't ring during sleep):",
            color = theme.secondaryTextColor,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        var showWakeTimeDialog by remember { mutableStateOf(false) }
        var showSleepTimeDialog by remember { mutableStateOf(false) }

        if (showWakeTimeDialog) {
            BlackSlateTimeDialog(
                title = "SET WAKE TIME",
                currentHour = wakeHour,
                currentMinute = wakeMinute,
                onDismiss = { showWakeTimeDialog = false },
                onConfirm = { h, m ->
                    onWakeTimeChange(h, m)
                    showWakeTimeDialog = false
                }
            )
        }

        if (showSleepTimeDialog) {
            BlackSlateTimeDialog(
                title = "SET SLEEP TIME",
                currentHour = sleepHour,
                currentMinute = sleepMinute,
                onDismiss = { showSleepTimeDialog = false },
                onConfirm = { h, m ->
                    onSleepTimeChange(h, m)
                    showSleepTimeDialog = false
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(chipUnselectedBg, RoundedCornerShape(12.dp))
                    .border(1.dp, sectionAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { showWakeTimeDialog = true }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = sectionAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WAKE UP", color = theme.secondaryTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(formatHourMin(wakeHour, wakeMinute), color = theme.primaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(chipUnselectedBg, RoundedCornerShape(12.dp))
                    .border(1.dp, theme.cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { showSleepTimeDialog = true }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Nightlight, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("BEDTIME", color = theme.secondaryTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(formatHourMin(sleepHour, sleepMinute), color = theme.primaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Days selector
        Text(
            text = "Active Days:",
            color = theme.secondaryTextColor,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val dayNames = listOf("M", "T", "W", "Th", "F", "Sa", "Su")
            dayNames.forEachIndexed { index, name ->
                val maskBit = 1 shl index
                val isSelected = (daysOfWeekMask and maskBit) != 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) sectionAccent else chipUnselectedBg,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            val newMask = if (isSelected) daysOfWeekMask and maskBit.inv() else daysOfWeekMask or maskBit
                            onDaysMaskChange(newMask)
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) getContrastTextColor(sectionAccent) else theme.primaryTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NormalAlarmWeeklyScheduleSection(
    repeatDaysOfWeek: Int,
    onDaysMaskChange: (Int) -> Unit
) {
    val theme = AppTheme.current
    val containerBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
    val chipUnselectedBg = if (theme.isMonochrome) Color(0xFF282828) else Color(0xFF0F1E3D)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .frostedCyanStyle(
                cornerRadius = 14.dp,
                borderWidth = 1.dp,
                backgroundColor = containerBg,
                borderColor = theme.cardBorderColor.copy(alpha = 0.6f),
                glowColor = theme.cardGlowColor.copy(alpha = 0.15f)
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "Weekly repeat",
                tint = theme.accentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "WEEKLY REPEAT SCHEDULE",
                color = theme.accentColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Every Day" to 127,
                "Weekdays" to 31,
                "Weekends" to 96,
                "Once" to 0
            ).forEach { (label, mask) ->
                val isSelected = repeatDaysOfWeek == mask
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) theme.accentColor.copy(alpha = 0.25f) else chipUnselectedBg,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) theme.accentColor else theme.cardBorderColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onDaysMaskChange(mask) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) theme.accentColor else theme.primaryTextColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 7-day buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val dayNames = listOf("M", "T", "W", "Th", "F", "Sa", "Su")
            dayNames.forEachIndexed { index, name ->
                val maskBit = 1 shl index
                val isSelected = (repeatDaysOfWeek and maskBit) != 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) theme.accentColor else chipUnselectedBg,
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) theme.accentColor else theme.cardBorderColor.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            val newMask = if (isSelected) repeatDaysOfWeek and maskBit.inv() else repeatDaysOfWeek or maskBit
                            onDaysMaskChange(newMask)
                        }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        color = if (isSelected) getContrastTextColor(theme.accentColor) else theme.primaryTextColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarConfigurationSection(
    year: Int,
    month: Int,
    day: Int,
    onDateChange: (Int, Int, Int) -> Unit,
    repeatAnnually: Boolean,
    onRepeatChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val theme = AppTheme.current
    val calAccent = if (theme.isMonochrome) theme.accentColor else Color(0xFFB388FF)
    val containerBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
    val chipUnselectedBg = if (theme.isMonochrome) Color(0xFF282828) else Color(0xFF0F1E3D)

    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
    }
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    val formattedDateStr = dateFormat.format(cal.time)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .frostedCyanStyle(
                cornerRadius = 14.dp,
                borderWidth = 1.dp,
                backgroundColor = containerBg,
                borderColor = calAccent.copy(alpha = 0.7f),
                glowColor = if (theme.isMonochrome) Color.Transparent else calAccent.copy(alpha = 0.2f)
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Date Alarm",
                tint = calAccent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "CALENDAR / DATE ALARM SETTINGS",
                color = calAccent,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Date Card with Big Calendar Picker Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(calAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, calAccent, RoundedCornerShape(12.dp))
                .clickable {
                    val datePicker = android.app.DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            onDateChange(selectedYear, selectedMonth + 1, selectedDay)
                        },
                        year,
                        month - 1,
                        day
                    )
                    datePicker.show()
                }
                .padding(12.dp)
                .testTag("open_calendar_date_picker")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TARGET DATE (TAP TO PICK CALENDAR)",
                        color = calAccent,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDateStr,
                        color = theme.primaryTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(calAccent, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CHOOSE",
                        color = getContrastTextColor(calAccent),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Preset Chips
        Text(
            text = "Quick Presets:",
            color = theme.secondaryTextColor,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Today" to 0,
                "Tomorrow" to 1,
                "+1 Week" to 7,
                "+1 Month" to 30
            ).forEach { (label, offsetDays) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(chipUnselectedBg, RoundedCornerShape(8.dp))
                        .border(0.5.dp, calAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable {
                            val presetCal = Calendar.getInstance().apply {
                                if (offsetDays > 0) add(Calendar.DAY_OF_YEAR, offsetDays)
                            }
                            onDateChange(
                                presetCal.get(Calendar.YEAR),
                                presetCal.get(Calendar.MONTH) + 1,
                                presetCal.get(Calendar.DAY_OF_MONTH)
                            )
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = calAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Repeat Annually Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(chipUnselectedBg, RoundedCornerShape(10.dp))
                .border(0.5.dp, calAccent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Repeat Every Year",
                    color = theme.primaryTextColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (repeatAnnually) "Annual recurring (Birthday, Anniversary)" else "One-time event",
                    color = theme.secondaryTextColor,
                    fontSize = 10.5.sp
                )
            }
            Switch(
                checked = repeatAnnually,
                onCheckedChange = onRepeatChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = getContrastTextColor(calAccent),
                    checkedTrackColor = calAccent,
                    uncheckedThumbColor = theme.subtleTextColor,
                    uncheckedTrackColor = if (theme.isMonochrome) Color(0xFF333333) else Color(0xFF162544)
                )
            )
        }
    }
}

private fun formatHourMin(hour: Int, minute: Int): String {
    val isAm = hour < 12
    val displayH = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val amPm = if (isAm) "AM" else "PM"
    return String.format(Locale.getDefault(), "%02d:%02d %s", displayH, minute, amPm)
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> "Jan"
    }
}

@Composable
fun CustomIntervalMinutesDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val theme = AppTheme.current
    val accent = if (theme.isMonochrome) theme.accentColor else Color(0xFFFFD54F)
    val buttonBg = if (theme.isMonochrome) Color(0xFF282828) else Color(0xFF0F1E3D)
    var textInput by remember { mutableStateOf(currentMinutes.toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .frostedCyanStyle(
                    cornerRadius = 20.dp,
                    borderWidth = theme.cardBorderWidth,
                    backgroundColor = theme.dialogBgColor,
                    borderColor = if (theme.isMonochrome) theme.dialogBorderColor else accent,
                    glowColor = if (theme.isMonochrome) Color.Transparent else accent.copy(alpha = 0.25f),
                    glowRadius = if (theme.isMonochrome) 0.dp else 12.dp
                )
                .padding(20.dp)
                .testTag("custom_interval_minutes_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "SET CUSTOM TIME",
                        color = accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Interval between reminders (minutes only):",
                    color = theme.secondaryTextColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                // Large Counter / Input with Stepper Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // -5 Button
                    Box(
                        modifier = Modifier
                            .background(buttonBg, RoundedCornerShape(10.dp))
                            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable {
                                val cur = textInput.toIntOrNull() ?: 15
                                val newVal = (cur - 5).coerceAtLeast(1)
                                textInput = newVal.toString()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("-5m", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // -1 Button
                    Box(
                        modifier = Modifier
                            .background(buttonBg, RoundedCornerShape(10.dp))
                            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable {
                                val cur = textInput.toIntOrNull() ?: 15
                                val newVal = (cur - 1).coerceAtLeast(1)
                                textInput = newVal.toString()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("-1m", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Big Number Input
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .background(buttonBg, RoundedCornerShape(12.dp))
                            .border(1.dp, accent, RoundedCornerShape(12.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = textInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 4) {
                                    textInput = input
                                }
                            },
                            textStyle = TextStyle(
                                color = theme.primaryTextColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_interval_input")
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // +1 Button
                    Box(
                        modifier = Modifier
                            .background(buttonBg, RoundedCornerShape(10.dp))
                            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable {
                                val cur = textInput.toIntOrNull() ?: 15
                                val newVal = cur + 1
                                textInput = newVal.toString()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("+1m", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // +5 Button
                    Box(
                        modifier = Modifier
                            .background(buttonBg, RoundedCornerShape(10.dp))
                            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable {
                                val cur = textInput.toIntOrNull() ?: 15
                                val newVal = cur + 5
                                textInput = newVal.toString()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("+5m", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Quick test presets
                Text(
                    text = "Quick Presets & Testing:",
                    color = theme.secondaryTextColor,
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 2, 5, 10, 20, 25, 75).forEach { mins ->
                        val isCurr = (textInput.toIntOrNull() ?: -1) == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isCurr) accent.copy(alpha = 0.3f) else buttonBg,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    0.5.dp,
                                    if (isCurr) accent else accent.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { textInput = mins.toString() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                color = if (isCurr) accent else theme.primaryTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val cancelBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(cancelBg, RoundedCornerShape(12.dp))
                            .border(1.dp, theme.cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CANCEL", color = theme.secondaryTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(accent, RoundedCornerShape(12.dp))
                            .clickable {
                                val mins = textInput.toIntOrNull()?.coerceAtLeast(1) ?: 15
                                onConfirm(mins)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SET INTERVAL", color = getContrastTextColor(accent), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
