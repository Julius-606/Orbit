// ==========================================
// IDENTITY: The Courier / Retrofit API Service
// FILEPATH: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/network/ApiService.kt
// VERSION: 1.3.0 | SYSTEM: Added Cluster Switching API Contracts
// VIBE: Added multi-node selection endpoints for Orbit workspace. 📡🖥️
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

    // 🖥️ TERMINAL PILOT ENDPOINTS
    @GET("api/v1/terminal/status")
    suspend fun getTerminalStatus(
        @Header("Authorization") token: String
    ): Response<TerminalStatusResponse>

    @POST("api/v1/terminal/execute")
    suspend fun executeCommand(
        @Header("Authorization") token: String,
        @Body request: TerminalCommandRequest
    ): Response<TerminalActionResponse>

    @POST("api/v1/terminal/suggest")
    suspend fun suggestCommand(
        @Header("Authorization") token: String,
        @Body request: TerminalSuggestionRequest
    ): Response<TerminalSuggestionResponse>

    @GET("api/v1/terminal/vault")
    suspend fun getVaultCommands(
        @Header("Authorization") token: String
    ): Response<TerminalVaultResponse>

    @POST("api/v1/terminal/vault/save")
    suspend fun saveToVault(
        @Header("Authorization") token: String,
        @Body request: VaultSaveRequest
    ): Response<TerminalActionResponse>

    @POST("api/v1/terminal/clear")
    suspend fun clearTerminal(
        @Header("Authorization") token: String
    ): Response<TerminalActionResponse>

    @POST("api/v1/terminal/nodes/connect")
    suspend fun connectNewNode(
        @Header("Authorization") token: String,
        @Body request: ConnectNodeRequest
    ): Response<TerminalActionResponse>

    @POST("api/v1/terminal/nodes/select")
    suspend fun selectActiveNode(
        @Header("Authorization") token: String,
        @Body request: SelectNodeRequest
    ): Response<TerminalActionResponse>
}

data class TaskCompletionRequest(
    val remarks: String?
)

data class VibeResponse(
    val governor_says: String
)

// Terminal Pilot Data Models
data class TelemetryData(
    val cpu: Double,
    val ram: Double,
    val disk: Double,
    val cwd: String
)

data class TerminalStatusResponse(
    val active_node: String,
    val nodes: List<String>,
    val telemetry: TelemetryData,
    val output: String
)

data class TerminalCommandRequest(
    val command: String
)

data class TerminalActionResponse(
    val status: String,
    val message: String? = null
)

data class TerminalSuggestionRequest(
    val user_goal: String
)

data class TerminalSuggestionResponse(
    val command: String,
    val explanation: String
)

data class VaultCommandItem(
    val id: Int,
    val name: String,
    val cmd: String,
    val category: String
)

data class TerminalVaultResponse(
    val commands: List<VaultCommandItem>
)

data class VaultSaveRequest(
    val name: String,
    val cmd: String,
    val category: String = "General"
)

data class ConnectNodeRequest(
    val name: String,
    val host: String,
    val port: Int = 8888
)

data class SelectNodeRequest(
    val name: String
)
