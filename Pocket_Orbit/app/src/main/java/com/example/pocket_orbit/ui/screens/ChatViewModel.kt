// ==========================================
// IDENTITY: The Translator / Chat ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/ChatViewModel.kt
// VERSION: 1.2.0 | SYSTEM: Orbit (The Life-OS Protocol)
// VIBE: Short-term dementia cured. Memory is now fully persistent and contextual. 🧠
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket_orbit.data.ChatDao
import com.example.pocket_orbit.data.ChatMessageEntity
import com.example.pocket_orbit.model.ChatMessageHistory
import com.example.pocket_orbit.model.ChatRequest
import com.example.pocket_orbit.network.ApiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(
    private val apiService: ApiService,
    private val chatDao: ChatDao
) : ViewModel() {

    // Persistent history from Room
    val chatHistory: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _pendingOfflineMessage = MutableStateFlow<String?>(null)
    val pendingOfflineMessage = _pendingOfflineMessage.asStateFlow()

    private val secretToken = "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4"

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
            val stagedMsg = ChatMessageEntity(
                text = text,
                isFromUser = true,
                isStaged = true
            )
            chatDao.insertMessage(stagedMsg)
            _pendingOfflineMessage.value = null
            
            chatDao.insertMessage(ChatMessageEntity(
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
            // 1. Get current history for context (last 10 messages for token efficiency)
            val currentHistory = chatHistory.value.takeLast(10).map {
                ChatMessageHistory(
                    role = if (it.isFromUser) "user" else "model",
                    content = it.text
                )
            }

            // 2. Save user message to DB
            chatDao.insertMessage(ChatMessageEntity(text = text, isFromUser = true))
            
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
                    chatDao.insertMessage(ChatMessageEntity(text = orbitReply, isFromUser = false))
                } else {
                    chatDao.insertMessage(ChatMessageEntity(text = "Orbit's brain is offline. Check the VM logs. 💀", isFromUser = false))
                }
            } catch (e: Exception) {
                chatDao.insertMessage(ChatMessageEntity(text = "Network slippage. Message wasn't sent. 📵", isFromUser = false))
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun syncStagedMessages() {
        viewModelScope.launch {
            val staged = chatDao.getStagedMessages()
            staged.forEach { msg ->
                try {
                    val response = apiService.converseWithOrbit(
                        token = "Bearer $secretToken",
                        request = ChatRequest(message = "[STAGED] ${msg.text}")
                    )
                    if (response.isSuccessful) {
                        chatDao.markMessageSynced(msg.id)
                        response.body()?.reply?.let { reply ->
                            chatDao.insertMessage(ChatMessageEntity(text = reply, isFromUser = false))
                        }
                    }
                } catch (e: Exception) {
                    // Still offline
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatDao.clearHistory()
        }
    }
}