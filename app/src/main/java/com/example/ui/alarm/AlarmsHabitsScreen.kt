package com.example.ui.alarm

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmRingingService
import com.example.alarm.AlarmSchedulerEngine
import com.example.data.AlarmItem
import com.example.data.AlarmType
import com.example.ui.components.frostedCyanStyle
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.getContrastSecondaryTextColor
import com.example.ui.theme.getContrastTextColor
import java.util.Calendar
import java.util.Locale

import androidx.compose.material.icons.filled.WarningAmber
import com.example.util.PermissionHelper

import androidx.compose.runtime.mutableLongStateOf
import kotlinx.coroutines.delay

enum class AlarmFilter {
    ALL,
    NORMAL,
    HABITS,
    CALENDAR
}

@Composable
fun AlarmsHabitsScreen(
    alarms: List<AlarmItem>,
    isSelectionMode: Boolean = false,
    selectedAlarmIds: Set<Long> = emptySet(),
    onToggleSelectAlarm: (Long) -> Unit = {},
    onEnterSelectionMode: (Long) -> Unit = {},
    onToggleAlarm: (AlarmItem, Boolean) -> Unit,
    onEditAlarm: (AlarmItem) -> Unit,
    onDeleteAlarm: (AlarmItem) -> Unit,
    onAddAlarmClick: (AlarmType) -> Unit,
    onRequestAlarmPermissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = AppTheme.current
    var selectedFilter by remember { mutableStateOf(AlarmFilter.ALL) }
    
    // Live ticking time for real-time countdown calculation
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val filteredAlarms = remember(alarms, selectedFilter) {
        when (selectedFilter) {
            AlarmFilter.ALL -> alarms
            AlarmFilter.NORMAL -> alarms.filter { it.type == AlarmType.NORMAL }
            AlarmFilter.HABITS -> alarms.filter { it.type == AlarmType.HABIT }
            AlarmFilter.CALENDAR -> alarms.filter { it.type == AlarmType.CALENDAR }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // FILTER TABS (ALL, NORMAL, HABITS, CALENDAR)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlarmFilterChip(
                label = "ALL (${alarms.size})",
                isSelected = selectedFilter == AlarmFilter.ALL,
                onClick = { selectedFilter = AlarmFilter.ALL },
                modifier = Modifier.weight(1f)
            )
            AlarmFilterChip(
                label = "ALARMS",
                isSelected = selectedFilter == AlarmFilter.NORMAL,
                onClick = { selectedFilter = AlarmFilter.NORMAL },
                modifier = Modifier.weight(1f)
            )
            AlarmFilterChip(
                label = "HABITS",
                isSelected = selectedFilter == AlarmFilter.HABITS,
                onClick = { selectedFilter = AlarmFilter.HABITS },
                modifier = Modifier.weight(1f)
            )
            AlarmFilterChip(
                label = "CALENDAR",
                isSelected = selectedFilter == AlarmFilter.CALENDAR,
                onClick = { selectedFilter = AlarmFilter.CALENDAR },
                modifier = Modifier.weight(1f)
            )
        }

        // PERMISSION STATUS BANNER (if overlay or battery optimization is not configured)
        if (!PermissionHelper.areAllAlarmPermissionsGranted(context)) {
            val bannerColor = if (theme.isMonochrome) Color.White else Color(0xFFFFB74D)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(
                        if (theme.isMonochrome) Color(0xFF1E1E1E) else bannerColor.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (theme.isMonochrome) Color(0xFF383838) else bannerColor.copy(alpha = 0.7f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onRequestAlarmPermissions() }
                    .padding(horizontal = 12.dp, vertical = 9.dp)
                    .testTag("alarm_permission_warning_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = bannerColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LOCK SCREEN & BATTERY SETUP",
                            color = bannerColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Tap to enable overlay & turn off battery optimization",
                            color = theme.secondaryTextColor,
                            fontSize = 10.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(bannerColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "FIX ➔",
                            color = getContrastTextColor(bannerColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ALARMS LIST OR EMPTY STATE
        if (filteredAlarms.isEmpty()) {
            EmptyAlarmsState(
                filter = selectedFilter,
                onAddAlarmClick = onAddAlarmClick
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("alarms_list"),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredAlarms,
                    key = { it.id }
                ) { alarm ->
                    AlarmCardItem(
                        alarm = alarm,
                        currentTimeMillis = currentTimeMillis,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedAlarmIds.contains(alarm.id),
                        onToggleSelect = { onToggleSelectAlarm(alarm.id) },
                        onLongPress = { onEnterSelectionMode(alarm.id) },
                        onToggle = { isChecked -> onToggleAlarm(alarm, isChecked) },
                        onEdit = { onEditAlarm(alarm) },
                        onDelete = { onDeleteAlarm(alarm) },
                        onTestRing = {
                            val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarm.id)
                                putExtra(AlarmRingingService.EXTRA_IS_SNOOZE, false)
                            }
                            context.startActivity(alertIntent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = AppTheme.current
    val accentColor = theme.accentColor
    val chipBg = if (isSelected) accentColor.copy(alpha = 0.22f) else if (theme.isMonochrome) Color(0xFF1A1A1A) else theme.surfaceColor
    val chipBorder = if (isSelected) accentColor else if (theme.isMonochrome) Color(0xFF333333) else theme.cardBorderColor.copy(alpha = 0.3f)
    val textColor = if (isSelected) accentColor else theme.secondaryTextColor

    Box(
        modifier = modifier
            .background(
                color = chipBg,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = chipBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmCardItem(
    alarm: AlarmItem,
    currentTimeMillis: Long = System.currentTimeMillis(),
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestRing: () -> Unit
) {
    val theme = AppTheme.current
    var menuExpanded by remember { mutableStateOf(false) }

    val rawCardBg = when (alarm.type) {
        AlarmType.HABIT -> theme.habitAlarmCardColor
        AlarmType.CALENDAR -> theme.calendarAlarmCardColor
        AlarmType.NORMAL -> theme.normalAlarmCardColor
    }

    val typeColor = when (alarm.type) {
        AlarmType.HABIT -> if (theme.isMonochrome) Color.White else Color(0xFFFFB74D)
        AlarmType.CALENDAR -> if (theme.isMonochrome) Color.White else Color(0xFFB388FF)
        AlarmType.NORMAL -> if (theme.isMonochrome) Color.White else theme.accentColor
    }

    val typeBadgeText = when (alarm.type) {
        AlarmType.HABIT -> "HABIT ALARM"
        AlarmType.CALENDAR -> "CALENDAR ALARM"
        AlarmType.NORMAL -> "NORMAL ALARM"
    }

    // Determine target scheduled occurrence
    val nextMillis = if (alarm.type == AlarmType.HABIT && alarm.isEnabled && alarm.nextTriggerTimeMillis > 0L) {
        alarm.nextTriggerTimeMillis
    } else {
        AlarmSchedulerEngine.calculateNextOccurrence(alarm, currentTimeMillis)
    }

    val timeDisplay = when (alarm.type) {
        AlarmType.HABIT -> {
            val cal = Calendar.getInstance().apply { timeInMillis = nextMillis }
            formatTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
        AlarmType.CALENDAR -> formatTime(alarm.hour, alarm.minute)
        AlarmType.NORMAL -> formatTime(alarm.hour, alarm.minute)
    }

    // Real-time live countdown updating every second / minute with currentTimeMillis
    val nextCountdown = run {
        val diff = nextMillis - currentTimeMillis
        when {
            diff <= 0 -> "Due now"
            diff < 60_000 -> "in ${(diff / 1000).coerceAtLeast(1)}s"
            diff < 3600_000 -> "in ${diff / 60_000}m"
            diff < 86400_000 -> {
                val hours = diff / 3600_000
                val mins = (diff % 3600_000) / 60_000
                if (mins == 0L) "in ${hours}h" else "in ${hours}h ${mins}m"
            }
            else -> {
                val days = diff / 86400_000
                val hours = (diff % 86400_000) / 3600_000
                "in ${days}d ${hours}h"
            }
        }
    }

    val cardBg = when {
        isSelected -> theme.accentColor.copy(alpha = if (theme.isMonochrome) 0.35f else 0.25f)
        !alarm.isEnabled -> rawCardBg.copy(alpha = if (theme.isMonochrome) 0.45f else 0.35f)
        else -> rawCardBg
    }
    val cardBorder = when {
        isSelected -> theme.accentColor
        alarm.isEnabled -> if (theme.isMonochrome) Color(0xFFCCCCCC) else typeColor.copy(alpha = 0.85f)
        else -> theme.cardBorderColor.copy(alpha = 0.3f)
    }
    val cardGlow = when {
        isSelected -> theme.cardGlowColor
        alarm.isEnabled && !theme.isMonochrome -> typeColor.copy(alpha = 0.25f)
        else -> Color.Transparent
    }

    val textColor = getContrastTextColor(cardBg)
    val textMuted = getContrastSecondaryTextColor(cardBg)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .frostedCyanStyle(
                cornerRadius = theme.cardCornerRadius,
                borderWidth = if (isSelected) (theme.cardBorderWidth + 0.5.dp) else theme.cardBorderWidth,
                backgroundColor = cardBg,
                borderColor = cardBorder,
                glowColor = cardGlow,
                glowRadius = if (isSelected) 12.dp else if (theme.isMonochrome) 0.dp else 8.dp
            )
            .clip(RoundedCornerShape(theme.cardCornerRadius))
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelect()
                    } else {
                        onEdit()
                    }
                },
                onLongClick = {
                    onLongPress()
                }
            )
            .padding(14.dp)
            .testTag("alarm_card_${alarm.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection Bubble (when in selection mode)
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (isSelected) theme.accentColor else Color.Transparent,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) theme.accentColor else theme.accentColor.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = getContrastTextColor(theme.accentColor),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // LEFT COLUMN: Time, Type, Details
            Column(modifier = Modifier.weight(1f)) {
                // Top Tag Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, typeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = typeBadgeText,
                            color = typeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (alarm.isEnabled) {
                        Box(
                            modifier = Modifier
                                .background(theme.accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⏰ $nextCountdown",
                                color = theme.accentColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Big Digital Time / Interval
                Text(
                    text = timeDisplay,
                    color = if (alarm.isEnabled) textColor else textMuted,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Title / Label
                Text(
                    text = alarm.title,
                    color = if (alarm.isEnabled) textColor.copy(alpha = 0.9f) else textMuted,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Detail Specs
                when (alarm.type) {
                    AlarmType.HABIT -> {
                        Text(
                            text = "Every ${alarm.habitIntervalMinutes}m • Active: ${formatTime(alarm.habitWakeHour, alarm.habitWakeMinute)} - ${formatTime(alarm.habitSleepHour, alarm.habitSleepMinute)} • ${formatDaysMask(alarm.habitDaysOfWeek)}",
                            color = textMuted,
                            fontSize = 10.5.sp
                        )
                    }

                    AlarmType.CALENDAR -> {
                        Text(
                            text = "Date: ${getMonthName(alarm.calendarMonth)} ${alarm.calendarDay}, ${alarm.calendarYear} • ${if (alarm.calendarRepeatAnnually) "Annual: Forever" else "This Year Only"}",
                            color = textMuted,
                            fontSize = 10.5.sp
                        )
                    }

                    AlarmType.NORMAL -> {
                        Text(
                            text = "${formatDaysMask(alarm.repeatDaysOfWeek)} • Snooze: ${if (alarm.snoozeEnabled) "${alarm.snoozeDurationMinutes}m" else "OFF"} • Tone: ${alarm.ringtoneName}",
                            color = textMuted,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            // RIGHT ACTIONS: Switch & 3-Dot Overflow Menu (Only in normal mode)
            if (!isSelectionMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = if (theme.isMonochrome) Color.White else typeColor,
                            uncheckedThumbColor = textMuted,
                            uncheckedTrackColor = if (theme.isMonochrome) Color(0xFF333333) else theme.surfaceColor
                        ),
                        modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                    )

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("alarm_menu_button_${alarm.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = theme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Alarm", color = textColor) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = theme.accentColor) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Test / Preview Alert", color = theme.accentColor) },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.accentColor) },
                                onClick = {
                                    menuExpanded = false
                                    onTestRing()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Alarm", color = DangerRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAlarmsState(
    filter: AlarmFilter,
    onAddAlarmClick: (AlarmType) -> Unit
) {
    val theme = AppTheme.current
    val accentColor = theme.accentColor
    val textColor = theme.primaryTextColor
    val textMuted = theme.secondaryTextColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .frostedCyanStyle(
                        cornerRadius = 40.dp,
                        borderWidth = theme.cardBorderWidth,
                        backgroundColor = if (theme.isMonochrome) Color(0xFF1A1A1A) else theme.surfaceColor,
                        borderColor = theme.cardBorderColor.copy(alpha = 0.5f),
                        glowColor = theme.cardGlowColor,
                        glowRadius = if (theme.isMonochrome) 0.dp else 16.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "No Alarms",
                    tint = accentColor,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "NO ALARMS CONFIGURED",
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Set customizable alarms, habit interval loops (with sleep bounds), and recurring annual calendar alarms.",
                color = textMuted,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, accentColor, RoundedCornerShape(12.dp))
                        .clickable { onAddAlarmClick(AlarmType.NORMAL) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "+ ALARM",
                        color = accentColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(if (theme.isMonochrome) Color(0xFF333333) else Color(0xFFFFB74D).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, if (theme.isMonochrome) Color.White else Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                        .clickable { onAddAlarmClick(AlarmType.HABIT) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "+ HABIT",
                        color = if (theme.isMonochrome) Color.White else Color(0xFFFFB74D),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(if (theme.isMonochrome) Color(0xFF333333) else Color(0xFFB388FF).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, if (theme.isMonochrome) Color.White else Color(0xFFB388FF), RoundedCornerShape(12.dp))
                        .clickable { onAddAlarmClick(AlarmType.CALENDAR) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "+ DATE",
                        color = if (theme.isMonochrome) Color.White else Color(0xFFB388FF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val isAm = hour < 12
    val displayH = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val amPm = if (isAm) "AM" else "PM"
    return String.format(Locale.getDefault(), "%02d:%02d %s", displayH, minute, amPm)
}

private fun formatHour(hour: Int): String {
    val isAm = hour < 12
    val displayH = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayH ${if (isAm) "AM" else "PM"}"
}

private fun formatDaysMask(mask: Int): String {
    if (mask == 127) return "Every day"
    if (mask == 31) return "Mon - Fri"
    if (mask == 96) return "Weekends"
    val days = mutableListOf<String>()
    if ((mask and 1) != 0) days.add("M")
    if ((mask and 2) != 0) days.add("T")
    if ((mask and 4) != 0) days.add("W")
    if ((mask and 8) != 0) days.add("Th")
    if ((mask and 16) != 0) days.add("F")
    if ((mask and 32) != 0) days.add("Sa")
    if ((mask and 64) != 0) days.add("Su")
    return days.joinToString(" ")
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
