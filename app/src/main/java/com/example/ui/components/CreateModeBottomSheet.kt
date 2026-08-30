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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.getContrastSecondaryTextColor
import com.example.ui.theme.getContrastTextColor

enum class CreateOption {
    REGULAR_TASK,
    NORMAL_ALARM,
    HABIT_ALARM,
    CALENDAR_ALARM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModeBottomSheet(
    onDismiss: () -> Unit,
    onSelectOption: (CreateOption) -> Unit
) {
    val theme = AppTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetBg = if (theme.isMonochrome) Color(0xFF121212) else theme.surfaceColor
    val textColor = getContrastTextColor(sheetBg)
    val textMuted = getContrastSecondaryTextColor(sheetBg)
    val accentColor = theme.accentColor

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = theme.cardBorderWidth,
                    color = theme.cardBorderColor.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CREATE NEW ITEM",
                        color = accentColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Choose item type to configure",
                        color = textMuted,
                        fontSize = 12.5.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Option 1: Regular Task
            CreateOptionCard(
                icon = Icons.Default.CheckCircle,
                iconColor = if (theme.isMonochrome) Color.White else theme.accentColor,
                title = "Regular Task",
                badge = "TASK",
                subtitle = "Checklist item with optional reminders and daily auto-reset",
                onClick = { onSelectOption(CreateOption.REGULAR_TASK) },
                testTag = "create_regular_task_option"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Normal Alarm
            CreateOptionCard(
                icon = Icons.Default.Alarm,
                iconColor = if (theme.isMonochrome) Color.White else theme.normalAlarmAccentColor,
                title = "Normal Alarm",
                badge = "ALARM",
                subtitle = "Full-screen alarm with custom ringtones, massive snooze & custom art",
                onClick = { onSelectOption(CreateOption.NORMAL_ALARM) },
                testTag = "create_normal_alarm_option"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 3: Habit Alarm
            CreateOptionCard(
                icon = Icons.Default.Repeat,
                iconColor = if (theme.isMonochrome) Color.White else theme.habitAccentColor,
                title = "Habit Alarm",
                badge = "INTERVAL",
                subtitle = "Repeats every X minutes strictly within your wake hours (respects sleep time)",
                onClick = { onSelectOption(CreateOption.HABIT_ALARM) },
                testTag = "create_habit_alarm_option"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 4: Calendar Alarm
            CreateOptionCard(
                icon = Icons.Default.DateRange,
                iconColor = if (theme.isMonochrome) Color.White else theme.calendarAccentColor,
                title = "Calendar Alarm",
                badge = "DATE",
                subtitle = "Bypass OEM notification limits for birthdays, anniversaries & annual events",
                onClick = { onSelectOption(CreateOption.CALENDAR_ALARM) },
                testTag = "create_calendar_alarm_option"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CreateOptionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    badge: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    val theme = AppTheme.current
    val cardBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
    val textColor = getContrastTextColor(cardBg)
    val textMuted = getContrastSecondaryTextColor(cardBg)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .frostedCyanStyle(
                cornerRadius = theme.cardCornerRadius,
                borderWidth = theme.cardBorderWidth,
                backgroundColor = cardBg,
                borderColor = if (theme.isMonochrome) Color(0xFF333333) else iconColor.copy(alpha = 0.6f),
                glowColor = if (theme.isMonochrome) Color.Transparent else iconColor.copy(alpha = 0.2f),
                glowRadius = if (theme.isMonochrome) 0.dp else 6.dp
            )
            .clip(RoundedCornerShape(theme.cardCornerRadius))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, iconColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(iconColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = iconColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = textMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
