// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/ui/screens/TrackerScreen.kt
// VERSION: 4.4.0 | SYSTEM: Orbit (The Life-OS Protocol)
// IDENTITY: The Ledger / All Tasks View
// VIBE: Task vs Reminder distinction secured. 🎯
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
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape

import com.example.pocket_orbit.data.StudyTaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(viewModel: DashboardViewModel) {
    val pendingTasks by viewModel.pendingTasks.collectAsState()

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
                title = { Text("Orbit Ledger 📔", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
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
                    Text("Daily Grind", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Text("🔄", fontSize = 16.sp)
                    }
                }
            }

            if (sortedTasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("All clear. Go touch some grass. 🌿", color = Color.Gray)
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
        }
    }
}

@Composable
fun TaskCard(task: StudyTaskEntity, onClick: () -> Unit = {}) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Type Badge
                    Surface(
                        color = if (task.isReminder) Color(0xFFFFD54F) else Color(0xFF2196F3),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (task.isReminder) "REMINDER" else "TASK",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.subject.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                
                Text(
                    text = task.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                task.dueDate?.let {
                    Text(
                        text = "Due: ${dateFormat.format(it)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Brain Rot Indicator
            val rotColor = when (task.brainRotLevel.lowercase()) {
                "cooked" -> Color(0xFFFF5252)
                "mid" -> Color(0xFFFFB74D)
                else -> Color(0xFF81C784)
            }
            
            Icon(
                imageVector = if (task.isReminder) Icons.Default.Notifications else Icons.Default.Assignment,
                contentDescription = null,
                tint = rotColor.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
