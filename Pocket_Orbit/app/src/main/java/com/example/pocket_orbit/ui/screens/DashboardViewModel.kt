// ==========================================
// IDENTITY: The Translator / ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/DashboardViewModel.kt
// VERSION: 1.0.1
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket_orbit.data.OrbitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: OrbitRepository
) : ViewModel() {

    val pendingTasks = repository.pendingTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.refreshTasksFromVM()
        }
    }

    // 🔥 THE FIX: Passing the completion event down to the repo
    fun markTaskCompleted(taskId: Int) {
        viewModelScope.launch {
            repository.markTaskComplete(taskId)
        }
    }
}