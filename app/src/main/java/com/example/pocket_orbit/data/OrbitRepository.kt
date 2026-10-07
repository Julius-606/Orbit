// ==========================================
// IDENTITY: The Brain / Orbit Repository
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/OrbitRepository.kt
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
                if (newTasks.isNotEmpty()) {
                    studyTaskDao.clearTasks()
                    studyTaskDao.insertTasks(newTasks)
                }
                Log.d("OrbitRepo", "Sync successful. Fresh tasks secured: ${newTasks.size}")
            } else {
                if (response.code() == 404) {
                    Log.w("OrbitRepo", "Backend endpoint offline or inactive (HTTP 404). Check if the Ngrok tunnel or Hugging Face Space is active. Relying on local offline tasks.")
                } else {
                    Log.w("OrbitRepo", "Backend returned code: ${response.code()}. Relying on local offline tasks.")
                }
            }
        } catch (e: Exception) {
            Log.w("OrbitRepo", "Network unreachable (${e.message}). Operating in offline-first mode.")
        }
    }

    suspend fun markTaskComplete(taskId: Int) {
        try {
            studyTaskDao.markTaskCompleted(taskId, "Completed from mobile")
            Log.d("OrbitRepo", "Task $taskId marked complete locally.")

            val response = apiService.completeTask("Bearer $secretToken", taskId, TaskCompletionRequest("Completed from mobile"))
            if (response.isSuccessful) {
                Log.d("OrbitRepo", "VM acknowledges completion for task $taskId.")
            } else {
                Log.w("OrbitRepo", "VM sync failed for completion. Will retry next sync cycle.")
            }
        } catch (e: Exception) {
            Log.e("OrbitRepo", "Failed to reach VM to mark complete: ${e.message}")
        }
    }
}
