// ==========================================
// IDENTITY: The Blueprints / Room DB Entities
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/Entities.kt
// VERSION: 2.0.0 | SYSTEM: Session-Based Persistent Conversations
// VIBE: Added ChatSessionEntity and linked ChatMessageEntity to sessionId. 🧠✨
// ==========================================

package com.example.pocket_orbit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val subject: String,
    val brainRotLevel: String, // "chill", "mid", "cooked"
    val isCompleted: Boolean,
    val dueDate: Date?,
    val remarks: String? = null,
    val isReminder: Boolean = false
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String, // Session unique ID (e.g. UUID string or timestamp)
    val title: String,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String = "default_session", // 🔥 Group messages into sessions
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStaged: Boolean = false
)

@Entity(tableName = "forex_logs")
data class ForexLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pair: String,
    val action: String, // "TP_HIT", "SL_HIT"
    val pnl: Double,
    val timestamp: Date
)

// ==========================================
// 🖥️ TERMINAL PILOT & VS CODE SESSION ENTITIES
// ==========================================

@Entity(tableName = "terminal_sessions")
data class TerminalSessionEntity(
    @PrimaryKey val id: String, // Unique UUID or session ID
    val name: String, // e.g. "1: bash", "2: node", "Setup Procedure"
    val nodeName: String = "HF_Space_Node",
    val shellType: String = "bash", // "bash", "powershell", "zsh"
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "terminal_log_entries")
data class TerminalLogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val command: String,
    val output: String,
    val exitCode: Int = 0,
    val cwd: String = "",
    val prompt: String = "$ ",
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0
)

@Entity(tableName = "terminal_pilot_chats")
data class TerminalPilotChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val userGoal: String,
    val aiExplanation: String,
    val recommendedCommandsJson: String = "[]", // Serialized list of SuggestedCommandItem
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "terminal_command_vault")
data class TerminalCommandVaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val command: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)
