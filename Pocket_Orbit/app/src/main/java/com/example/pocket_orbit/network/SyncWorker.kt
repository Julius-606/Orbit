// ==========================================
// IDENTITY: The Ghost Runner / WorkManager SyncWorker
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/SyncWorker.kt
// VERSION: 2.0.0 | SYSTEM: Unified Multi-Session Task & Chat Reconciler
// VIBE: Syncing offline wins and history sessions while you sleep. 👻✨
// ==========================================

package com.example.pocket_orbit.network

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.pocket_orbit.data.AppDatabase
import com.example.pocket_orbit.data.ChatMessageEntity
import com.example.pocket_orbit.model.ChatRequest
import com.example.pocket_orbit.model.ChatMessageHistory

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val apiService = RetrofitClient.apiService
        val secretToken = "Bearer 3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4"

        Log.d("SyncWorker", "🪐 Orbit background sync initiated across full system clusters...")

        return try {
            // 1. Reconcile Completed Tasks with VM
            val unsyncedTasks = database.studyTaskDao().getUnsyncedCompletions()
            unsyncedTasks.forEach { task ->
                val response = apiService.completeTask(
                    secretToken, 
                    task.id, 
                    TaskCompletionRequest(task.remarks)
                )
                if (response.isSuccessful) {
                    Log.d("SyncWorker", "Successfully synced completion for task: ${task.id}")
                }
            }

            // 2. Fetch fresh pending task arrays from VM to update offline state
            val tasksResponse = apiService.getPendingTasks(secretToken)
            if (tasksResponse.isSuccessful && tasksResponse.body() != null) {
                database.studyTaskDao().clearTasks()
                database.studyTaskDao().insertTasks(tasksResponse.body()!!)
                Log.d("SyncWorker", "Secured fresh pending tasks from VM successfully.")
            }

            // 3. Process Staged Chat Messages across sessions
            val stagedMessages = database.chatDao().getStagedMessages()
            stagedMessages.forEach { msg ->
                // Safe fallbacks to keep context intact
                val response = apiService.converseWithOrbit(
                    token = secretToken, 
                    request = ChatRequest(
                        message = "[STAGED] ${msg.text}",
                        history = emptyList() // Simple stateless processing on sync worker thread
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    database.chatDao().markMessageSynced(msg.id)
                    // Insert Jarvis' response reply directly into its parent thread session layout!
                    val orbitReply = response.body()!!.reply
                    database.chatDao().insertMessage(
                        ChatMessageEntity(
                            sessionId = msg.sessionId,
                            text = orbitReply,
                            isFromUser = false
                        )
                    )
                    Log.d("SyncWorker", "Staged message ${msg.id} synchronized cleanly.")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Cluster network reconciliation slippage: ${e.message}")
            Result.retry()
        }
    }
}
