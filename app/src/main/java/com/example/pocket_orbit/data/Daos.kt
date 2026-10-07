// ==========================================
// IDENTITY: The Guards / Room DB DAOs
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/Daos.kt
// VERSION: 2.0.0 | SYSTEM: Session-Based Chat History Queries
// VIBE: Expanded ChatDao to support persistent multiple chat sessions. 🧠⚡
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
    suspend fun insertSession(session: ChatSessionEntity)

    @Query("SELECT * FROM chat_sessions ORDER BY lastActiveTimestamp DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE isStaged = 1")
    suspend fun getStagedMessages(): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET isStaged = 0 WHERE id = :messageId")
    suspend fun markMessageSynced(messageId: Int)

    @Query("UPDATE chat_sessions SET lastActiveTimestamp = :timestamp WHERE id = :sessionId")
    suspend fun updateSessionTimestamp(sessionId: String, timestamp: Long)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearSessionHistory(sessionId: String)
}

@Dao
interface ForexLogDao {
    @Insert
    suspend fun insertLog(log: ForexLogEntity)

    @Query("SELECT * FROM forex_logs ORDER BY timestamp DESC LIMIT 10")
    fun getRecentLogs(): Flow<List<ForexLogEntity>>
}

@Dao
interface TerminalDao {
    // Sessions
    @Query("SELECT * FROM terminal_sessions ORDER BY lastActiveTimestamp DESC")
    fun getAllSessions(): Flow<List<TerminalSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TerminalSessionEntity)

    @Query("DELETE FROM terminal_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("UPDATE terminal_sessions SET lastActiveTimestamp = :timestamp WHERE id = :sessionId")
    suspend fun touchSession(sessionId: String, timestamp: Long)

    @Query("UPDATE terminal_sessions SET name = :newName WHERE id = :sessionId")
    suspend fun renameSession(sessionId: String, newName: String)

    // Logs & Terminal History
    @Query("SELECT * FROM terminal_log_entries WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getLogsForSession(sessionId: String): Flow<List<TerminalLogEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(entry: TerminalLogEntryEntity)

    @Query("DELETE FROM terminal_log_entries WHERE sessionId = :sessionId")
    suspend fun clearLogsForSession(sessionId: String)

    @Query("SELECT DISTINCT command FROM terminal_log_entries WHERE command != '' ORDER BY timestamp DESC LIMIT 50")
    fun getRecentCommands(): Flow<List<String>>

    // User + AI Pilot Chats in Auto Mode
    @Query("SELECT * FROM terminal_pilot_chats WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPilotChatsForSession(sessionId: String): Flow<List<TerminalPilotChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPilotChat(chat: TerminalPilotChatEntity)

    @Query("DELETE FROM terminal_pilot_chats WHERE sessionId = :sessionId")
    suspend fun clearPilotChatsForSession(sessionId: String)

    // Command Vault
    @Query("SELECT * FROM terminal_command_vault ORDER BY timestamp DESC")
    fun getVaultCommands(): Flow<List<TerminalCommandVaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultCommand(cmd: TerminalCommandVaultEntity)

    @Query("DELETE FROM terminal_command_vault WHERE id = :id")
    suspend fun deleteVaultCommand(id: Int)
}
