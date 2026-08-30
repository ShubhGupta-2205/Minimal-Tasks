package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.ui.theme.DangerRed
import com.example.ui.theme.getContrastSecondaryTextColor
import com.example.ui.theme.getContrastTextColor

@Composable
fun TaskSelectionBubbleBar(
    selectedCount: Int,
    isAllSelected: Boolean,
    onToggleSelectAll: () -> Unit,
    onCompleteSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = AppTheme.current
    val barBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
    val accentColor = theme.accentColor
    val deleteTint = if (theme.isMonochrome) theme.accentColor else DangerRed

    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .height(56.dp)
                .frostedCyanStyle(
                    cornerRadius = 28.dp,
                    borderWidth = theme.cardBorderWidth.coerceAtLeast(1.dp),
                    backgroundColor = barBg,
                    borderColor = theme.cardBorderColor,
                    glowColor = theme.cardGlowColor,
                    glowRadius = if (theme.isMonochrome) 0.dp else 14.dp
                )
                .clip(RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("task_multi_selection_bubble_bar")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Count Bubble Pill
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                        .border(1.dp, accentColor, RoundedCornerShape(18.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$selectedCount SELECTED",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                // Select All / Deselect All Bubble
                BubbleActionButton(
                    icon = Icons.Default.SelectAll,
                    label = if (isAllSelected) "NONE" else "ALL",
                    tint = accentColor,
                    onClick = onToggleSelectAll
                )

                // Complete Action Bubble
                BubbleActionButton(
                    icon = Icons.Default.CheckCircle,
                    label = "DONE",
                    tint = accentColor,
                    onClick = onCompleteSelected
                )

                // Delete Action Bubble
                BubbleActionButton(
                    icon = Icons.Default.Delete,
                    label = "DELETE",
                    tint = deleteTint,
                    onClick = onDeleteSelected
                )

                // Cancel / Close Bubble
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(if (theme.isMonochrome) Color(0xFF2C2C2C) else theme.surfaceColor, CircleShape)
                        .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onCancelSelection),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel selection",
                        tint = getContrastTextColor(barBg),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmSelectionBubbleBar(
    selectedCount: Int,
    isAllSelected: Boolean,
    onToggleSelectAll: () -> Unit,
    onToggleEnabledSelected: (Boolean) -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = AppTheme.current
    val barBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
    val accentColor = theme.accentColor
    val deleteTint = if (theme.isMonochrome) theme.accentColor else DangerRed

    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .height(56.dp)
                .frostedCyanStyle(
                    cornerRadius = 28.dp,
                    borderWidth = theme.cardBorderWidth.coerceAtLeast(1.dp),
                    backgroundColor = barBg,
                    borderColor = theme.cardBorderColor,
                    glowColor = theme.cardGlowColor,
                    glowRadius = if (theme.isMonochrome) 0.dp else 14.dp
                )
                .clip(RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("alarm_multi_selection_bubble_bar")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Count Bubble Pill
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                        .border(1.dp, accentColor, RoundedCornerShape(18.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$selectedCount SELECTED",
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                // Select All / Deselect All Bubble
                BubbleActionButton(
                    icon = Icons.Default.SelectAll,
                    label = if (isAllSelected) "NONE" else "ALL",
                    tint = accentColor,
                    onClick = onToggleSelectAll
                )

                // Enable All Selected
                BubbleActionButton(
                    icon = Icons.Default.PowerSettingsNew,
                    label = "ON",
                    tint = accentColor,
                    onClick = { onToggleEnabledSelected(true) }
                )

                // Disable All Selected
                BubbleActionButton(
                    icon = Icons.Default.PowerSettingsNew,
                    label = "OFF",
                    tint = getContrastSecondaryTextColor(barBg),
                    onClick = { onToggleEnabledSelected(false) }
                )

                // Delete Action Bubble
                BubbleActionButton(
                    icon = Icons.Default.Delete,
                    label = "DELETE",
                    tint = deleteTint,
                    onClick = onDeleteSelected
                )

                // Cancel / Close Bubble
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(if (theme.isMonochrome) Color(0xFF2C2C2C) else theme.surfaceColor, CircleShape)
                        .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape)
                        .clickable(onClick = onCancelSelection),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel selection",
                        tint = getContrastTextColor(barBg),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BubbleActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    val theme = AppTheme.current
    val buttonBg = if (theme.isMonochrome) Color(0xFF2A2A2A) else theme.surfaceColor

    Box(
        modifier = Modifier
            .background(buttonBg, RoundedCornerShape(16.dp))
            .border(1.dp, tint.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = tint,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
