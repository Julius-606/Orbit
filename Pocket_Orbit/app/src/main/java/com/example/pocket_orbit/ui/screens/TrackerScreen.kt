// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/ui/screens/TrackerScreen.kt
// VERSION: 4.3.0 | SYSTEM: Orbit (The Life-OS Protocol)
// IDENTITY: The Ledger / All Tasks View
// VIBE: Sorting secured. Empty list bug fixed. 🎯
// ================================================================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.pocket_orbit.data.StudyTaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(viewModel: DashboardViewModel) {
    // 🔥 Collecting the actual state from the ViewModel
    val pendingTasks by viewModel.pendingTasks.collectAsState()

    // 🔥 SORTING ENGINE: Cooked > Mid > Chill
    val sortedTasks = remember(pendingTasks) {
        val brainRotPriority = mapOf("cooked" to 1, "mid" to 2, "chill" to 3)
        pendingTasks.sortedWith(
            compareBy<StudyTaskEntity> { brainRotPriority[it.brainRotLevel.lowercase()] ?: 4 }
                .thenBy { it.dueDate?.time ?: Long.MAX_VALUE }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Ledger \uD83D\uDDC2️", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Upcoming Grinds \uD83D\uDD25", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    // Refresh button to pull latest from Neon.tech/VM
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Text("\uD83D\uDD04", fontSize = 16.sp)
                    }
                }
            }

            if (sortedTasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ledger is empty.", color = Color.Gray)
                        Text("Orbit is awaiting your next command. 🧠", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshData() }) {
                            Text("SYNC WITH NEON")
                        }
                    }
                }
            } else {
                items(sortedTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onClick = { viewModel.markTaskCompleted(task.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun TaskCard(task: StudyTaskEntity, alpha: Float = 1.0f, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.subject.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2196F3)
                    )
                    
                    if (task.isReminder) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFFEB3B).copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "REMINDER",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBC02D)
                            )
                        }
                    }
                }
                
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Brain Rot Visual Indicator
            val rotColor = when (task.brainRotLevel.lowercase()) {
                "cooked" -> Color(0xFFFF5252)
                "mid" -> Color(0xFFFFB74D)
                else -> Color(0xFF81C784)
            }
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(rotColor, shape = androidx.compose.foundation.shape.CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            if (task.isReminder) {
                Icon(Icons.Default.Notifications, contentDescription = "Reminder", tint = Color.Gray)
            } else {
                Icon(Icons.Default.Assignment, contentDescription = "Task", tint = Color.Gray)
            }
        }
    }
}