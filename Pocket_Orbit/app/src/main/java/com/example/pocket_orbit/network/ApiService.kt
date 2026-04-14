// ==========================================
// IDENTITY: The Courier / Retrofit API Service
// FILEPATH: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/network/ApiService.kt
// VERSION: 1.1.0
// VIBE: Added TaskCompletionRequest to sync remarks with the VM Brain. 📡
// ==========================================

package com.example.pocket_orbit.network

import com.example.pocket_orbit.data.StudyTaskEntity
import com.example.pocket_orbit.model.ChatRequest
import com.example.pocket_orbit.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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

    @PUT("api/v1/study/tasks/{task_id}/complete")
    suspend fun completeTask(
        @Header("Authorization") token: String,
        @Path("task_id") taskId: Int,
        @Body request: TaskCompletionRequest
    ): Response<Unit>

    @POST("api/v1/orbit/converse")
    suspend fun converseWithOrbit(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}

data class TaskCompletionRequest(
    val remarks: String?
)

data class VibeResponse(
    val governor_says: String
)