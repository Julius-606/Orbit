// ==========================================
// IDENTITY: The Brain / Orbit Repository
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/OrbitRepository.kt
// ==========================================

package com.example.pocket_orbit.data

import android.util.Log
import com.example.pocket_orbit.network.ApiService
import com.example.pocket_orbit.network.TaskCompletionRequest
import com.example.pocket_orbit.network.RetrofitClient
import kotlinx.coroutines.flow.Flow

class OrbitRepository(
    private val studyTaskDao: StudyTaskDao,
    apiService: ApiService? = null,
    private val secretToken: String = ""
) {
    private val apiService: ApiService get() = RetrofitClient.apiService

    val pendingTasks: Flow<List<StudyTaskEntity>> = studyTaskDao.getPendingTasks()
    val allTasks: Flow<List<StudyTaskEntity>> = studyTaskDao.getAllTasks()

    suspend fun refreshTasksFromVM() {
        try {
            val response = apiService.getPendingTasks("Bearer $secretToken")

            if (response.isSuccessful && response.body() != null) {
                val newTasks = response.body()!!.data
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

    suspend fun createTask(
        title: String,
        subject: String,
        brainRotLevel: String,
        dueDate: java.util.Date?,
        isReminder: Boolean,
        remarks: String?
    ): StudyTaskEntity {
        val newId = (System.currentTimeMillis() % 10000000).toInt()
        val task = StudyTaskEntity(
            id = newId,
            title = title,
            subject = subject.ifBlank { "General Med" },
            brainRotLevel = brainRotLevel.lowercase(),
            isCompleted = false,
            dueDate = dueDate,
            remarks = remarks,
            isReminder = isReminder
        )
        studyTaskDao.insertTask(task)
        Log.d("OrbitRepo", "Task $newId created locally: $title (Reminder: $isReminder)")

        try {
            val isoDate = dueDate?.let {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                sdf.format(it)
            }
            val payload = mapOf(
                "title" to title,
                "subject" to subject,
                "brainRotLevel" to brainRotLevel,
                "isReminder" to isReminder,
                "remarks" to remarks,
                "dueDate" to isoDate
            )
            apiService.createStudyTask("Bearer $secretToken", payload)
            Log.d("OrbitRepo", "Created task synced to Orbit Brain.")
        } catch (e: Exception) {
            Log.w("OrbitRepo", "Cloud sync for new task pending offline: ${e.message}")
        }
        return task
    }

    suspend fun updateTask(task: StudyTaskEntity) {
        try {
            studyTaskDao.insertTask(task)
            apiService.updateStudyTask("******", task.id, taskPayload(task))
            Log.d("OrbitRepo", "Task ${task.id} updated locally: ${task.title}")
        } catch (e: Exception) {
            Log.e("OrbitRepo", "Error updating task ${task.id}: ${e.message}")
        }
    }

    suspend fun deleteTask(taskId: Int) {
        try {
            studyTaskDao.deleteTask(taskId)
            apiService.deleteStudyTask("******", taskId)
            Log.d("OrbitRepo", "Task $taskId deleted locally.")
        } catch (e: Exception) {
            Log.e("OrbitRepo", "Error deleting task $taskId: ${e.message}")
        }
    }

    private fun taskPayload(task: StudyTaskEntity): Map<String, Any?> {
        val isoDate = task.dueDate?.let {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(it)
        }
        return mapOf(
            "title" to task.title,
            "subject" to task.subject,
            "brain_rot_level" to task.brainRotLevel,
            "is_reminder" to task.isReminder,
            "remarks" to task.remarks,
            "due_date" to isoDate
        )
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
