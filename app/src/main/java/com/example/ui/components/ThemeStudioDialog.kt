package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppSettingsState
import com.example.ui.theme.getContrastBorderColor
import com.example.ui.theme.getContrastSecondaryTextColor
import com.example.ui.theme.getContrastTextColor
import java.util.Locale

// Curated standard and vibrant color choices for canvas backgrounds
val BackgroundColorOptions = listOf(
    0xFF000000 to "Pure Black",
    0xFF121212 to "Dark Charcoal",
    0xFF1A1D24 to "Obsidian Slate",
    0xFF0A192F to "Deep Navy",
    0xFF0D2818 to "Forest Green",
    0xFF2D0C15 to "Burgundy Crimson",
    0xFF1F1135 to "Royal Purple",
    0xFF1F140E to "Warm Espresso",
    0xFF0F2C3A to "Ocean Teal",
    0xFF242424 to "Medium Gray",
    0xFFE5E7EB to "Modern Light",
    0xFFFFFFFF to "Pure White"
)

// Curated solid palette for cards and components
val CardColorOptions = listOf(
    0xFF121212 to "Charcoal Black",
    0xFF1A1A1A to "Deep Gray",
    0xFF262626 to "Dark Slate",
    0xFF14213D to "Midnight Blue",
    0xFF0F382A to "Emerald Dark",
    0xFF3D131D to "Crimson Dark",
    0xFF281442 to "Amethyst Dark",
    0xFF332014 to "Espresso Dark",
    0xFF004D40 to "Deep Teal",
    0xFF374151 to "Slate Gray",
    0xFFE0E0E0 to "Ash White",
    0xFFFFFFFF to "Pure White"
)

// Accent highlight colors
val AccentColorOptions = listOf(
    0xFFFFFFFF to "Clean White",
    0xFF00FFFF to "Neon Cyan",
    0xFFFFD54F to "Warm Gold",
    0xFF00E676 to "Vibrant Green",
    0xFFFF5252 to "Coral Red",
    0xFFB388FF to "Soft Violet",
    0xFF40C4FF to "Sky Blue",
    0xFFFF80AB to "Pink Rose",
    0xFFFFAB40 to "Amber Orange"
)

enum class ThemeStudioTab {
    BACKGROUND,
    TASK_CARDS,
    NORMAL_ALARMS,
    HABIT_ALARMS,
    CALENDAR_ALARMS,
    ACCENT
}

