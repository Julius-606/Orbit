// ==========================================
// IDENTITY: The Translator / ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/DashboardViewModel.kt
// COMPONENT: Android Architecture (MVVM)
// ROLE: Takes data from the Repository and feeds it to the UI. Survives screen rotations.
// VIBE: The calm, collected manager who doesn't panic when you rotate your phone. 📱🔄
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

    // The UI listens to this state. If the DB updates, the UI instantly redraws. W architecture.
    val pendingTasks = repository.pendingTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // As soon as the app opens, tell the repo to try and fetch fresh data from the VM
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            // We launch this in a coroutine so the UI doesn't freeze.
            // Never block the main thread, or the app crashes like a margin call.
            repository.refreshTasksFromVM()
        }
    }
}