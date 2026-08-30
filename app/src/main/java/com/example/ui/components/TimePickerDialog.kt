package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.alarm.AudioToneGenerator
import com.example.data.TaskItem
import com.example.ui.theme.AppTheme
import com.example.ui.theme.getContrastTextColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TaskTimePickerDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onConfirmTime: (Long, String?) -> Unit
) {
    val context = LocalContext.current
    val theme = AppTheme.current
    val calendar = remember {
        Calendar.getInstance().apply {
            if (task.reminderTimeMillis != null && task.reminderTimeMillis > System.currentTimeMillis()) {
                timeInMillis = task.reminderTimeMillis
            } else {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
        }
    }

    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var isTomorrow by remember { mutableStateOf(false) }
    var selectedTone by remember { mutableStateOf(task.customToneName ?: "Cyber Pulse") }

    val toneOptions = listOf("Cyber Pulse", "Digital Beep", "Zen Chime", "Neon Warning", "Cosmic Ping", "Default Ping")

    DisposableEffect(Unit) {
        onDispose {
            AudioToneGenerator.stopPreview()
        }
    }

    fun calculateMillis(): Long {
        val cal = Calendar.getInstance()
        if (isTomorrow) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        cal.set(Calendar.HOUR_OF_DAY, selectedHour)
        cal.set(Calendar.MINUTE, selectedMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (!isTomorrow && cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .frostedCyanStyle(
                    cornerRadius = 20.dp,
                    borderWidth = theme.cardBorderWidth,
                    backgroundColor = theme.dialogBgColor,
                    borderColor = theme.dialogBorderColor,
                    glowColor = theme.dialogGlowColor,
                    glowRadius = if (theme.isMonochrome) 0.dp else 14.dp
                )
                .padding(20.dp)
                .testTag("time_picker_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Reminder",
                            tint = theme.accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SET REMINDER",
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
                            contentDescription = "Close",
                            tint = theme.secondaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "\"${task.title}\"",
                    color = theme.primaryTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = theme.cardBorderColor.copy(alpha = 0.35f)
                )

                // Subtitle
                Text(
                    text = "ENTER TIME (CLICK TO TYPE)",
                    color = theme.subtleTextColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Digital Input Row
                BlackSlateTimeInputs(
                    initialHour24 = selectedHour,
                    initialMinute = selectedMinute,
                    onTimeChanged = { h24, m ->
                        selectedHour = h24
                        selectedMinute = m
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Day Selection Pill (Today vs Tomorrow)
                val pillContainerBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .background(pillContainerBg, RoundedCornerShape(20.dp))
                            .border(1.dp, theme.cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DayPill(
                            label = "Today",
                            isSelected = !isTomorrow,
                            onClick = { isTomorrow = false }
                        )
                        DayPill(
                            label = "Tomorrow",
                            isSelected = isTomorrow,
                            onClick = { isTomorrow = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Audio Sound Ping Selector
                val soundContainerBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(soundContainerBg, RoundedCornerShape(12.dp))
                        .border(1.dp, theme.cardBorderColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = theme.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NOTIFICATION SOUND",
                                color = theme.subtleTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        // Test button
                        Box(
                            modifier = Modifier
                                .background(theme.accentColor.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                                .border(0.8.dp, theme.accentColor, RoundedCornerShape(8.dp))
                                .clickable {
                                    AudioToneGenerator.playTaskPing(context, selectedTone)
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Test tone", tint = theme.accentColor, modifier = Modifier.size(13.dp))
                                Text("TEST", color = theme.accentColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val unselectedChipBg = if (theme.isMonochrome) Color(0xFF282828) else theme.cardBgColor

                    // Horizontal tone chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        toneOptions.take(3).forEach { tone ->
                            val isSelected = selectedTone == tone
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) theme.accentColor else unselectedChipBg,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedTone = tone
                                        AudioToneGenerator.playTaskPing(context, tone)
                                    }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tone.substringBefore(" "),
                                    color = if (isSelected) getContrastTextColor(theme.accentColor) else theme.primaryTextColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        toneOptions.drop(3).forEach { tone ->
                            val isSelected = selectedTone == tone
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) theme.accentColor else unselectedChipBg,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedTone = tone
                                        AudioToneGenerator.playTaskPing(context, tone)
                                    }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tone.substringBefore(" "),
                                    color = if (isSelected) getContrastTextColor(theme.accentColor) else theme.primaryTextColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preview scheduled time string
                val previewMillis = calculateMillis()
                val format = SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault())
                Text(
                    text = "Alarm set for: ${format.format(Date(previewMillis))}",
                    color = theme.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val cancelBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(cancelBg, RoundedCornerShape(14.dp))
                            .border(1.dp, theme.cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CANCEL",
                            color = theme.primaryTextColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Button(
                        onClick = {
                            onConfirmTime(calculateMillis(), selectedTone)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("confirm_reminder_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.accentColor,
                            contentColor = getContrastTextColor(theme.accentColor)
                        )
                    ) {
                        Text(
                            text = "SAVE REMINDER",
                            color = getContrastTextColor(theme.accentColor),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = AppTheme.current
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) theme.accentColor else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) getContrastTextColor(theme.accentColor) else theme.primaryTextColor,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Clean Digital Time Input Component
 * Blank slate editing with quick stepper controls and AM/PM toggle pills.
 */
@Composable
fun BlackSlateTimeInputs(
    initialHour24: Int,
    initialMinute: Int,
    onTimeChanged: (hour24: Int, minute: Int) -> Unit
) {
    val theme = AppTheme.current
    val displayH = when {
        initialHour24 == 0 -> 12
        initialHour24 > 12 -> initialHour24 - 12
        else -> initialHour24
    }

    var isAm by remember(initialHour24) { mutableStateOf(initialHour24 < 12) }

    var hourStr by remember(displayH) {
        mutableStateOf(String.format(Locale.getDefault(), "%02d", displayH))
    }
    var minuteStr by remember(initialMinute) {
        mutableStateOf(String.format(Locale.getDefault(), "%02d", initialMinute))
    }

    var hourFocused by remember { mutableStateOf(false) }
    var minuteFocused by remember { mutableStateOf(false) }

    fun notifyChange(h: Int, m: Int, am: Boolean) {
        val validH = h.coerceIn(1, 12)
        val validM = m.coerceIn(0, 59)
        val h24 = when {
            am && validH == 12 -> 0
            !am && validH < 12 -> validH + 12
            else -> validH
        }
        onTimeChanged(h24, validM)
    }

    fun adjustMinutes(delta: Int) {
        val currentH = hourStr.toIntOrNull() ?: displayH
        val currentM = minuteStr.toIntOrNull() ?: initialMinute
        var totalMinutes = (if (isAm) (if (currentH == 12) 0 else currentH) else (if (currentH == 12) 12 else currentH + 12)) * 60 + currentM
        totalMinutes = (totalMinutes + delta + 1440) % 1440

        val newH24 = totalMinutes / 60
        val newM = totalMinutes % 60
        val newAm = newH24 < 12
        val newDisplayH = when {
            newH24 == 0 -> 12
            newH24 > 12 -> newH24 - 12
            else -> newH24
        }

        isAm = newAm
        hourStr = String.format(Locale.getDefault(), "%02d", newDisplayH)
        minuteStr = String.format(Locale.getDefault(), "%02d", newM)
        notifyChange(newDisplayH, newM, newAm)
    }

    val boxBg = if (theme.isMonochrome) Color(0xFF141414) else theme.surfaceColor
    val unselectedAmPmBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.cardBgColor

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // HOUR BOX
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "HOUR",
                    color = theme.secondaryTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 64.dp)
                        .background(boxBg, RoundedCornerShape(14.dp))
                        .border(1.5.dp, if (hourFocused) theme.accentColor else theme.cardBorderColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (hourStr.isEmpty()) {
                        Text(
                            text = "HH",
                            color = theme.accentColor.copy(alpha = 0.3f),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    BasicTextField(
                        value = hourStr,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(2)
                            hourStr = digits
                            val num = digits.toIntOrNull()
                            if (num != null && num in 1..12) {
                                val m = minuteStr.toIntOrNull() ?: initialMinute
                                notifyChange(num, m, isAm)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                hourFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    hourStr = ""
                                } else {
                                    val num = hourStr.toIntOrNull()?.coerceIn(1, 12) ?: displayH
                                    hourStr = String.format(Locale.getDefault(), "%02d", num)
                                    val m = minuteStr.toIntOrNull() ?: initialMinute
                                    notifyChange(num, m, isAm)
                                }
                            }
                            .testTag("black_slate_hour_input"),
                        textStyle = TextStyle(
                            color = theme.primaryTextColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(theme.accentColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            // COLON SEPARATOR
            Text(
                text = ":",
                color = theme.accentColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 16.dp)
            )

            // MINUTE BOX
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MIN",
                    color = theme.secondaryTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 64.dp)
                        .background(boxBg, RoundedCornerShape(14.dp))
                        .border(1.5.dp, if (minuteFocused) theme.accentColor else theme.cardBorderColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (minuteStr.isEmpty()) {
                        Text(
                            text = "MM",
                            color = theme.accentColor.copy(alpha = 0.3f),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    BasicTextField(
                        value = minuteStr,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(2)
                            minuteStr = digits
                            val num = digits.toIntOrNull()
                            if (num != null && num in 0..59) {
                                val h = hourStr.toIntOrNull() ?: displayH
                                notifyChange(h, num, isAm)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                minuteFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    minuteStr = ""
                                } else {
                                    val num = minuteStr.toIntOrNull()?.coerceIn(0, 59) ?: initialMinute
                                    minuteStr = String.format(Locale.getDefault(), "%02d", num)
                                    val h = hourStr.toIntOrNull() ?: displayH
                                    notifyChange(h, num, isAm)
                                }
                            }
                            .testTag("black_slate_minute_input"),
                        textStyle = TextStyle(
                            color = theme.primaryTextColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(theme.accentColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // AM / PM TOGGLE PILLS
            Column(
                modifier = Modifier.padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isAm) theme.accentColor else unselectedAmPmBg,
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (isAm) theme.accentColor else theme.cardBorderColor.copy(alpha = 0.4f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            isAm = true
                            val h = hourStr.toIntOrNull() ?: displayH
                            val m = minuteStr.toIntOrNull() ?: initialMinute
                            notifyChange(h, m, true)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("slate_am_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AM",
                        color = if (isAm) getContrastTextColor(theme.accentColor) else theme.primaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (!isAm) theme.accentColor else unselectedAmPmBg,
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (!isAm) theme.accentColor else theme.cardBorderColor.copy(alpha = 0.4f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            isAm = false
                            val h = hourStr.toIntOrNull() ?: displayH
                            val m = minuteStr.toIntOrNull() ?: initialMinute
                            notifyChange(h, m, false)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("slate_pm_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PM",
                        color = if (!isAm) getContrastTextColor(theme.accentColor) else theme.primaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Stepper Chips (+15m, +30m, +1h, -15m)
        val chipBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(-15 to "-15m", 15 to "+15m", 30 to "+30m", 60 to "+1h").forEach { (delta, label) ->
                    Box(
                        modifier = Modifier
                            .background(chipBg, RoundedCornerShape(8.dp))
                            .border(0.8.dp, theme.cardBorderColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .clickable { adjustMinutes(delta) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = theme.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Universal Reusable Time Picker Modal Dialog
 * Used across Settings, Alarms, Habits, and Tasks.
 */
@Composable
fun BlackSlateTimeDialog(
    title: String,
    currentHour: Int,
    currentMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour24: Int, minute: Int) -> Unit
) {
    val theme = AppTheme.current
    var workingHour by remember { mutableIntStateOf(currentHour) }
    var workingMinute by remember { mutableIntStateOf(currentMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = theme.accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.secondaryTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input
                BlackSlateTimeInputs(
                    initialHour24 = workingHour,
                    initialMinute = workingMinute,
                    onTimeChanged = { h24, m ->
                        workingHour = h24
                        workingMinute = m
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val cancelBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(cancelBg, RoundedCornerShape(14.dp))
                            .border(1.dp, theme.cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CANCEL",
                            color = theme.primaryTextColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .background(theme.accentColor, RoundedCornerShape(14.dp))
                            .clickable {
                                onConfirm(workingHour, workingMinute)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SET TIME",
                            color = getContrastTextColor(theme.accentColor),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
