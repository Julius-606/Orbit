// ==========================================
// IDENTITY: The Translator / Chat ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/ChatViewModel.kt
// VERSION: 1.0.0 | SYSTEM: Orbit (The Life-OS Protocol)
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket_orbit.model.ChatMessage
import com.example.pocket_orbit.model.ChatRequest
import com.example.pocket_orbit.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val apiService: ApiService
) : ViewModel() {

    // Holds the list of messages. The UI observes this.
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory

    // Tells the UI when the VM is "typing"
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Your secret vault key (keep it matching your VM's .env)
    private val secretToken = "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4"

    fun sendMessage(text: String) {
        if (text.isBlank()) return // Don't send empty orders to the market

        // 1. Immediately show the user's message in the UI (Zero slippage)
        val userMsg = ChatMessage(text = text, isFromUser = true)
        _chatHistory.value = _chatHistory.value + userMsg

        // 2. Turn on the loading indicator
        _isLoading.value = true

        // 3. Fire the request to the FastAPI VM in the background
        viewModelScope.launch {
            try {
                val response = apiService.converseWithOrbit(
                    token = "Bearer $secretToken",
                    request = ChatRequest(message = text)
                )

                if (response.isSuccessful && response.body() != null) {
                    // W Secured! Orbit replied.
                    val orbitReply = response.body()!!.reply
                    _chatHistory.value = _chatHistory.value + ChatMessage(text = orbitReply, isFromUser = false)
                } else {
                    // VM rejected it. 
                    _chatHistory.value = _chatHistory.value + ChatMessage("Orbit's brain rejected the request. Are you over-leveraging? 💀", isFromUser = false)
                }
            } catch (e: Exception) {
                // Safaricom acting up again...
                _chatHistory.value = _chatHistory.value + ChatMessage("Network error. Can't reach the VM. Go touch grass. 📵", isFromUser = false)
            } finally {
                // Turn off the loading indicator
                _isLoading.value = false
            }
        }
    }
}
