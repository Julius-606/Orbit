// ==========================================
// IDENTITY: The Translator / Session-Based Chat ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/ChatViewModel.kt
// VERSION: 2.0.0 | SYSTEM: Multi-session interactive AI history recall
// VIBE: Full support for jumping between distinct persistent chat threads. 🧠🧬
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket_orbit.data.ChatDao
import com.example.pocket_orbit.data.ChatMessageEntity
import com.example.pocket_orbit.data.ChatSessionEntity
import com.example.pocket_orbit.model.ChatMessageHistory
import com.example.pocket_orbit.model.ChatRequest
import com.example.pocket_orbit.network.ApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val apiService: ApiService,
    private val chatDao: ChatDao
) : ViewModel() {

    // Active session ID tracker
    private val _currentSessionId = MutableStateFlow("default_session")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    // Available sessions
    val chatSessions: StateFlow<List<ChatSessionEntity>> = chatDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Message list attached specifically to current session
    @OptIn(ExperimentalCoroutinesApi::class)
    val chatHistory: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { sessionId -> chatDao.getMessagesForSession(sessionId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _pendingOfflineMessage = MutableStateFlow<String?>(null)
    val pendingOfflineMessage = _pendingOfflineMessage.asStateFlow()

    private val secretToken = "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4"

    init {
        // Ensure default session exists
        viewModelScope.launch {
            chatDao.insertSession(ChatSessionEntity("default_session", "Jarvis Brainstorm 🪐"))
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun createNewSession(title: String) {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            chatDao.insertSession(ChatSessionEntity(id = newId, title = title))
            _currentSessionId.value = newId
        }
    }

    fun sendMessage(text: String, isOffline: Boolean = false) {
        if (text.isBlank()) return

        if (isOffline) {
            _pendingOfflineMessage.value = text
            return
        }

        executeSendMessage(text)
    }

    fun stageMessage(text: String) {
        viewModelScope.launch {
            val sessionId = _currentSessionId.value
            val stagedMsg = ChatMessageEntity(
                sessionId = sessionId,
                text = text,
                isFromUser = true,
                isStaged = true
            )
            chatDao.insertMessage(stagedMsg)
            _pendingOfflineMessage.value = null
            
            chatDao.insertMessage(ChatMessageEntity(
                sessionId = sessionId,
                text = "Message staged. I'll process this as soon as we're back online. Stay focused. 🫡",
                isFromUser = false
            ))
        }
    }

    fun discardPendingMessage() {
        _pendingOfflineMessage.value = null
    }

    private fun executeSendMessage(text: String) {
        viewModelScope.launch {
            val sessionId = _currentSessionId.value
            val now = System.currentTimeMillis()

            // 1. Get current session history context
            val currentHistory = chatHistory.value.takeLast(10).map {
                ChatMessageHistory(
                    role = if (it.isFromUser) "user" else "model",
                    content = it.text
                )
            }

            // 2. Save user message to DB under current session
            chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, text = text, isFromUser = true, timestamp = now))
            chatDao.updateSessionTimestamp(sessionId, now)
            
            _isLoading.value = true
            try {
                // 3. Send to VM with full memory context
                val response = apiService.converseWithOrbit(
                    token = "Bearer $secretToken",
                    request = ChatRequest(
                        message = text,
                        history = currentHistory
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val orbitReply = response.body()!!.reply
                    chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, text = orbitReply, isFromUser = false))
                } else {
                    chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, text = "Orbit's brain is offline. Check the VM logs. 💀", isFromUser = false))
                }
            } catch (e: Exception) {
                chatDao.insertMessage(ChatMessageEntity(sessionId = sessionId, text = "Network slippage. Message wasn't sent. 📵", isFromUser = false))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatDao.clearSessionHistory(_currentSessionId.value)
        }
    }
}
