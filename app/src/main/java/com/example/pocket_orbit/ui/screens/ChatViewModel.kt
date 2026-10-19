// ==========================================
// IDENTITY: The Neural Translator & Archival Vault / Chat ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/ChatViewModel.kt
// VERSION: 3.0.0 | SYSTEM: Orbit Persistent Chat Archival Engine
// VIBE: Multi-session chat history, Room database persistence, and Gemini cloud sync. 🧠🗄️
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket_orbit.BuildConfig
import com.example.pocket_orbit.data.ChatDao
import com.example.pocket_orbit.data.ChatMessageEntity
import com.example.pocket_orbit.data.ChatSessionEntity
import com.example.pocket_orbit.model.ChatMessage
import com.example.pocket_orbit.model.ChatRequest
import com.example.pocket_orbit.model.ChatHistoryMessage
import com.example.pocket_orbit.network.ApiService
import com.example.pocket_orbit.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(
    private val chatDao: ChatDao,
    apiService: ApiService? = null
) : ViewModel() {

    private val apiService: ApiService get() = RetrofitClient.apiService
    private val secretToken = BuildConfig.ORBIT_SECRET_TOKEN

    // All archived chat sessions ordered by latest active
    val allSessions: StateFlow<List<ChatSessionEntity>> = chatDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSessionId = MutableStateFlow<String>("default_orbit_session")
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    private val _activeSession = MutableStateFlow<ChatSessionEntity?>(null)
    val activeSession: StateFlow<ChatSessionEntity?> = _activeSession.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    // Backward-compatible flow for ChatMessage UI representation
    val chatHistory: StateFlow<List<ChatMessage>> = _messages.map { list ->
        list.map { ChatMessage(text = it.text, isFromUser = it.isFromUser) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var messagesJob: Job? = null

    init {
        initializeSessions()
    }

    private fun initializeSessions() {
        viewModelScope.launch {
            allSessions.collect { sessionsList ->
                if (sessionsList.isEmpty()) {
                    val defaultSession = ChatSessionEntity(
                        id = "default_orbit_session",
                        title = "Main Briefing 🪐",
                        lastActiveTimestamp = System.currentTimeMillis()
                    )
                    chatDao.insertSession(defaultSession)
                    // Insert welcome message
                    chatDao.insertMessage(
                        ChatMessageEntity(
                            sessionId = "default_orbit_session",
                            text = "Orbit AI online. Ready to optimize your syllabus, monitor Forex, and secure daily wins.",
                            isFromUser = false,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    switchSession("default_orbit_session")
                } else if (_activeSession.value == null) {
                    val target = sessionsList.find { it.id == _activeSessionId.value } ?: sessionsList.first()
                    switchSession(target.id)
                }
            }
        }
    }

    fun switchSession(sessionId: String) {
        _activeSessionId.value = sessionId
        _activeSession.value = allSessions.value.find { it.id == sessionId }
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatDao.getMessagesForSession(sessionId).collect { list ->
                _messages.value = list
            }
        }
    }

    fun createNewSession(customTitle: String? = null) {
        viewModelScope.launch {
            val newId = "session_${UUID.randomUUID().toString().take(8)}"
            val title = customTitle?.ifBlank { null } ?: run {
                val timeStr = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date())
                "Briefing ($timeStr)"
            }
            val newSession = ChatSessionEntity(
                id = newId,
                title = title,
                lastActiveTimestamp = System.currentTimeMillis()
            )
            chatDao.insertSession(newSession)
            // Welcome bubble
            chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = newId,
                    text = "New neural channel opened. What's on your agenda Commander?",
                    isFromUser = false,
                    timestamp = System.currentTimeMillis()
                )
            )
            switchSession(newId)
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            chatDao.renameSession(sessionId, newTitle.trim())
            if (_activeSessionId.value == sessionId) {
                _activeSession.value = _activeSession.value?.copy(title = newTitle.trim())
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatDao.clearSessionHistory(sessionId)
            chatDao.deleteSession(sessionId)
            val remaining = allSessions.value.filter { it.id != sessionId }
            if (remaining.isNotEmpty()) {
                switchSession(remaining.first().id)
            } else {
                createNewSession("Main Briefing")
            }
        }
    }

    fun clearCurrentSessionHistory() {
        val current = _activeSessionId.value
        viewModelScope.launch {
            chatDao.clearSessionHistory(current)
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        val currentId = _activeSessionId.value

        viewModelScope.launch {
            // Save user message to Room
            val userMsg = ChatMessageEntity(
                sessionId = currentId,
                text = trimmed,
                isFromUser = true,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(userMsg)
            chatDao.updateSessionTimestamp(currentId, System.currentTimeMillis())

            // Auto-rename session if it is still a default/new title and this is the first user query
            val currentTitle = _activeSession.value?.title ?: ""
            if (currentTitle.startsWith("Briefing") || currentTitle.startsWith("New") || currentTitle == "Main Briefing 🪐") {
                val generatedTitle = if (trimmed.length > 28) trimmed.take(28) + "..." else trimmed
                chatDao.renameSession(currentId, generatedTitle)
            }

            _isLoading.value = true

            try {
                val response = apiService.converseWithOrbit(
                    token = "Bearer $secretToken",
                    request = ChatRequest(
                        message = trimmed,
                        history = _messages.value.takeLast(20).map {
                            ChatHistoryMessage(
                                role = if (it.isFromUser) "user" else "model",
                                content = it.text
                            )
                        }
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val orbitReply = response.body()!!.reply
                    val botMsg = ChatMessageEntity(
                        sessionId = currentId,
                        text = orbitReply,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    chatDao.insertMessage(botMsg)
                } else {
                    val botMsg = ChatMessageEntity(
                        sessionId = currentId,
                        text = "Orbit Brain returned code ${response.code()}. Check local workstation tunnel connection.",
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    chatDao.insertMessage(botMsg)
                }
            } catch (e: Exception) {
                val botMsg = ChatMessageEntity(
                    sessionId = currentId,
                    text = "Offline Mode: Cannot reach backend VM at ${RetrofitClient.currentBaseUrl} (${e.message}). Saved locally in vault.",
                    isFromUser = false,
                    timestamp = System.currentTimeMillis()
                )
                chatDao.insertMessage(botMsg)
            } finally {
                _isLoading.value = false
                chatDao.updateSessionTimestamp(currentId, System.currentTimeMillis())
            }
        }
    }
}