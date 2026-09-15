// ================================================================================
// FILE: app/src/main/java/com/example/pocket_orbit/ui/screens/ChatScreen.kt
// VERSION: 5.0.0 | SYSTEM: Session-Based Multi-Thread Conversations & Markdown Rendering
// IDENTITY: The Neural Interface / Chat UI with Persistent Sessions list & Markdown support.
// ================================================================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
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
import com.example.pocket_orbit.data.ChatSessionEntity
import kotlinx.coroutines.launch
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    var inputText by remember { mutableStateOf("") }
    val chatHistory by viewModel.chatHistory.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pendingOfflineMessage by viewModel.pendingOfflineMessage.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    var isOnline by remember { mutableStateOf(isNetworkAvailable(context)) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(260.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chat Threads 🧠", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { 
                        viewModel.createNewSession("New Session ${chatSessions.size + 1}") 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "New Session")
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(chatSessions) { session ->
                        val isSelected = session.id == currentSessionId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.selectSession(session.id)
                                    scope.launch { drawerState.close() }
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = session.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            val activeTitle = chatSessions.find { it.id == currentSessionId }?.title ?: "Orbit AI 🪐"
                            Text(activeTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = if (isOnline) "Online • Life Coach Mode" else "Offline • Local Memory Only", 
                                fontSize = 11.sp, 
                                color = if (isOnline) Color(0xFF00FFCC) else Color.Gray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Toggle Sidebar Threads")
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
            
            // Offline Staging Dialog
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
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val backgroundColor = when {
        message.isStaged -> MaterialTheme.colorScheme.tertiaryContainer
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
                Box(modifier = Modifier.padding(12.dp)) {
                    if (message.isFromUser) {
                        Text(text = message.text, color = textColor, fontSize = 15.sp)
                    } else {
                        // Render full formatted markdown responses from Orbit AI
                        MarkdownText(text = message.text, color = textColor)
                    }
                }
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

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

