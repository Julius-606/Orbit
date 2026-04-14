// ==========================================
// IDENTITY: The Guards / Room DB DAOs
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/Daos.kt
// VERSION: 1.2.0
// VIBE: Added ChatDao for persistent memory and offline staging. 🧠
// ==========================================

package com.example.pocket_orbit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    @Query("SELECT * FROM study_tasks WHERE isCompleted = 0")
    fun getPendingTasks(): Flow<List<StudyTaskEntity>>

    @Query("DELETE FROM study_tasks")
    suspend fun clearTasks()

    @Query("UPDATE study_tasks SET isCompleted = 1, remarks = :remarks WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: Int, remarks: String?)

    @Query("SELECT * FROM study_tasks WHERE isCompleted = 1 AND remarks IS NOT NULL")
    suspend fun getUnsyncedCompletions(): List<StudyTaskEntity>
}

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE isStaged = 1")
    suspend fun getStagedMessages(): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET isStaged = 0 WHERE id = :messageId")
    suspend fun markMessageSynced(messageId: Int)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface ForexLogDao {
    @Insert
    suspend fun insertLog(log: ForexLogEntity)

    @Query("SELECT * FROM forex_logs ORDER BY timestamp DESC LIMIT 10")
    fun getRecentLogs(): Flow<List<ForexLogEntity>>
}