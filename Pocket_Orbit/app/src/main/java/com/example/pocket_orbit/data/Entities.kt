// ==========================================
// IDENTITY: The Blueprints / Room DB Entities
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/Entities.kt
// VERSION: 1.3.0
// VIBE: Added Reply support for Chat messages. 🧠💬
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

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStaged: Boolean = false,
    val replyToId: Int? = null, // 🔥 ID of the message being replied to
    val replyToText: String? = null // 🏎️ Cache the text for UI speed
)

@Entity(tableName = "forex_logs")
data class ForexLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pair: String,
    val action: String, // "TP_HIT", "SL_HIT"
    val pnl: Double,
    val timestamp: Date
)
