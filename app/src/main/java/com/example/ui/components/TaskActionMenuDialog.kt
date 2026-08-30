package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TaskItem
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.getContrastSecondaryTextColor
import com.example.ui.theme.getContrastTextColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskActionMenuDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onOpenTimePicker: (TaskItem) -> Unit,
    onClearReminder: (TaskItem) -> Unit,
    onToggleAutoUntick: (TaskItem, Boolean) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit
) {
    val theme = AppTheme.current
    var autoUntickState by remember(task.autoUntickTomorrow) {
        mutableStateOf(task.autoUntickTomorrow)
    }

    // Dynamic contrast colors based on task card background
    val dialogBg = theme.dialogBgColor
    val textColor = theme.primaryTextColor
    val textMutedColor = theme.secondaryTextColor
    val accentColor = theme.accentColor

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .frostedCyanStyle(
                    cornerRadius = theme.cardCornerRadius.coerceAtLeast(14.dp),
                    borderWidth = theme.cardBorderWidth,
                    backgroundColor = dialogBg,
                    borderColor = theme.dialogBorderColor,
                    glowColor = theme.dialogGlowColor,
                    glowRadius = if (theme.isMonochrome) 0.dp else 12.dp
                )
                .padding(18.dp)
                .testTag("task_action_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header: Task Title and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TASK OPTIONS",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = task.title,
                            color = textColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close options",
                            tint = textMutedColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = theme.cardBorderColor.copy(alpha = 0.35f)
                )

                // 1. Specific Reminder Option
                val optionBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                val optionBorder = theme.cardBorderColor.copy(alpha = 0.5f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 12.dp,
                            borderWidth = 1.dp,
                            backgroundColor = optionBg,
                            borderColor = optionBorder
                        )
                        .clickable {
                            onDismiss()
                            onOpenTimePicker(task)
                        }
                        .padding(12.dp)
                        .testTag("set_reminder_option")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Alarm",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Set Specific Reminder Time",
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (task.reminderTimeMillis != null) {
                                val timeFormat = SimpleDateFormat("EEEE, MMM d 'at' h:mm a", Locale.getDefault())
                                val formatted = timeFormat.format(Date(task.reminderTimeMillis))
                                Text(
                                    text = "Scheduled: $formatted",
                                    color = accentColor,
                                    fontSize = 12.sp
                                )
                            } else {
                                Text(
                                    text = "Tap to choose alarm time",
                                    color = textMutedColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (task.reminderTimeMillis != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClearReminder(task) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlarmOff,
                                contentDescription = "Remove reminder",
                                tint = DangerRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Remove Reminder",
                                color = DangerRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Auto-untick by tomorrow morning Option
                val sunTint = if (theme.isMonochrome) theme.secondaryTextColor else if (theme.isCustom) theme.accentColor else Color(0xFFFFD54F)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 12.dp,
                            borderWidth = 1.dp,
                            backgroundColor = optionBg,
                            borderColor = optionBorder
                        )
                        .padding(12.dp)
                        .testTag("auto_untick_option"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Morning Reset",
                            tint = sunTint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Untick by tomorrow morning",
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Puts task back on the active list tomorrow",
                                color = textMutedColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = autoUntickState,
                        onCheckedChange = { checked ->
                            autoUntickState = checked
                            onToggleAutoUntick(task, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = theme.switchThumbColor,
                            checkedTrackColor = theme.switchTrackColor,
                            uncheckedThumbColor = textMutedColor,
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Edit Task
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 12.dp,
                            borderWidth = 1.dp,
                            backgroundColor = optionBg,
                            borderColor = optionBorder
                        )
                        .clickable {
                            onDismiss()
                            onEditTask(task)
                        }
                        .padding(12.dp)
                        .testTag("edit_task_option"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Edit Task Name",
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Delete Task Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 12.dp,
                            borderWidth = 1.dp,
                            backgroundColor = DangerRed.copy(alpha = 0.15f),
                            borderColor = DangerRed,
                            glowColor = DangerRed.copy(alpha = 0.3f)
                        )
                        .clickable {
                            onDismiss()
                            onDeleteTask(task)
                        }
                        .padding(12.dp)
                        .testTag("delete_task_option"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Delete Task",
                        color = DangerRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
