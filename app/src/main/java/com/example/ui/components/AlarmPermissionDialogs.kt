package com.example.ui.components

import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppTheme
import com.example.ui.theme.getContrastSecondaryTextColor
import com.example.ui.theme.getContrastTextColor
import com.example.util.PermissionHelper

/**
 * Dialog to request Lock Screen and Overlay (Draw Over Other Apps) permissions.
 */
@Composable
fun LockScreenOverlayPermissionDialog(
    onDismiss: () -> Unit,
    onContinueToBattery: () -> Unit
) {
    val context = LocalContext.current
    val theme = AppTheme.current

    val hasOverlay = PermissionHelper.canDrawOverlays(context)
    val hasExactAlarm = PermissionHelper.canScheduleExactAlarms(context)
    val hasNotification = PermissionHelper.hasNotificationPermission(context)

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
                    borderColor = theme.dialogBorderColor,
                    glowColor = theme.dialogGlowColor.copy(alpha = 0.25f),
                    glowRadius = if (theme.isMonochrome) 0.dp else 12.dp
                )
                .padding(20.dp)
                .testTag("lock_screen_overlay_permission_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(theme.accentColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, theme.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Overlay Permission",
                        tint = theme.accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "LOCK SCREEN & OVERLAY ACCESS",
                    color = theme.accentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "To ring alarms immediately on top of your locked screen or when no apps are running, Android requires Display Over Other Apps and Alarm permissions.",
                    color = theme.primaryTextColor.copy(alpha = 0.9f),
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Permission item 1: Show on Lock Screen (Xiaomi / HyperOS / Android 14+)
                PermissionRowCard(
                    title = "Show on Lock Screen",
                    description = "Turns screen on & shows alarm card over lock screen without unlocking",
                    isGranted = false, // Always offer button to verify / toggle on OEM settings
                    actionText = "SET PERMISSION",
                    onGrantClick = {
                        PermissionHelper.openLockScreenPermission(context)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Permission item 2: Autostart & Background Run
                PermissionRowCard(
                    title = "Autostart & Background Run",
                    description = "Allows alarm to ring on lock screen even if app was swiped from recents",
                    isGranted = false,
                    actionText = "ENABLE AUTOSTART",
                    onGrantClick = {
                        PermissionHelper.openAutoStartPermission(context)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Permission item 3: Overlay / Display Over Apps
                PermissionRowCard(
                    title = "Display Over Other Apps",
                    description = "Presents ringing alarm full-screen over apps & home",
                    isGranted = hasOverlay,
                    onGrantClick = {
                        PermissionHelper.openOverlaySettings(context)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Permission item 4: Exact Alarms (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PermissionRowCard(
                        title = "Exact Alarm Timing",
                        description = "Triggers alarms precisely to the second",
                        isGranted = hasExactAlarm,
                        onGrantClick = {
                            PermissionHelper.openExactAlarmSettings(context)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Permission item 5: Notifications (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionRowCard(
                        title = "Alarm Notifications",
                        description = "Displays ringing heads-up alert banner",
                        isGranted = hasNotification,
                        onGrantClick = {
                            PermissionHelper.openAppNotificationSettings(context)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // OEM Failsafe Notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (theme.isMonochrome) Color(0xFF1C1C1C) else theme.accentColor.copy(alpha = 0.08f),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            0.8.dp,
                            if (theme.isMonochrome) Color(0xFF383838) else theme.accentColor.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "💡 Failsafe Tip: Enable 'Autostart' & set Battery to 'No restrictions' so the alarm wakes your screen even when cleared from recently closed apps.",
                        color = if (theme.isMonochrome) theme.secondaryTextColor else theme.accentColor,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Secondary
                    val secBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(secBg, RoundedCornerShape(12.dp))
                            .border(1.dp, theme.cardBorderColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LATER",
                            color = theme.secondaryTextColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Primary Button: Proceed to Battery Optimization step
                    Button(
                        onClick = onContinueToBattery,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("continue_to_battery_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.accentColor,
                            contentColor = getContrastTextColor(theme.accentColor)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "NEXT: BATTERY ➔",
                            color = getContrastTextColor(theme.accentColor),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated dialog reminding the user to turn off battery optimization.
 */
@Composable
fun BatteryOptimizationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val theme = AppTheme.current
    val isIgnoringBattery = PermissionHelper.isIgnoringBatteryOptimizations(context)
    val amber = if (theme.isMonochrome) Color(0xFFE0E0E0) else Color(0xFFFFB74D)

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
                    borderColor = if (theme.isMonochrome) theme.dialogBorderColor else amber,
                    glowColor = if (theme.isMonochrome) Color.Transparent else amber.copy(alpha = 0.25f),
                    glowRadius = if (theme.isMonochrome) 0.dp else 12.dp
                )
                .padding(20.dp)
                .testTag("battery_optimization_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(amber.copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, amber, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = "Battery Optimization",
                        tint = amber,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "DISABLE BATTERY OPTIMIZATION",
                    color = amber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Phone manufacturers (Samsung, Xiaomi, Pixel, OnePlus, Vivo, etc.) aggressively kill background apps. To guarantee your alarms ring 100% on time without being put to sleep, please turn off battery optimization.",
                    color = theme.primaryTextColor.copy(alpha = 0.9f),
                    fontSize = 12.5.sp,
                    lineHeight = 17.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status Banner
                val successColor = if (theme.isMonochrome) Color.White else Color(0xFF00E676)
                val dangerColor = if (theme.isMonochrome) Color(0xFF888888) else Color(0xFFFF5252)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isIgnoringBattery) successColor.copy(alpha = 0.15f) else dangerColor.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isIgnoringBattery) successColor else dangerColor.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isIgnoringBattery) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isIgnoringBattery) successColor else dangerColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isIgnoringBattery) "STATUS: UNRESTRICTED (PERFECT)" else "STATUS: BATTERY OPTIMIZED (RISK)",
                                color = if (isIgnoringBattery) successColor else dangerColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isIgnoringBattery) "App will never be closed by system sleep" else "Phone may sleep & delay alarms",
                                color = theme.secondaryTextColor,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button: Open Battery Settings
                Button(
                    onClick = {
                        PermissionHelper.requestIgnoreBatteryOptimization(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("request_ignore_battery_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = amber,
                        contentColor = getContrastTextColor(amber)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = getContrastTextColor(amber),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "TURN OFF OPTIMIZATION",
                            color = getContrastTextColor(amber),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dismiss Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DONE / DISMISS",
                        color = theme.secondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRowCard(
    title: String,
    description: String,
    isGranted: Boolean,
    actionText: String = "GRANT",
    onGrantClick: () -> Unit
) {
    val theme = AppTheme.current
    val rowBg = if (theme.isMonochrome) Color(0xFF181818) else theme.surfaceColor
    val okColor = if (theme.isMonochrome) Color.White else Color(0xFF00E676)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg, RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isGranted) okColor.copy(alpha = 0.6f) else theme.cardBorderColor.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = theme.primaryTextColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    color = theme.secondaryTextColor,
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Box(
                    modifier = Modifier
                        .background(okColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "✓ OK",
                        color = okColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(theme.accentColor, RoundedCornerShape(8.dp))
                        .clickable { onGrantClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = actionText,
                        color = getContrastTextColor(theme.accentColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
