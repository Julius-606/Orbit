// ==========================================
// IDENTITY: The Brain / Orbit Repository
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/OrbitRepository.kt
// VERSION: 1.1.0
// VIBE: Syncing remarks and handling offline-first task completion. 🧠
// ==========================================

package com.example.pocket_orbit.data

import android.util.Log
import com.example.pocket_orbit.network.ApiService
import com.example.pocket_orbit.network.TaskCompletionRequest
import kotlinx.coroutines.flow.Flow

class OrbitRepository(
    private val studyTaskDao: StudyTaskDao,
    private val apiService: ApiService,
    private val secretToken: String
) {
    val pendingTasks: Flow<List<StudyTaskEntity>> = studyTaskDao.getPendingTasks()

    suspend fun refreshTasksFromVM() {
        try {
            val response = apiService.getPendingTasks("Bearer $secretToken")

            if (response.isSuccessful && response.body() != null) {
                val newTasks = response.body()!!
                studyTaskDao.clearTasks()
                studyTaskDao.insertTasks(newTasks)
                Log.d("OrbitRepo", "Sync successful. Fresh tasks secured. 🎯")
            } else {
                Log.e("OrbitRepo", "VM rejected us. Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.w("OrbitRepo", "Network error. Relying on offline vault. 📵")
        }
    }

    // 🔥 UPDATED: Now handles remarks and attempts sync immediately
    suspend fun markTaskComplete(taskId: Int, remarks: String?) {
        try {
            // 1. Instant UI update (Offline First)
            studyTaskDao.markTaskCompleted(taskId, remarks)
            Log.d("OrbitRepo", "Task $taskId marked complete locally with remarks: $remarks")

            // 2. Sync with VM Brain
            val response = apiService.completeTask(
                "Bearer $secretToken", 
                taskId, 
                TaskCompletionRequest(remarks = remarks)
            )
            
            if (response.isSuccessful) {
                Log.d("OrbitRepo", "VM sync successful for task $taskId.")
            } else {
                Log.w("OrbitRepo", "VM sync failed (Code: ${response.code()}). Will retry later.")
            }
        } catch (e: Exception) {
            Log.e("OrbitRepo", "Offline mode: Failed to reach VM for task $taskId. Stored locally.")
        }
    }
    
    // TODO: Implement a background worker to sync getUnsyncedCompletions()
}