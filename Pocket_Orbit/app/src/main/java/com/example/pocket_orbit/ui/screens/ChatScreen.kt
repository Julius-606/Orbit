// ================================================================================
// FILE: app/src/main/java/com/example/pocket_orbit/ui/screens/ChatScreen.kt
// VERSION: 4.1.1 | SYSTEM: Orbit (The Life-OS Protocol)
// IDENTITY: The Neural Interface / Chat UI with Persistent Memory & Offline Staging
// ================================================================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket_orbit.data.ChatMessageEntity
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel 
) {
    var inputText by remember { mutableStateOf("") }
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pendingOfflineMessage by viewModel.pendingOfflineMessage.collectAsState()
    
    val context = LocalContext.current
    
    // Simple state-based check for initial render, though a Flow would be better for real-time
    var isOnline by remember { mutableStateOf(isNetworkAvailable(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Orbit AI 🪐", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isOnline) "Online • Life Coach Mode" else "Offline • Local Memory Only", 
                            fontSize = 12.sp, 
                            color = if (isOnline) Color(0xFF00FFCC) else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                items(chatHistory.reversed()) { message ->
                    ChatBubble(message)
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask Orbit: 'What should I do now?'") },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                isOnline = isNetworkAvailable(context)
                                viewModel.sendMessage(inputText, isOffline = !isOnline)
                                inputText = ""
                            }
                        },
                        containerColor = if (isOnline) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.Send else Icons.Default.CloudOff,
                            contentDescription = "Send", 
                            tint = Color.Black
                        )
                    }
                }
            }
        }
        
        // 🔥 Offline Staging Dialog
        pendingOfflineMessage?.let { text ->
            AlertDialog(
                onDismissRequest = { viewModel.discardPendingMessage() },
                title = { Text("You're Offline") },
                text = { Text("Orbit's brain is in the cloud. Should I stage this message to send when you're back online?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.stageMessage(text) }) {
                        Text("STAGE")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.discardPendingMessage() }) {
                        Text("IGNORE")
                    }
                }
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val backgroundColor = when {
        message.isStaged -> MaterialTheme.colorScheme.tertiaryContainer // Waiting for sync
        message.isFromUser -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    
    val textColor = if (message.isFromUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start) {
            Surface(
                shape = shape,
                color = backgroundColor,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 15.sp
                )
            }
            if (message.isStaged) {
                Text(
                    text = "Pending Sync...",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

// Helper to check network state
fun isNetworkAvailable(context: android.content.Context): Boolean {
    val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}
