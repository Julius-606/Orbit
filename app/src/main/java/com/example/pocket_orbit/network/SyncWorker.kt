// ==========================================
// IDENTITY: The Background Courier / SyncWorker
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/SyncWorker.kt
// ==========================================

package com.example.pocket_orbit.network

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pocket_orbit.BuildConfig
import com.example.pocket_orbit.data.AppDatabase
import com.example.pocket_orbit.data.OrbitRepository

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repo = OrbitRepository(
                studyTaskDao = db.studyTaskDao(),
                apiService = RetrofitClient.apiService,
                secretToken = BuildConfig.ORBIT_SECRET_TOKEN
            )
            repo.refreshTasksFromVM()
            Log.d("SyncWorker", "Orbit periodic sync completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Periodic sync error: ${e.message}")
            Result.retry()
        }
    }
}
