// ==========================================
// IDENTITY: The Ghost Runner / WorkManager SyncWorker
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/SyncWorker.kt
// VERSION: 1.1.0
// VIBE: Syncing offline wins while you sleep. 👻
// ==========================================

package com.example.pocket_orbit.network

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.pocket_orbit.data.AppDatabase
import com.example.pocket_orbit.model.ChatRequest

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val apiService = RetrofitClient.apiService
        val secretToken = "Bearer 3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4"

        Log.d("SyncWorker", "Orbit background sync initiated...")

        return try {
            // 1. Sync completed tasks
            val unsyncedTasks = database.studyTaskDao().getUnsyncedCompletions()
            unsyncedTasks.forEach { task ->
                val response = apiService.completeTask(
                    secretToken, 
                    task.id, 
                    TaskCompletionRequest(task.remarks)
                )
                if (response.isSuccessful) {
                    Log.d("SyncWorker", "Synced task ${task.id}")
                }
            }

            // 2. Sync staged chat messages
            val stagedMessages = database.chatDao().getStagedMessages()
            stagedMessages.forEach { msg ->
                val response = apiService.converseWithOrbit(
                    secretToken, 
                    ChatRequest(msg.text)
                )
                if (response.isSuccessful) {
                    database.chatDao().markMessageSynced(msg.id)
                    // Note: Optionally insert Orbit's reply to the database here
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed: ${e.message}")
            Result.retry()
        }
    }
}