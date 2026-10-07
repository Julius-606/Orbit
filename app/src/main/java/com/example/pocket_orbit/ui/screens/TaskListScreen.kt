// ==========================================
// IDENTITY: The Ledger / Task List View
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/TaskListScreen.kt
// VERSION: 1.0.5 | SYSTEM: Orbit Life-OS
// VIBE: Fixed imports and downgraded PullToRefresh to match project BOM. 🎯
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.pocket_orbit.data.StudyTaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: DashboardViewModel, // Using the existing ViewModel
    modifier: Modifier = Modifier
) {
    // Gathering our state from the VM
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Pull-to-refresh state for Material3 1.2.0 (BOM 2024.02.00)
    val pullToRefreshState = rememberPullToRefreshState()

    // Trigger refresh when the user pulls down
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.syncTasks()
        }
    }

    // Keep the UI state in sync with the ViewModel state
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pendingTasks) { task ->
                TaskItem(task = task)
            }
        }

        // PullToRefreshContainer is the standard for M3 1.2.0
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun TaskItem(task: StudyTaskEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (task.subject.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}