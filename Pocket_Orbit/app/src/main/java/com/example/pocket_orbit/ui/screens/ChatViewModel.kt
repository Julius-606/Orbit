// ==========================================
// IDENTITY: The Translator / Chat ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/ChatViewModel.kt
// VERSION: 1.3.0 | SYSTEM: Orbit (The Life-OS Protocol)
// VIBE: Reply logic and advanced context secured. 🧠💬
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

    val chatHistory: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _replyingTo = MutableStateFlow<ChatMessageEntity?>(null)
    val replyingTo = _replyingTo.asStateFlow()

    private val _pendingOfflineMessage = MutableStateFlow<String?>(null)
    val pendingOfflineMessage = _pendingOfflineMessage.asStateFlow()

    private val secretToken = "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4"

    fun setReplyingTo(message: ChatMessageEntity?) {
        _replyingTo.value = message
    }

    fun sendMessage(text: String, isOffline: Boolean = false) {
        if (text.isBlank()) return
        if (isOffline) {
            _pendingOfflineMessage.value = text
            return
        }
        executeSendMessage(text)
    }

    private fun executeSendMessage(text: String) {
        viewModelScope.launch {
            val replyRef = _replyingTo.value
            _replyingTo.value = null // Clear reply state immediately for UI feel

            // 1. Get current history for context
            val currentHistory = chatHistory.value.takeLast(10).map {
                ChatMessageHistory(
                    role = if (it.isFromUser) "user" else "model",
                    content = it.text
                )
            }

            // 2. Save user message (with reply context if any)
            chatDao.insertMessage(ChatMessageEntity(
                text = text, 
                isFromUser = true,
                replyToId = replyRef?.id,
                replyToText = replyRef?.text
            ))
            
            _isLoading.value = true
            try {
                // 3. Construct the message with quoted context if replying
                val messageToSend = if (replyRef != null) {
                    "Regarding: \"${replyRef.text}\"\n\n$text"
                } else {
                    text
                }

                val response = apiService.converseWithOrbit(
                    token = "Bearer $secretToken",
                    request = ChatRequest(
                        message = messageToSend,
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

    fun stageMessage(text: String) {
        viewModelScope.launch {
            chatDao.insertMessage(ChatMessageEntity(text = text, isFromUser = true, isStaged = true))
            _pendingOfflineMessage.value = null
            chatDao.insertMessage(ChatMessageEntity(text = "Message staged. Sync pending. 🫡", isFromUser = false))
        }
    }

    fun discardPendingMessage() { _pendingOfflineMessage.value = null }

    fun clearHistory() {
        viewModelScope.launch { chatDao.clearHistory() }
    }
}
