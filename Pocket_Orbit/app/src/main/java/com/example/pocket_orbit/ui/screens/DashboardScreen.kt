// ================================================================================
// FILE: /Projects/Orbit/Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/ui/screens/DashboardScreen.kt
// VERSION: 3.1.5 | SYSTEM: Orbit (The Jarvis Protocol)
// ================================================================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.pocket_orbit.data.StudyTaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {

    val pendingTasks by viewModel.pendingTasks.collectAsState()
    val nextTask: StudyTaskEntity? = pendingTasks.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pocket Orbit 🪐",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync VM",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ------------------------------------------------------------------
            // FOREX GUARDIAN WIDGET
            // ------------------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Forex Guardian 📈", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("XAUUSD: Monitoring...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                    Badge(containerColor = Color(0xFF4CAF50)) {
                        Text("Active", modifier = Modifier.padding(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Med-Scholar Queue 🧠", fontWeight = FontWeight.Bold, fontSize = 20.sp)

            // ------------------------------------------------------------------
            // MED-SCHOLAR TASK CARD
            // ------------------------------------------------------------------
            if (nextTask != null) {
                val rotLevel = nextTask.brainRotLevel?.lowercase() ?: "chill"

                val (rotColor, rotText) = when(rotLevel) {
                    "cooked" -> Pair(Color(0xFFF44336), "Cooked (Brain is fried 💀)")
                    "mid" -> Pair(Color(0xFFFF9800), "Mid (Simmering 🔥)")
                    "chill" -> Pair(Color(0xFF4CAF50), "Chill (Chilling 🧊)")
                    else -> Pair(Color.Gray, "Unknown Territory")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = nextTask.subject ?: "General Med",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = rotColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = rotText,
                                    color = rotColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = nextTask.title ?: "Untitled Task",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        val dateText = nextTask.dueDate?.toString() ?: "No deadline. Just vibes."
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Due: $dateText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            // 🔥 THE FIX: Actually hitting the view model to secure the bag!
                            onClick = { viewModel.markTaskCompleted(nextTask.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Mark Completed W", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            } else {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "No Tasks",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Zero pending tasks.\nGo touch grass or analyze some MT5 charts. 📉✨",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}