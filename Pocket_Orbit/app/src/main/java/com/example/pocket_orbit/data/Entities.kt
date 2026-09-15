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
