// ==========================================
// IDENTITY: The Brain / Orbit Repository
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/OrbitRepository.kt
// VERSION: 1.0.1
// ==========================================

package com.example.pocket_orbit.data

import android.util.Log
import com.example.pocket_orbit.network.ApiService
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
                Log.e("OrbitRepo", "VM rejected us. Opps activity? Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.w("OrbitRepo", "Network error. Relying on offline vault. Safaricom acting up again? 📵")
        }
    }

    // 🔥 THE FIX: Executes the 'Take Profit' locally first, then syncs with the VM Brain
    suspend fun markTaskComplete(taskId: Int) {
        try {
            // 1. Instant UI update (Offline First mentality)
            studyTaskDao.markTaskCompleted(taskId)
            Log.d("OrbitRepo", "Task $taskId marked complete locally. Good stuff.")

            // 2. Tell the VM we secured the W
            val response = apiService.completeTask("Bearer $secretToken", taskId)
            if (response.isSuccessful) {
                Log.d("OrbitRepo", "VM acknowledges the W! Task $taskId fully completed.")
            } else {
                Log.w("OrbitRepo", "VM sync failed for completion. Will retry next sync cycle.")
            }
        } catch (e: Exception) {
            Log.e("OrbitRepo", "Failed to reach VM to mark complete: ${e.message}")
        }
    }
}