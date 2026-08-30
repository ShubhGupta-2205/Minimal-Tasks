package com.example.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.alarm.AlarmRingingService
import com.example.data.AlarmItem
import com.example.data.AlarmType
import com.example.data.AppDatabase
import com.example.ui.components.ImmersiveAmbientBackground
import com.example.ui.components.frostedCyanStyle
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.CyanNeonGlow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.FrostedBackground
import com.example.ui.theme.TextCyanSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupWindowFlags()

        val alarmId = intent.getLongExtra(AlarmRingingService.EXTRA_ALARM_ID, -1L)
        val isSnooze = intent.getBooleanExtra(AlarmRingingService.EXTRA_IS_SNOOZE, false)
        val snoozeCount = intent.getIntExtra(AlarmRingingService.EXTRA_SNOOZE_COUNT, AlarmRingingService.activeSnoozeCount)

        setContent {
            var alarmItem by remember { mutableStateOf<AlarmItem?>(AlarmRingingService.activeRingingAlarm) }

            LaunchedEffect(alarmId) {
                if (alarmItem == null && alarmId != -1L) {
                    val db = AppDatabase.getDatabase(applicationContext)
                    alarmItem = db.alarmDao().getAlarmById(alarmId)
                }
            }

            AlarmAlertScreen(
                alarm = alarmItem,
                isSnooze = isSnooze,
                snoozeCount = snoozeCount,
                onDismiss = {
                    val dismissIntent = Intent(this, AlarmRingingService::class.java).apply {
                        action = AlarmRingingService.ACTION_DISMISS
                        putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
                    }
                    startService(dismissIntent)
                    finish()
                },
                onSnooze = {
                    val snoozeIntent = Intent(this, AlarmRingingService::class.java).apply {
                        action = AlarmRingingService.ACTION_SNOOZE
                        putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
                        putExtra(AlarmRingingService.EXTRA_SNOOZE_COUNT, snoozeCount)
                    }
                    startService(snoozeIntent)
                    finish()
                }
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupWindowFlags()
    }

    override fun onResume() {
        super.onResume()
        setupWindowFlags()
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
    }
}

@Composable
fun AlarmAlertScreen(
    alarm: AlarmItem?,
    isSnooze: Boolean,
    snoozeCount: Int = 0,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var currentTimeStr by remember { mutableStateOf("") }
    var currentAmPm by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }
    var secondsRemaining by remember { mutableStateOf(120) } // 2 minutes failsafe countdown

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
            val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            currentTimeStr = timeFormat.format(now)
            currentAmPm = amPmFormat.format(now).uppercase()
            currentDateStr = dateFormat.format(now)
            if (secondsRemaining > 0) {
                secondsRemaining--
            }
            delay(1000)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val snoozeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "snoozeScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        // Background Layer: Custom Image or Cyber Glow
        if (!alarm?.customBackgroundUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(alarm?.customBackgroundUri),
                contentDescription = "Custom Alarm Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
            )
        } else if (alarm?.customBackgroundPreset == "CYAN_GRID") {
            Image(
                painter = painterResource(id = R.drawable.bg_cyber_grid_1787763688204),
                contentDescription = "Cyber Grid",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        } else {
            ImmersiveAmbientBackground()
        }

        // Foreground content container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Alarm Type Badge, Failsafe Indicator & Date
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                val badgeText = when (alarm?.type) {
                    AlarmType.HABIT -> "⚡ HABIT ALERT"
                    AlarmType.CALENDAR -> "📅 CALENDAR ALARM"
                    AlarmType.NORMAL, null -> if (isSnooze) "⏰ SNOOZED ALARM" else "⏰ ALARM"
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(CyanNeon.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .border(1.dp, CyanNeon, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = CyanNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    if (snoozeCount > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFB300).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(16.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "SNOOZE $snoozeCount/3",
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Failsafe 2-minute status chip
                val failsafeMins = secondsRemaining / 60
                val failsafeSecs = secondsRemaining % 60
                Box(
                    modifier = Modifier
                        .background(Color(0x3300FFFF), RoundedCornerShape(12.dp))
                        .border(0.8.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (snoozeCount < 3)
                            "🛡️ FAILSAFE: Auto-snooze in %02d:%02d".format(failsafeMins, failsafeSecs)
                        else
                            "🛡️ FAILSAFE: Auto-dismiss in %02d:%02d (Final)".format(failsafeMins, failsafeSecs),
                        color = TextCyanSubtle,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentDateStr,
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // MIDDLE SECTION: Clock + Massive Curved Snooze Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Digital Time Display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentTimeStr,
                        color = TextWhite,
                        fontSize = 76.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentAmPm,
                        color = CyanNeon,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }

                // Alarm Label / Habit Message
                val titleDisplay = alarm?.title?.ifBlank { "Alarm" } ?: "Wake Up"
                Text(
                    text = titleDisplay,
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                if (alarm?.type == AlarmType.HABIT && !alarm.habitMessage.isNullOrBlank()) {
                    Text(
                        text = alarm.habitMessage,
                        color = TextCyanSubtle,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // MASSIVE CIRCULAR SNOOZE BUTTON (Right underneath time display)
                if (alarm?.snoozeEnabled != false && snoozeCount < 3) {
                    val snoozeMins = alarm?.snoozeDurationMinutes ?: 5

                    Box(
                        modifier = Modifier
                            .scale(snoozeScale)
                            .size(176.dp)
                            .frostedCyanStyle(
                                cornerRadius = 88.dp,
                                borderWidth = 2.dp,
                                backgroundColor = FrostedBackground,
                                borderColor = CyanNeon,
                                glowColor = CyanNeonGlow,
                                glowRadius = 24.dp
                            )
                            .clip(CircleShape)
                            .clickable(onClick = onSnooze)
                            .testTag("alarm_snooze_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = "Snooze",
                                tint = CyanNeon,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "SNOOZE",
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.5.sp
                            )
                            Text(
                                text = "+$snoozeMins MIN",
                                color = CyanNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${snoozeCount + 1}/3",
                                color = TextMuted,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, CyanNeon.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Ringing",
                            tint = CyanNeon,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }

            // BOTTOM SECTION: STOP / DISMISS BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .frostedCyanStyle(
                        cornerRadius = 20.dp,
                        borderWidth = 1.5.dp,
                        backgroundColor = Color(0x66FF5252),
                        borderColor = DangerRed,
                        glowColor = Color(0x66FF5252),
                        glowRadius = 14.dp
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 18.dp)
                    .testTag("alarm_dismiss_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop Alarm",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "DISMISS / STOP ALARM",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
