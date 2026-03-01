// ==========================================
// IDENTITY: The Courier / Retrofit API Service
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/ApiService.kt
// COMPONENT: Android Networking
// ROLE: The standard REST interface. When you pull-to-refresh, this guy fetches the data.
// VIBE: "Hey VM, got any new Med School tasks for me?" 📦
// ==========================================

package com.example.pocket_orbit.network

import com.example.pocket_orbit.data.StudyTaskEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    // Fetches your pending study tasks from the FastAPI backend
    @GET("api/v1/study/tasks/pending")
    suspend fun getPendingTasks(
        @Header("Authorization") token: String
    ): Response<List<StudyTaskEntity>>

    // Fetches the Governor's current vibe recommendation
    @GET("api/v1/tasks/current-vibe")
    suspend fun getCurrentVibe(
        @Header("Authorization") token: String
    ): Response<VibeResponse>
}

data class VibeResponse(
    val governor_says: String
)