@Composable
fun ThemeStudioDialog(
    settings: AppSettingsState,
    onDismiss: () -> Unit,
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
    ) -> Unit
) {
    var solidBgColor by remember { mutableLongStateOf(settings.customSolidBgColor) }
    var taskCardColor by remember { mutableLongStateOf(settings.customTaskCardColor) }
    var normalAlarmColor by remember { mutableLongStateOf(settings.customNormalAlarmCardColor) }
    var habitAlarmColor by remember { mutableLongStateOf(settings.customHabitAlarmCardColor) }
    var calendarAlarmColor by remember { mutableLongStateOf(settings.customCalendarAlarmCardColor) }
    var accentColor by remember { mutableLongStateOf(settings.customAccentColor) }

    var cardTransparency by remember { mutableFloatStateOf(settings.customCardTransparency) }
    var cornerRadius by remember { mutableFloatStateOf(settings.customCornerRadius) }
    var borderWidth by remember { mutableFloatStateOf(settings.customBorderWidth) }

    var selectedTab by remember { mutableStateOf(ThemeStudioTab.BACKGROUND) }

    val mainScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0C0E).copy(alpha = 0.98f))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(mainScrollState)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(accentColor).copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, Color(accentColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Studio",
                                tint = Color(accentColor),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CUSTOM THEME STUDIO",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Tailor backgrounds, card colors & geometry",
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFCCCCCC),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    thickness = 1.dp,
                    color = Color(0xFF2E2E32)
                )

                // LIVE PREVIEW CANVAS
                Text(
                    text = "LIVE INTERFACE PREVIEW",
                    color = Color(accentColor),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                ThemeStudioLivePreview(
                    bgColor = Color(solidBgColor),
                    taskCardColor = Color(taskCardColor),
                    normalAlarmColor = Color(normalAlarmColor),
                    habitAlarmColor = Color(habitAlarmColor),
                    calendarAlarmColor = Color(calendarAlarmColor),
                    accentColor = Color(accentColor),
                    transparency = cardTransparency,
                    cornerRadius = cornerRadius,
                    borderWidth = borderWidth
                )

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 1: CUSTOMIZATION TABS (Horizontal Scrollable Chips)
                Text(
                    text = "CUSTOMIZE COMPONENT COLORS",
                    color = Color(accentColor),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                val scrollTabsState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollTabsState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioTabChip(
                        label = "Canvas BG",
                        icon = Icons.Default.FormatPaint,
                        isSelected = selectedTab == ThemeStudioTab.BACKGROUND,
                        accentColor = Color(accentColor),
                        onClick = { selectedTab = ThemeStudioTab.BACKGROUND }
                    )
                    StudioTabChip(
                        label = "Task Cards",
                        icon = Icons.Default.CheckCircle,
                        isSelected = selectedTab == ThemeStudioTab.TASK_CARDS,
                        accentColor = Color(accentColor),
                        onClick = { selectedTab = ThemeStudioTab.TASK_CARDS }
                    )
                    StudioTabChip(
                        label = "Normal Alarms",
                        icon = Icons.Default.Alarm,
                        isSelected = selectedTab == ThemeStudioTab.NORMAL_ALARMS,
                        accentColor = Color(accentColor),
                        onClick = { selectedTab = ThemeStudioTab.NORMAL_ALARMS }
                    )
                    StudioTabChip(
                        label = "Habit Alarms",
                        icon = Icons.Default.Repeat,
                        isSelected = selectedTab == ThemeStudioTab.HABIT_ALARMS,
                        accentColor = Color(accentColor),
                        onClick = { selectedTab = ThemeStudioTab.HABIT_ALARMS }
                    )
                    StudioTabChip(
                        label = "Date Alarms",
                        icon = Icons.Default.DateRange,
                        isSelected = selectedTab == ThemeStudioTab.CALENDAR_ALARMS,
                        accentColor = Color(accentColor),
                        onClick = { selectedTab = ThemeStudioTab.CALENDAR_ALARMS }
                    )
                    StudioTabChip(
                        label = "Accents",
                        icon = Icons.Default.ColorLens,
                        isSelected = selectedTab == ThemeStudioTab.ACCENT,
                        accentColor = Color(accentColor),
                        onClick = { selectedTab = ThemeStudioTab.ACCENT }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // COLOR PALETTES BOX
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161618), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF28282E), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    when (selectedTab) {
                        ThemeStudioTab.BACKGROUND -> {
                            Column {
                                Text(
                                    text = "Solid Canvas Color",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Select base background color for the application frame:",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                ColorPaletteGrid(
                                    options = BackgroundColorOptions,
                                    selectedColorLong = solidBgColor,
                                    onSelect = { solidBgColor = it }
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E1E22), RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFF33333A), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(accentColor),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "💡 Tip: To frame a custom photo or wallpaper from your gallery, you can configure it in Wallpaper Settings.",
                                            color = Color(0xFFCCCCCC),
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        ThemeStudioTab.TASK_CARDS -> {
                            Column {
                                Text(
                                    text = "Task Cards Surface Color",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Color applied to task cards and task action dialogs:",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                ColorPaletteGrid(
                                    options = CardColorOptions,
                                    selectedColorLong = taskCardColor,
                                    onSelect = { taskCardColor = it }
                                )
                            }
                        }

                        ThemeStudioTab.NORMAL_ALARMS -> {
                            Column {
                                Text(
                                    text = "Normal Alarm Cards Color",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Surface tint for standard recurring/one-time alarms:",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                ColorPaletteGrid(
                                    options = CardColorOptions,
                                    selectedColorLong = normalAlarmColor,
                                    onSelect = { normalAlarmColor = it }
                                )
                            }
                        }

                        ThemeStudioTab.HABIT_ALARMS -> {
                            Column {
                                Text(
                                    text = "Habit Alarm Cards Color",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Surface tint for interval habit alarms (e.g. water reminders):",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                ColorPaletteGrid(
                                    options = CardColorOptions,
                                    selectedColorLong = habitAlarmColor,
                                    onSelect = { habitAlarmColor = it }
                                )
                            }
                        }

                        ThemeStudioTab.CALENDAR_ALARMS -> {
                            Column {
                                Text(
                                    text = "Calendar Alarm Cards Color",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Surface tint for annual date/birthday alarms:",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                ColorPaletteGrid(
                                    options = CardColorOptions,
                                    selectedColorLong = calendarAlarmColor,
                                    onSelect = { calendarAlarmColor = it }
                                )
                            }
                        }

                        ThemeStudioTab.ACCENT -> {
                            Column {
                                Text(
                                    text = "Highlights & Active Accents",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Color for active buttons, tabs, highlights, and icons:",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )

                                ColorPaletteGrid(
                                    options = AccentColorOptions,
                                    selectedColorLong = accentColor,
                                    onSelect = { accentColor = it }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 2: CLEAN DARK MODE SLIDERS (Easy to read, modern dark styling)
                Text(
                    text = "STYLE & GEOMETRY SLIDERS",
                    color = Color(accentColor),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161618), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF28282E), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Slider 1: Card Transparency / Opacity
                        CleanSliderRow(
                            label = "Card Opacity",
                            valueDisplay = "${(cardTransparency * 100).toInt()}%",
                            value = cardTransparency,
                            valueRange = 0.2f..1.0f,
                            accentColor = Color(accentColor),
                            onValueChange = { cardTransparency = it }
                        )

                        // Slider 2: Corner Radius
                        CleanSliderRow(
                            label = "Corner Rounding",
                            valueDisplay = "${cornerRadius.toInt()} dp",
                            value = cornerRadius,
                            valueRange = 4f..28f,
                            accentColor = Color(accentColor),
                            onValueChange = { cornerRadius = it }
                        )

                        // Slider 3: Border Width
                        CleanSliderRow(
                            label = "Border Width",
                            valueDisplay = String.format(Locale.getDefault(), "%.1f dp", borderWidth),
                            value = borderWidth,
                            valueRange = 0.5f..3.0f,
                            accentColor = Color(accentColor),
                            onValueChange = { borderWidth = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            solidBgColor = 0xFF000000
                            taskCardColor = 0xFF141414
                            normalAlarmColor = 0xFF161616
                            habitAlarmColor = 0xFF181818
                            calendarAlarmColor = 0xFF1C1C1C
                            accentColor = 0xFFFFFFFF
                            cardTransparency = 0.95f
                            cornerRadius = 14f
                            borderWidth = 1.0f
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE0E0E0)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF44444A))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RESET",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onSaveCustomTheme(
                                solidBgColor,
                                taskCardColor,
                                normalAlarmColor,
                                habitAlarmColor,
                                calendarAlarmColor,
                                accentColor,
                                cardTransparency,
                                cornerRadius,
                                borderWidth
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.8f)
                            .height(48.dp)
                            .testTag("save_custom_theme_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(accentColor),
                            contentColor = getContrastTextColor(Color(accentColor))
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SAVE & APPLY THEME",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun StudioTabChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0xFF1E1E22),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor else Color(0xFF333338),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentColor else Color(0xFF8E8E93),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFFCCCCCC),
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ColorPaletteGrid(
    options: List<Pair<Long, String>>,
    selectedColorLong: Long,
    onSelect: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val chunked = options.chunked(4)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { (colorValue, name) ->
                    val color = Color(colorValue)
                    val isSelected = selectedColorLong == colorValue
                    val checkmarkColor = getContrastTextColor(color)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(color, RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF00E5FF) else getContrastBorderColor(color),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelect(colorValue) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = name,
                                tint = checkmarkColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                // Fill remainder of row if < 4
                if (rowItems.size < 4) {
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CleanSliderRow(
    label: String,
    valueDisplay: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFF222228), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = valueDisplay,
                    color = accentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF33333D)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ThemeStudioLivePreview(
    bgColor: Color,
    taskCardColor: Color,
    normalAlarmColor: Color,
    habitAlarmColor: Color,
    calendarAlarmColor: Color,
    accentColor: Color,
    transparency: Float,
    cornerRadius: Float,
    borderWidth: Float
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    val taskText = getContrastTextColor(taskCardColor)
    val taskSubtext = getContrastSecondaryTextColor(taskCardColor)

    val normalAlarmText = getContrastTextColor(normalAlarmColor)
    val normalAlarmSubtext = getContrastSecondaryTextColor(normalAlarmColor)

    val habitAlarmText = getContrastTextColor(habitAlarmColor)
    val habitAlarmSubtext = getContrastSecondaryTextColor(habitAlarmColor)

    val calendarAlarmText = getContrastTextColor(calendarAlarmColor)
    val calendarAlarmSubtext = getContrastSecondaryTextColor(calendarAlarmColor)

    val bgText = getContrastTextColor(bgColor)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, getContrastBorderColor(bgColor), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Simulated Mini App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(taskCardColor.copy(alpha = transparency), RoundedCornerShape(8.dp))
                    .border(borderWidth.dp, getContrastBorderColor(taskCardColor), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(accentColor.copy(alpha = 0.25f), CircleShape)
                            .border(1.dp, accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Q", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MINIMAL TASKS",
                        color = taskText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(accentColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = getContrastTextColor(accentColor),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Task Card Sample
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(taskCardColor.copy(alpha = transparency), shape)
                    .border(borderWidth.dp, getContrastBorderColor(taskCardColor), shape)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .border(1.dp, accentColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Submit Quarterly Report",
                                color = taskText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Unticks tomorrow morning • 8:30 AM",
                                color = taskSubtext,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                    Text(text = "TASK", color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 2. Normal Alarm Card Sample
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(normalAlarmColor.copy(alpha = transparency), shape)
                    .border(borderWidth.dp, getContrastBorderColor(normalAlarmColor), shape)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "07:00 AM",
                            color = normalAlarmText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Morning Wakeup • Snooze: 5m",
                            color = normalAlarmSubtext,
                            fontSize = 9.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .border(1.dp, accentColor, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ALARM ON",
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. Habit Alarm Card Sample
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(habitAlarmColor.copy(alpha = transparency), shape)
                    .border(borderWidth.dp, getContrastBorderColor(habitAlarmColor), shape)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Drink Water (Every 60m)",
                            color = habitAlarmText,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active: 8:00 AM - 10:00 PM • Chained after dismiss",
                            color = habitAlarmSubtext,
                            fontSize = 9.5.sp
                        )
                    }
                    Text(
                        text = "HABIT",
                        color = Color(0xFFFFD54F),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. Calendar Alarm Card Sample
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(calendarAlarmColor.copy(alpha = transparency), shape)
                    .border(borderWidth.dp, getContrastBorderColor(calendarAlarmColor), shape)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Mom's Birthday • Aug 29",
                            color = calendarAlarmText,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Annual Calendar Alarm • Bypasses OEM limits",
                            color = calendarAlarmSubtext,
                            fontSize = 9.5.sp
                        )
                    }
                    Text(
                        text = "CALENDAR",
                        color = Color(0xFFB388FF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
