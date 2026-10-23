// ================================================================================
// FILE: app/src/main/java/com/example/pocket_orbit/ui/screens/DashboardScreen.kt
// VERSION: 4.0.0 | SYSTEM: Orbit Assistant Command Deck
// IDENTITY: Full-on Assistant Dashboard with Calendar, Markers, and Task/Reminder Hub
// ================================================================================

package com.example.pocket_orbit.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket_orbit.data.StudyTaskEntity
import com.example.pocket_orbit.network.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current

    // Reactive State from ViewModel
    val allTasks by viewModel.allTasks.collectAsState()
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val displayedYear by viewModel.displayedYear.collectAsState()
    val displayedMonth by viewModel.displayedMonth.collectAsState()
    val selectedDateKey by viewModel.selectedDateKey.collectAsState()
    val tasksByDateMap by viewModel.tasksByDateMap.collectAsState()
    val selectedDateTasks by viewModel.selectedDateTasks.collectAsState()

    // Dialog state
    var showCreateDialog by remember { mutableStateOf(false) }
    var createDialogIsReminder by remember { mutableStateOf(false) }
    var createDialogTargetDate by remember { mutableStateOf<Date?>(null) }
    var taskToEdit by remember { mutableStateOf<StudyTaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<StudyTaskEntity?>(null) }

    // Next top-priority focus task
    val nextPriorityTask = remember(pendingTasks) {
        pendingTasks.sortedWith(
            compareBy<StudyTaskEntity> {
                when (it.brainRotLevel.lowercase()) {
                    "cooked" -> 0
                    "mid" -> 1
                    else -> 2
                }
            }.thenBy { it.dueDate?.time ?: Long.MAX_VALUE }
        ).firstOrNull()
    }

    // Dynamic greeting based on time of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning, Commander 🌅"
            in 12..16 -> "Good afternoon, Scholar ☀️"
            in 17..21 -> "Good evening, Commander 🌆"
            else -> "Late Night Protocol Active 🌌"
        }
    }

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        sdf.format(Date())
    }

    // Active stat metrics
    val activeTasksCount = remember(pendingTasks) { pendingTasks.count { !it.isReminder } }
    val remindersCount = remember(allTasks) { allTasks.count { it.isReminder && !it.isCompleted } }
    val completedCount = remember(allTasks) { allTasks.count { it.isCompleted } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🪐", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Orbit Assistant",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "All Systems Online",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.selectToday() }) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Jump to Today",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { viewModel.syncTasks() }) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync VM",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    createDialogIsReminder = false
                    createDialogTargetDate = null
                    showCreateDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Action", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Assistant Welcome Banner
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = todayFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 2. Metrics Quick Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricMiniCard(
                        title = "Tasks",
                        count = activeTasksCount.toString(),
                        icon = Icons.Default.Assignment,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Reminders",
                        count = remindersCount.toString(),
                        icon = Icons.Default.Alarm,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Done",
                        count = completedCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Focus",
                        count = if (activeTasksCount == 0) "100%" else "${maxOf(20, 100 - activeTasksCount * 12)}%",
                        icon = Icons.Default.Bolt,
                        accentColor = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Quick Action Shortcut Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionChipButton(
                        text = "+ Add Task",
                        icon = Icons.Default.AddCircleOutline,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        createDialogIsReminder = false
                        createDialogTargetDate = null
                        showCreateDialog = true
                    }

                    ActionChipButton(
                        text = "+ Set Reminder",
                        icon = Icons.Default.NotificationsActive,
                        color = MaterialTheme.colorScheme.tertiary
                    ) {
                        createDialogIsReminder = true
                        createDialogTargetDate = null
                        showCreateDialog = true
                    }

                    ActionChipButton(
                        text = "Sync Cloud",
                        icon = Icons.Default.CloudSync,
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        viewModel.syncTasks()
                        Toast.makeText(context, "Syncing with Orbit Cloud...", Toast.LENGTH_SHORT).show()
                    }

                    ActionChipButton(
                        text = "Test Alert 🔔",
                        icon = Icons.Default.NotificationsActive,
                        color = Color(0xFF00E676)
                    ) {
                        NotificationHelper.sendTestNotification(context)
                        Toast.makeText(context, "Test notification triggered!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // 4. Interactive Calendar with Markers
            item {
                AssistantCalendarCard(
                    displayedYear = displayedYear,
                    displayedMonth = displayedMonth,
                    selectedDateKey = selectedDateKey,
                    tasksByDateMap = tasksByDateMap,
                    onPrevMonth = { viewModel.changeMonth(-1) },
                    onNextMonth = { viewModel.changeMonth(1) },
                    onSelectDate = { y, m, d -> viewModel.setSelectedDate(y, m, d) },
                    onTodayClick = { viewModel.selectToday() }
                )
            }

            // 5. Selected Day's Agenda
            item {
                val selectedLabel = remember(selectedDateKey) {
                    try {
                        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val date = sdfIn.parse(selectedDateKey)
                        if (date != null) {
                            val sdfOut = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
                            val isToday = DashboardViewModel.formatDateKey(Date()) == selectedDateKey
                            if (isToday) "Today • ${sdfOut.format(date)}" else sdfOut.format(date)
                        } else selectedDateKey
                    } catch (e: Exception) {
                        selectedDateKey
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(
                                containerColor = if (selectedDateTasks.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${selectedDateTasks.size}",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                try {
                                    val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    createDialogTargetDate = sdfIn.parse(selectedDateKey)
                                } catch (e: Exception) {
                                    createDialogTargetDate = null
                                }
                                createDialogIsReminder = false
                                showCreateDialog = true
                            }
                        ) {
                            Text("+ Add for Date")
                        }
                    }

                    if (selectedDateTasks.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelfImprovement,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No tasks or alerts on this date",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Clear horizon. Schedule a grind or rest your mind.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            selectedDateTasks.forEach { task ->
                                AgendaTaskItem(
                                    task = task,
                                    onToggleComplete = {
                                        viewModel.markTaskCompleted(task.id, context)
                                    },
                                    onEdit = {
                                        taskToEdit = task
                                    },
                                    onDelete = {
                                        taskToDelete = task
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 6. Next Priority Focus Hero Card
            if (nextPriorityTask != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Next Critical Focus ⚡",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val rotLevel = nextPriorityTask.brainRotLevel.lowercase()
                        val (rotColor, rotBadge) = when (rotLevel) {
                            "cooked" -> Pair(Color(0xFFF44336), "Cooked 💀")
                            "mid" -> Pair(Color(0xFFFF9800), "Mid 🔥")
                            else -> Pair(Color(0xFF4CAF50), "Chill 🧊")
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = nextPriorityTask.subject,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = rotColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = rotBadge,
                                            color = rotColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = nextPriorityTask.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                if (!nextPriorityTask.remarks.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = nextPriorityTask.remarks,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val dueStr = remember(nextPriorityTask.dueDate) {
                                    nextPriorityTask.dueDate?.let {
                                        SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault()).format(it)
                                    } ?: "No explicit deadline"
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (nextPriorityTask.isReminder) Icons.Default.Alarm else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Due: $dueStr",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        viewModel.markTaskCompleted(nextPriorityTask.id, context)
                                        Toast.makeText(context, "Completed! Objective cleared ✅", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mark Complete & Clear W", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 7. Forex Guardian Pulse
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Forex Guardian Pulse 📈",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("XAUUSD / Gold Engine", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("1H / 15M Breakout Scanner Active", fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF4CAF50)
                            ) {
                                Text(
                                    text = "Monitoring",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Setup Dialog
    if (showCreateDialog) {
        CreateTaskReminderDialog(
            initialDate = createDialogTargetDate,
            isReminderDefault = createDialogIsReminder,
            onDismiss = { showCreateDialog = false },
            onSave = { title, subject, brainRot, dueDate, isRem, remarks ->
                viewModel.createTask(
                    title = title,
                    subject = subject,
                    brainRotLevel = brainRot,
                    dueDate = dueDate,
                    isReminder = isRem,
                    remarks = remarks,
                    context = context
                )
            }
        )
    }

    // Edit Task / Reminder Dialog
    if (taskToEdit != null) {
        val target = taskToEdit!!
        CreateTaskReminderDialog(
            taskToEdit = target,
            onDismiss = { taskToEdit = null },
            onSave = { title, subject, brainRot, dueDate, isRem, remarks ->
                viewModel.updateTask(
                    task = target.copy(
                        title = title,
                        subject = subject,
                        brainRotLevel = brainRot,
                        dueDate = dueDate,
                        isReminder = isRem,
                        remarks = remarks
                    ),
                    context = context
                )
                taskToEdit = null
            }
        )
    }

    // Confirm Delete Dialog for Tasks/Reminders
    if (taskToDelete != null) {
        val target = taskToDelete!!
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Entry?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${target.title}'?\nAny pending alarms and notifications for this item will be cancelled.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(target.id, context)
                        taskToDelete = null
                        Toast.makeText(context, "Deleted from schedule", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { taskToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// CALENDAR COMPONENT WITH MULTI-COLOR MARKERS
// ==========================================
@Composable
fun AssistantCalendarCard(
    displayedYear: Int,
    displayedMonth: Int,
    selectedDateKey: String,
    tasksByDateMap: Map<String, List<StudyTaskEntity>>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (year: Int, month: Int, day: Int) -> Unit,
    onTodayClick: () -> Unit
) {
    val monthName = remember(displayedMonth, displayedYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.YEAR, displayedYear)
        }
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    val daysInMonth = remember(displayedMonth, displayedYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeekOffset = remember(displayedMonth, displayedYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        // Calendar.SUNDAY is 1, so offset is (dayOfWeek - 1)
        cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Header Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevMonth, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of Week Labels
            val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayHeaders.forEachIndexed { index, label ->
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (index == 0 || index == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val totalCells = firstDayOfWeekOffset + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (rowIndex in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (colIndex in 0 until 7) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - firstDayOfWeekOffset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val isToday = (displayedYear == todayYear && displayedMonth == todayMonth && dayNumber == todayDay)
                                val dateKey = String.format(Locale.US, "%04d-%02d-%02d", displayedYear, displayedMonth + 1, dayNumber)
                                val isSelected = dateKey == selectedDateKey
                                val dayTasks = tasksByDateMap[dateKey] ?: emptyList()

                                CalendarDayCell(
                                    dayNumber = dayNumber,
                                    isToday = isToday,
                                    isSelected = isSelected,
                                    tasks = dayTasks,
                                    onClick = { onSelectDate(displayedYear, displayedMonth, dayNumber) }
                                )
                            } else {
                                // Empty spacer cell
                                Box(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            // Legend / Marker Guide
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarkerLegendItem(color = Color(0xFFF44336), label = "Cooked")
                MarkerLegendItem(color = Color(0xFFFF9800), label = "Mid")
                MarkerLegendItem(color = Color(0xFF4CAF50), label = "Chill")
                MarkerLegendItem(color = MaterialTheme.colorScheme.tertiary, label = "Reminder")
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    tasks: List<StudyTaskEntity>,
    onClick: () -> Unit
) {
    val cellShape = CircleShape

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(38.dp)
            .clip(cellShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = cellShape
            )
            .clickable { onClick() }
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "$dayNumber",
            fontSize = 12.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        // Dot markers for tasks / reminders on this day
        if (tasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hasCooked = tasks.any { it.brainRotLevel.equals("cooked", ignoreCase = true) }
                val hasMid = tasks.any { it.brainRotLevel.equals("mid", ignoreCase = true) }
                val hasChill = tasks.any { it.brainRotLevel.equals("chill", ignoreCase = true) }
                val hasReminder = tasks.any { it.isReminder }

                val dotColorList = mutableListOf<Color>()
                if (hasCooked) dotColorList.add(Color(0xFFF44336))
                if (hasMid) dotColorList.add(Color(0xFFFF9800))
                if (hasChill && dotColorList.size < 2) dotColorList.add(Color(0xFF4CAF50))
                if (hasReminder && dotColorList.size < 3) dotColorList.add(MaterialTheme.colorScheme.tertiary)

                dotColorList.take(3).forEach { dotColor ->
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else dotColor)
                    )
                }
            }
        }
    }
}

@Composable
fun MarkerLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AgendaTaskItem(
    task: StudyTaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit
) {
    val rotLevel = task.brainRotLevel.lowercase()
    val rotColor = when (rotLevel) {
        "cooked" -> Color(0xFFF44336)
        "mid" -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Complete",
                        tint = if (task.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = rotColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = task.subject,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = rotColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (task.isReminder) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Reminder",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = task.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    task.dueDate?.let { due ->
                        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(due)
                        Text(
                            text = "Time: $timeStr",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    count: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActionChipButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = color
            )
        }
    }
}