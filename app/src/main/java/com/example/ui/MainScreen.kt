package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.AlarmItem
import com.example.data.AlarmType
import com.example.data.BackgroundThemeType
import com.example.data.TaskItem
import com.example.ui.alarm.AddEditAlarmDialog
import com.example.ui.alarm.AlarmsHabitsScreen
import com.example.ui.components.LockScreenOverlayPermissionDialog
import com.example.ui.components.BatteryOptimizationDialog
import com.example.util.PermissionHelper
import com.example.ui.components.AddTaskBar
import com.example.ui.components.CreateModeBottomSheet
import com.example.ui.components.CreateOption
import com.example.ui.components.EditTaskDialog
import com.example.ui.components.FrostedCyanBox
import com.example.ui.components.ImmersiveAmbientBackground
import com.example.ui.components.AlarmSelectionBubbleBar
import com.example.ui.components.QuickAddTaskDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TaskActionMenuDialog
import com.example.ui.components.TaskItemRow
import com.example.ui.components.TaskSelectionBubbleBar
import com.example.ui.components.TaskTimePickerDialog
import com.example.ui.components.frostedCyanStyle
import com.example.ui.theme.AppTheme
import com.example.ui.theme.getContrastSecondaryTextColor
import com.example.ui.theme.getContrastTextColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val theme = AppTheme.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // 2-Window ViewPager2 / HorizontalPager state
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Multi-selection states
    var selectedTaskIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val isTaskSelectionMode = selectedTaskIds.isNotEmpty()

    var selectedAlarmIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val isAlarmSelectionMode = selectedAlarmIds.isNotEmpty()

    // Dialog & Sheet states
    var showCreateMenuSheet by remember { mutableStateOf(false) }
    var showQuickAddTaskDialog by remember { mutableStateOf(false) }
    var alarmDialogTypeToOpen by remember { mutableStateOf<AlarmType?>(null) }
    var alarmToEdit by remember { mutableStateOf<AlarmItem?>(null) }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }
    var selectedTaskForMenu by remember { mutableStateOf<TaskItem?>(null) }
    var selectedTaskForTimePicker by remember { mutableStateOf<TaskItem?>(null) }
    var selectedTaskForEdit by remember { mutableStateOf<TaskItem?>(null) }

    // Notification permission handling for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Check background & lock screen permissions on start
        if (!PermissionHelper.canDrawOverlays(context)) {
            showOverlayPermissionDialog = true
        } else if (!PermissionHelper.isIgnoringBatteryOptimizations(context)) {
            showBatteryOptimizationDialog = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundColor)
    ) {
        // 1. DYNAMIC BACKGROUND LAYER (Ambient Orbital Aura / Cyber Grid / Custom Image)
        if (!theme.isMonochrome) {
            when (settings.backgroundType) {
                BackgroundThemeType.DARK_BLUE -> {
                    ImmersiveAmbientBackground()
                }

                BackgroundThemeType.CYAN_GRID -> {
                    Image(
                        painter = painterResource(id = R.drawable.bg_cyber_grid_1787763688204),
                        contentDescription = "Cyber grid background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f))
                    )
                }

                BackgroundThemeType.CUSTOM_IMAGE -> {
                    if (settings.customImageUri != null) {
                        AsyncImage(
                            model = settings.customImageUri,
                            contentDescription = "Custom background",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f))
                        )
                    } else {
                        ImmersiveAmbientBackground()
                    }
                }
            }
        }

        // 2. OUTER FRAME CONTAINER
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .border(
                    width = theme.cardBorderWidth,
                    color = if (theme.isMonochrome) Color(0xFF222222) else theme.cardBorderColor.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // TOP HEADER WITH AVATAR AND APP TITLE
                val headerBg = if (theme.isMonochrome) Color(0xFF121212) else theme.surfaceColor
                val headerBorder = if (theme.isMonochrome) Color(0xFF333333) else theme.cardBorderColor
                val headerGlow = if (theme.isMonochrome) Color.Transparent else theme.cardGlowColor
                
                FrostedCyanBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("app_header_box"),
                    cornerRadius = theme.cardCornerRadius.coerceAtMost(8.dp),
                    borderWidth = theme.cardBorderWidth,
                    backgroundColor = headerBg,
                    borderColor = headerBorder,
                    glowColor = headerGlow,
                    padding = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Avatar + Minimal Tasks branding
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        color = theme.accentColor.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = theme.accentColor,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Q",
                                    color = theme.accentColor,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MINIMAL ",
                                    color = theme.primaryTextColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "TASKS",
                                    color = theme.accentColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SWIPEABLE WINDOW TABS / SLIDER INDICATOR (Window 1: Tasks vs Window 2: Alarms & Habits)
                val activeCount = tasks.count { !it.isCompleted }
                val activeAlarmsCount = alarms.count { it.isEnabled }
                val tabBg = if (theme.isMonochrome) Color(0xFF161616) else theme.surfaceColor
                val tabBorder = if (theme.isMonochrome) Color(0xFF2C2C2C) else theme.cardBorderColor.copy(alpha = 0.4f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .frostedCyanStyle(
                            cornerRadius = 14.dp,
                            borderWidth = theme.cardBorderWidth,
                            backgroundColor = tabBg,
                            borderColor = tabBorder,
                            glowColor = if (theme.isMonochrome) Color.Transparent else theme.cardGlowColor.copy(alpha = 0.2f),
                            glowRadius = if (theme.isMonochrome) 0.dp else 6.dp
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Window 1 Tab Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (pagerState.currentPage == 0) theme.accentColor.copy(alpha = 0.22f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (pagerState.currentPage == 0) 1.dp else 0.dp,
                                color = if (pagerState.currentPage == 0) theme.accentColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            }
                            .padding(vertical = 8.dp)
                            .testTag("tab_window_tasks"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = "Tasks",
                                tint = if (pagerState.currentPage == 0) theme.accentColor else theme.secondaryTextColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TASKS ($activeCount)",
                                color = if (pagerState.currentPage == 0) theme.primaryTextColor else theme.secondaryTextColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Window 2 Tab Button
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .background(
                                color = if (pagerState.currentPage == 1) theme.accentColor.copy(alpha = 0.22f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (pagerState.currentPage == 1) 1.dp else 0.dp,
                                color = if (pagerState.currentPage == 1) theme.accentColor else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            }
                            .padding(vertical = 8.dp)
                            .testTag("tab_window_alarms"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Alarms",
                                tint = if (pagerState.currentPage == 1) theme.accentColor else theme.secondaryTextColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ALARMS & HABITS ($activeAlarmsCount)",
                                color = if (pagerState.currentPage == 1) theme.primaryTextColor else theme.secondaryTextColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // HORIZONTAL PAGER: WINDOW 1 & WINDOW 2
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    when (page) {
                        // WINDOW 1: TASKS & REMINDERS MANAGER
                        0 -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Search & Add Task Input Bar
                                AddTaskBar(
                                    onAddTask = { title -> viewModel.addTask(title) },
                                    onOpenCreateMenu = { showCreateMenuSheet = true }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Section Tracking Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ACTIVE TASKS",
                                        color = theme.accentColor.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                    Text(
                                        text = "$activeCount REMAINING",
                                        color = theme.secondaryTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Tasks List
                                if (tasks.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .frostedCyanStyle(
                                                cornerRadius = theme.cardCornerRadius,
                                                borderWidth = theme.cardBorderWidth,
                                                backgroundColor = if (theme.isMonochrome) Color(0xFF161616) else theme.surfaceColor,
                                                borderColor = if (theme.isMonochrome) Color(0xFF2C2C2C) else theme.cardBorderColor.copy(alpha = 0.3f),
                                                glowColor = if (theme.isMonochrome) Color.Transparent else theme.cardGlowColor.copy(alpha = 0.1f)
                                            )
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "NO TASKS YET",
                                                color = theme.accentColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Type above to add your first goal or task.",
                                                color = theme.secondaryTextColor,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .testTag("task_list"),
                                        contentPadding = PaddingValues(bottom = 80.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(
                                            items = tasks,
                                            key = { it.id }
                                        ) { task ->
                                            TaskItemRow(
                                                task = task,
                                                isSelectionMode = isTaskSelectionMode,
                                                isSelected = selectedTaskIds.contains(task.id),
                                                onToggle = { item ->
                                                    if (isTaskSelectionMode) {
                                                        selectedTaskIds = if (selectedTaskIds.contains(item.id)) {
                                                            selectedTaskIds - item.id
                                                        } else {
                                                            selectedTaskIds + item.id
                                                        }
                                                    } else {
                                                        viewModel.toggleTask(item)
                                                    }
                                                },
                                                onLongPress = { item ->
                                                    selectedTaskIds = if (selectedTaskIds.contains(item.id)) {
                                                        selectedTaskIds - item.id
                                                    } else {
                                                        selectedTaskIds + item.id
                                                    }
                                                },
                                                onOpenMenu = { item ->
                                                    selectedTaskForMenu = item
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // WINDOW 2: TIMED ALARMS & HABITS HUB
                        1 -> {
                            AlarmsHabitsScreen(
                                alarms = alarms,
                                isSelectionMode = isAlarmSelectionMode,
                                selectedAlarmIds = selectedAlarmIds,
                                onToggleSelectAlarm = { alarmId ->
                                    selectedAlarmIds = if (selectedAlarmIds.contains(alarmId)) {
                                        selectedAlarmIds - alarmId
                                    } else {
                                        selectedAlarmIds + alarmId
                                    }
                                },
                                onEnterSelectionMode = { alarmId ->
                                    selectedAlarmIds = if (selectedAlarmIds.contains(alarmId)) {
                                        selectedAlarmIds - alarmId
                                    } else {
                                        selectedAlarmIds + alarmId
                                    }
                                },
                                onToggleAlarm = { item, enabled -> viewModel.toggleAlarm(item, enabled) },
                                onEditAlarm = { item ->
                                    alarmToEdit = item
                                    alarmDialogTypeToOpen = item.type
                                },
                                onDeleteAlarm = { item -> viewModel.deleteAlarm(item) },
                                onAddAlarmClick = { type ->
                                    alarmToEdit = null
                                    alarmDialogTypeToOpen = type
                                },
                                onRequestAlarmPermissions = {
                                    showOverlayPermissionDialog = true
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 3. BOTTOM ACTION BAR: MULTI-MODE PLUS (+) BUTTON & SETTINGS OR SMOOTH SELECTION BUBBLE
            if (isTaskSelectionMode && pagerState.currentPage == 0) {
                val allTasksSelected = selectedTaskIds.size == tasks.size && tasks.isNotEmpty()
                TaskSelectionBubbleBar(
                    selectedCount = selectedTaskIds.size,
                    isAllSelected = allTasksSelected,
                    onToggleSelectAll = {
                        selectedTaskIds = if (allTasksSelected) {
                            emptySet()
                        } else {
                            tasks.map { it.id }.toSet()
                        }
                    },
                    onCompleteSelected = {
                        viewModel.completeTasks(selectedTaskIds, true)
                        selectedTaskIds = emptySet()
                    },
                    onDeleteSelected = {
                        viewModel.deleteTasks(selectedTaskIds)
                        selectedTaskIds = emptySet()
                    },
                    onCancelSelection = {
                        selectedTaskIds = emptySet()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                )
            } else if (isAlarmSelectionMode && pagerState.currentPage == 1) {
                val allAlarmsSelected = selectedAlarmIds.size == alarms.size && alarms.isNotEmpty()
                AlarmSelectionBubbleBar(
                    selectedCount = selectedAlarmIds.size,
                    isAllSelected = allAlarmsSelected,
                    onToggleSelectAll = {
                        selectedAlarmIds = if (allAlarmsSelected) {
                            emptySet()
                        } else {
                            alarms.map { it.id }.toSet()
                        }
                    },
                    onToggleEnabledSelected = { enabled ->
                        viewModel.toggleAlarmsEnabled(selectedAlarmIds, enabled)
                        selectedAlarmIds = emptySet()
                    },
                    onDeleteSelected = {
                        viewModel.deleteAlarms(selectedAlarmIds)
                        selectedAlarmIds = emptySet()
                    },
                    onCancelSelection = {
                        selectedAlarmIds = emptySet()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                )
            } else {
                val btnBg = if (theme.isMonochrome) Color(0xFF1E1E1E) else theme.surfaceColor
                val btnBorder = if (theme.isMonochrome) Color(0xFF444444) else theme.cardBorderColor
                val btnGlow = if (theme.isMonochrome) Color.Transparent else theme.cardGlowColor

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 6.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Main '+' Multi-Mode Creation Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .frostedCyanStyle(
                                cornerRadius = 27.dp,
                                borderWidth = 1.5.dp,
                                backgroundColor = if (theme.isMonochrome) Color(0xFF242424) else theme.accentColor.copy(alpha = 0.25f),
                                borderColor = if (theme.isMonochrome) Color.White else theme.accentColor,
                                glowColor = btnGlow,
                                glowRadius = if (theme.isMonochrome) 0.dp else 14.dp
                            )
                            .clip(CircleShape)
                            .clickable { showCreateMenuSheet = true }
                            .testTag("main_plus_menu_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create New Item",
                            tint = if (theme.isMonochrome) Color.White else theme.accentColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Polished "SETTINGS" Button with Gear/Cog Icon
                    Box(
                        modifier = Modifier
                            .height(54.dp)
                            .frostedCyanStyle(
                                cornerRadius = 27.dp,
                                borderWidth = 1.5.dp,
                                backgroundColor = btnBg,
                                borderColor = btnBorder,
                                glowColor = btnGlow,
                                glowRadius = if (theme.isMonochrome) 0.dp else 12.dp
                            )
                            .clip(RoundedCornerShape(27.dp))
                            .clickable { showSettingsDialog = true }
                            .padding(horizontal = 14.dp)
                            .testTag("settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings Icon",
                                tint = theme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SETTINGS",
                                color = theme.primaryTextColor,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. DIALOGS, SHEETS & OVERLAYS

        // '+' Multi-Mode Bottom Sheet Menu
        if (showCreateMenuSheet) {
            CreateModeBottomSheet(
                onDismiss = { showCreateMenuSheet = false },
                onSelectOption = { option ->
                    showCreateMenuSheet = false
                    when (option) {
                        CreateOption.REGULAR_TASK -> {
                            showQuickAddTaskDialog = true
                        }
                        CreateOption.NORMAL_ALARM -> {
                            alarmToEdit = null
                            alarmDialogTypeToOpen = AlarmType.NORMAL
                        }
                        CreateOption.HABIT_ALARM -> {
                            alarmToEdit = null
                            alarmDialogTypeToOpen = AlarmType.HABIT
                        }
                        CreateOption.CALENDAR_ALARM -> {
                            alarmToEdit = null
                            alarmDialogTypeToOpen = AlarmType.CALENDAR
                        }
                    }
                }
            )
        }

        // Quick Add Task Dialog (When selected from the multi-mode menu)
        if (showQuickAddTaskDialog) {
            QuickAddTaskDialog(
                onDismiss = { showQuickAddTaskDialog = false },
                onAddTask = { title ->
                    viewModel.addTask(title)
                    showQuickAddTaskDialog = false
                }
            )
        }

        // Add / Edit Alarm Dialog (Normal, Habit, Calendar)
        alarmDialogTypeToOpen?.let { type ->
            AddEditAlarmDialog(
                initialAlarm = alarmToEdit,
                alarmType = type,
                onDismiss = {
                    alarmDialogTypeToOpen = null
                    alarmToEdit = null
                },
                onSave = { alarm ->
                    if (alarm.id == 0L) {
                        viewModel.addAlarm(alarm)
                    } else {
                        viewModel.updateAlarm(alarm)
                    }
                    alarmDialogTypeToOpen = null
                    alarmToEdit = null
                }
            )
        }

        // Task Action Menu Dialog (on long-press)
        selectedTaskForMenu?.let { task ->
            TaskActionMenuDialog(
                task = task,
                onDismiss = { selectedTaskForMenu = null },
                onOpenTimePicker = { item ->
                    selectedTaskForMenu = null
                    selectedTaskForTimePicker = item
                },
                onClearReminder = { item ->
                    viewModel.clearTaskReminder(item)
                    selectedTaskForMenu = null
                },
                onToggleAutoUntick = { item, enabled ->
                    viewModel.toggleAutoUntickTomorrow(item, enabled)
                },
                onEditTask = { item ->
                    selectedTaskForMenu = null
                    selectedTaskForEdit = item
                },
                onDeleteTask = { item ->
                    viewModel.deleteTask(item)
                    selectedTaskForMenu = null
                }
            )
        }

        // Time & Date Picker Dialog
        selectedTaskForTimePicker?.let { task ->
            TaskTimePickerDialog(
                task = task,
                onDismiss = { selectedTaskForTimePicker = null },
                onConfirmTime = { triggerMillis, toneName ->
                    viewModel.setTaskReminder(task, triggerMillis, toneName)
                    selectedTaskForTimePicker = null
                }
            )
        }

        // Edit Task Title Dialog
        selectedTaskForEdit?.let { task ->
            EditTaskDialog(
                task = task,
                onDismiss = { selectedTaskForEdit = null },
                onSave = { item, newTitle ->
                    viewModel.updateTaskTitle(item, newTitle)
                    selectedTaskForEdit = null
                }
            )
        }

        // Settings Dialog (Theme, Background Import, Daily Notifications, Diagnostics)
        if (showSettingsDialog) {
            SettingsDialog(
                settings = settings,
                onDismiss = { showSettingsDialog = false },
                onSelectThemeMode = { mode ->
                    viewModel.setThemeMode(mode)
                },
                onSaveCustomTheme = { bgColor, taskColor, normalAlarmColor, habitAlarmColor, calendarAlarmColor, accentColor, transparency, cornerRadius, borderWidth ->
                    viewModel.setCustomThemeSettings(
                        bgColor = bgColor,
                        taskColor = taskColor,
                        normalAlarmColor = normalAlarmColor,
                        habitAlarmColor = habitAlarmColor,
                        calendarAlarmColor = calendarAlarmColor,
                        accentColor = accentColor,
                        transparency = transparency,
                        cornerRadius = cornerRadius,
                        borderWidth = borderWidth
                    )
                },
                onSelectBackgroundType = { type, uri ->
                    viewModel.setBackgroundType(type, uri)
                },
                onSetMorningReminder = { enabled, h, m ->
                    viewModel.setMorningReminder(enabled, h, m)
                },
                onSetEveningReminder = { enabled, h, m ->
                    viewModel.setEveningReminder(enabled, h, m)
                },
                onSendTestNotification = {
                    viewModel.sendTestNotification()
                },
                hasNotificationPermission = hasNotificationPermission,
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onOpenAlarmPermissions = {
                    showSettingsDialog = false
                    showOverlayPermissionDialog = true
                }
            )
        }

        // Lock Screen & Overlay Permission Dialog
        if (showOverlayPermissionDialog) {
            LockScreenOverlayPermissionDialog(
                onDismiss = { showOverlayPermissionDialog = false },
                onContinueToBattery = {
                    showOverlayPermissionDialog = false
                    showBatteryOptimizationDialog = true
                }
            )
        }

        // Battery Optimization Reminder Dialog
        if (showBatteryOptimizationDialog) {
            BatteryOptimizationDialog(
                onDismiss = { showBatteryOptimizationDialog = false }
            )
        }
    }
}


