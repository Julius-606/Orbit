// ==========================================
// IDENTITY: The Vocabulary / Chat Models
// FILEPATH: app/src/main/java/com/example/pocket_orbit/model/ChatModels.kt
// VERSION: 1.0.0 | SYSTEM: Orbit (The Life-OS Protocol)
// ==========================================

package com.example.pocket_orbit.model

// This is what the UI uses to draw the chat bubbles
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

// This is what we send to the FastAPI VM
data class ChatRequest(
    val message: String
)

// This is what the FastAPI VM replies with
data class ChatResponse(
    val reply: String,
    val status: String
)
