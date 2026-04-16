// ==========================================
// IDENTITY: The Ledger / Task List View
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/TaskListScreen.kt
// VERSION: 1.2.1 | SYSTEM: Orbit Life-OS
// VIBE: Tinder-swipe secured with Remarks Pop-up. 🎯 (M3 1.3.0 PullToRefresh Refactor)
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket_orbit.data.StudyTaskEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    // State for the Remarks Dialog
    var showRemarksDialog by remember { mutableStateOf<StudyTaskEntity?>(null) }

    // 🔥 SORTING ENGINE: 1. Brain Rot Level (Cooked > Mid > Chill) | 2. Due Date
    val sortedTasks = remember(pendingTasks) {
        val brainRotPriority = mapOf("cooked" to 1, "mid" to 2, "chill" to 3)
        pendingTasks.sortedWith(
            compareBy<StudyTaskEntity> { brainRotPriority[it.brainRotLevel.lowercase()] ?: 4 }
                .thenBy { it.dueDate?.time ?: Long.MAX_VALUE }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.syncTasks() },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = sortedTasks,
                key = { it.id }
            ) { task ->
                SwipeToCompleteWrapper(
                    task = task,
                    onSwipe = { showRemarksDialog = task }
                )
            }
        }
        
        // 🔥 Remarks Dialog: The "Before it goes" reviews field
        showRemarksDialog?.let { task ->
            RemarksDialog(
                taskTitle = task.title,
                onDismiss = { showRemarksDialog = null },
                onConfirm = { remarks ->
                    viewModel.markTaskCompleted(task.id, remarks)
                    showRemarksDialog = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToCompleteWrapper(
    task: StudyTaskEntity,
    onSwipe: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd || it == SwipeToDismissBoxValue.EndToStart) {
                onSwipe()
                false // Reset so it doesn't disappear until VM confirms
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    else -> Color(0xFF4CAF50)
                }, label = "swipe_bg_color"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Complete Task",
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            TaskItem(task = task)
        }
    )
}

@Composable
fun RemarksDialog(
    taskTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Mark as Complete?") },
        text = {
            Column {
                Text(text = "How did '$taskTitle' go? Any review/remarks?")
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    placeholder = { Text("Optional remarks...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(remarks.ifBlank { null }) }) {
                Text("FINISH")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun TaskItem(task: StudyTaskEntity) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.subject.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold
                )
                
                val rotColor = when (task.brainRotLevel.lowercase()) {
                    "cooked" -> Color(0xFFFF5252)
                    "mid" -> Color(0xFFFFB74D)
                    else -> Color(0xFF81C784)
                }
                
                Surface(
                    color = rotColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.extraSmall,
                    border = androidx.compose.foundation.BorderStroke(1.dp, rotColor)
                ) {
                    Text(
                        text = task.brainRotLevel.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = rotColor,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (task.dueDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DUE: ${dateFormat.format(task.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
