package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskItem
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.WarningGold
import com.example.ui.theme.getContrastSecondaryTextColor
import com.example.ui.theme.getContrastTextColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItemRow(
    task: TaskItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggle: (TaskItem) -> Unit,
    onLongPress: (TaskItem) -> Unit,
    onOpenMenu: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = AppTheme.current

    val completedAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted && !isSelectionMode) 0.6f else 1f,
        label = "completedAlpha"
    )

    val cardBg = when {
        isSelected -> theme.accentColor.copy(alpha = if (theme.isMonochrome) 0.35f else 0.25f)
        task.isCompleted -> theme.taskCardColor.copy(alpha = if (theme.isMonochrome) 0.5f else 0.4f)
        else -> theme.taskCardColor
    }
    val cardBorder = when {
        isSelected -> theme.accentColor
        task.isCompleted -> theme.cardBorderColor.copy(alpha = 0.3f)
        else -> theme.cardBorderColor
    }
    val cardGlow = when {
        isSelected -> theme.cardGlowColor
        task.isCompleted -> Color.Transparent
        else -> theme.cardGlowColor
    }

    val textColor = getContrastTextColor(cardBg)
    val textMutedColor = getContrastSecondaryTextColor(cardBg)
    val accentColor = theme.accentColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .frostedCyanStyle(
                cornerRadius = theme.cardCornerRadius,
                borderWidth = if (isSelected) (theme.cardBorderWidth + 0.5.dp) else theme.cardBorderWidth,
                backgroundColor = cardBg,
                borderColor = cardBorder,
                glowColor = cardGlow,
                glowRadius = if (isSelected) (if (theme.isMonochrome) 0.dp else 12.dp) else if (task.isCompleted || theme.isMonochrome) 0.dp else 8.dp
            )
            .alpha(completedAlpha)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onLongPress(task)
                    } else {
                        onToggle(task)
                    }
                },
                onLongClick = { onLongPress(task) }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("task_item_${task.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Indicator: Multi-selection bubble OR standard checkbox
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isSelected) accentColor else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) accentColor else accentColor.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = getContrastTextColor(accentColor),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        } else {
            // Smooth rounded checkbox
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = if (task.isCompleted) accentColor.copy(alpha = 0.35f) else Color.Transparent,
                        shape = RoundedCornerShape(7.dp)
                    )
                    .border(
                        width = 1.2.dp,
                        color = if (task.isCompleted) accentColor else accentColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(7.dp)
                    )
                    .clickable { onToggle(task) }
                    .testTag("checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Left accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(28.dp)
                .background(
                    color = if (isSelected) accentColor else if (task.isCompleted) accentColor.copy(alpha = 0.2f) else accentColor,
                    shape = RoundedCornerShape(3.dp)
                )
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Task Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = task.title,
                color = if (task.isCompleted && !isSelectionMode) textColor.copy(alpha = 0.5f) else textColor,
                fontSize = 14.sp,
                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (task.isCompleted && !isSelectionMode) TextDecoration.LineThrough else TextDecoration.None
            )

            // Subtitle reminder & auto-reset badges
            if (task.reminderTimeMillis != null || task.autoUntickTomorrow) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (task.reminderTimeMillis != null) {
                        val reminderCalendar = Calendar.getInstance().apply { timeInMillis = task.reminderTimeMillis }
                        val todayCalendar = Calendar.getInstance()
                        val isToday = reminderCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                                reminderCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)

                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val reminderText = if (isToday) {
                            "${timeFormat.format(Date(task.reminderTimeMillis))} Today"
                        } else {
                            val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                            dateFormat.format(Date(task.reminderTimeMillis))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Reminder",
                                tint = accentColor.copy(alpha = 0.85f),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = reminderText,
                                color = accentColor.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    if (task.autoUntickTomorrow) {
                        val resetBadgeColor = if (theme.isMonochrome) theme.secondaryTextColor else if (theme.isCustom) theme.accentColor else WarningGold
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Auto reset",
                                tint = resetBadgeColor.copy(alpha = 0.85f),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Reset morning",
                                color = resetBadgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Rightmost End: Dedicated 3-Dot Button to open Action Menu
        if (!isSelectionMode) {
            IconButton(
                onClick = { onOpenMenu(task) },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("task_menu_button_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Task options",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
