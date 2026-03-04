// ==========================================
// IDENTITY: The Courier / Retrofit API Service
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/ApiService.kt
// VERSION: 1.0.1
// ==========================================

package com.example.pocket_orbit.network

import com.example.pocket_orbit.data.StudyTaskEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("api/v1/study/tasks/pending")
    suspend fun getPendingTasks(
        @Header("Authorization") token: String
    ): Response<List<StudyTaskEntity>>

    @GET("api/v1/tasks/current-vibe")
    suspend fun getCurrentVibe(
        @Header("Authorization") token: String
    ): Response<VibeResponse>

    // 🔥 THE FIX: New endpoint mapping to tell the VM the task is dusted
    @PUT("api/v1/study/tasks/{task_id}/complete")
    suspend fun completeTask(
        @Header("Authorization") token: String,
        @Path("task_id") taskId: Int
    ): Response<Unit>
}

data class VibeResponse(
    val governor_says: String
)