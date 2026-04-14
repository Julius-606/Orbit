// ==========================================
// IDENTITY: The Translator / ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/DashboardViewModel.kt
// VERSION: 1.1.0
// VIBE: Now handles task completion with remarks. 🧠
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket_orbit.data.OrbitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshTasksFromVM()
            _isRefreshing.value = false
        }
    }

    fun syncTasks() = refreshData()

    // 🔥 UPDATED: Accepting remarks from the UI
    fun markTaskCompleted(taskId: Int, remarks: String? = null) {
        viewModelScope.launch {
            repository.markTaskComplete(taskId, remarks)
        }
    }
}