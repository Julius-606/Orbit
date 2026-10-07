// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/ui/screens/TrackerScreen.kt
// VERSION: 4.1.2 | SYSTEM: Orbit (The Life-OS Protocol)
// IDENTITY: The Ledger / All Tasks View
// VIBE: Dropped the mock data. Now pulling real grinds from the DB. ✅
// ================================================================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    // 🔥 PULLING REAL DATA FROM THE VAULT!
    val pendingTasks by viewModel.pendingTasks.collectAsState()

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
                Text("Upcoming Grinds \uD83D\uDD25", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            if (pendingTasks.isEmpty()) {
                item {
                    Text(
                        "All caught up! Head over to the Assistant tab to have Orbit assign you some Med School pain. 🧠",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(pendingTasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = { viewModel.markTaskCompleted(task.id) }
                    )
                }
            }

            // NOTE: Completed tasks aren't currently fetched by the DAO.
            // But once you add a 'getCompletedTasks' query to Room, they'd go right here!
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun TaskCard(task: StudyTaskEntity, alpha: Float = 1.0f, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clickable { onClick() }, // Tap to secure the W
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
                Text(
                    text = task.subject,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3) // Hardcoded to Med blue for now
                )
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
            }

            if (task.isCompleted) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color(0xFF4CAF50))
            } else {
                Icon(Icons.Default.List, contentDescription = "Pending", tint = Color.Gray)
            }
        }
    }
}