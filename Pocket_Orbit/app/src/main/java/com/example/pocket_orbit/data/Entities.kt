// ==========================================
// IDENTITY: The Blueprints / Room DB Entities
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/Entities.kt
// VERSION: 1.2.0
// VIBE: Added ChatMessageEntity for offline memory and persistent conversations. 🧠
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
    val isReminder: Boolean = false // 🔥 Distinguish between Task and Reminder
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStaged: Boolean = false // 🔥 True if sent while offline and pending sync
)

@Entity(tableName = "forex_logs")
data class ForexLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pair: String,
    val action: String, // "TP_HIT", "SL_HIT"
    val pnl: Double,
    val timestamp: Date
)