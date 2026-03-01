// ==========================================
// IDENTITY: The Blueprints / Room DB Entities
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/Entities.kt
// COMPONENT: Android Offline Database
// ROLE: Defines the exact structure of your phone's memory.
// VIBE: Structuring data so you can still read Med notes when Safaricom bundles vanish. 📵
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
    val dueDate: Date?
)

@Entity(tableName = "forex_logs")
data class ForexLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pair: String,
    val action: String, // "TP_HIT", "SL_HIT"
    val pnl: Double,
    val timestamp: Date
)