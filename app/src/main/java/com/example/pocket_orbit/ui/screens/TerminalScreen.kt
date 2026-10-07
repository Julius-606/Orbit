// ================================================================================
// FILE: app/src/main/java/com/example/pocket_orbit/ui/screens/TerminalScreen.kt
// VERSION: 3.0.0 | SYSTEM: Orbit VS Code Terminal & Auto Pilot Deck
// IDENTITY: Pixel-perfect VS Code terminal, persistent multi-sessions, live streams, and AI procedures.
// ================================================================================

package com.example.pocket_orbit.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket_orbit.data.TerminalCommandVaultEntity
import com.example.pocket_orbit.data.TerminalLogEntryEntity
import com.example.pocket_orbit.network.RetrofitClient
import com.example.pocket_orbit.data.TerminalSessionEntity
import com.example.pocket_orbit.network.SuggestedCommandItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// VS Code Color Palette
private val VsCodeDark = Color(0xFF1E1E1E)
private val VsCodeBlack = Color(0xFF181818)
private val VsCodeTabActive = Color(0xFF252526)
private val VsCodeTabInactive = Color(0xFF2D2D2D)
private val VsCodeBorder = Color(0xFF333333)
private val VsCodeAccentBlue = Color(0xFF007ACC)
private val VsCodeGreen = Color(0xFF4EC9B0)
private val VsCodeSuccessGreen = Color(0xFF89D185)
private val VsCodeYellow = Color(0xFFDCDCAA)
private val VsCodeOrange = Color(0xFFCE9178)
private val VsCodeErrorRed = Color(0xFFF48771)
private val VsCodeText = Color(0xFFCCCCCC)
private val VsCodeTextBright = Color(0xFFE0E0E0)
private val VsCodeCard = Color(0xFF252526)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(viewModel: TerminalViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    // State collections
    val sessions by viewModel.allSessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val sessionLogs by viewModel.sessionLogs.collectAsState()
    val sessionPilotChats by viewModel.sessionPilotChats.collectAsState()
    val recentCommands by viewModel.recentCommands.collectAsState()
    val vaultCommands by viewModel.localVaultCommands.collectAsState()

    val cpu by viewModel.cpu.collectAsState()
    val ram by viewModel.ram.collectAsState()
    val disk by viewModel.disk.collectAsState()
    val cwd by viewModel.cwd.collectAsState()
    val osType by viewModel.osType.collectAsState()
    val prompt by viewModel.prompt.collectAsState()
    val activeNode by viewModel.activeNode.collectAsState()
    val nodesList by viewModel.nodesList.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val currentSuggestion by viewModel.currentSuggestion.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()

    // Screen tab: 0 = Manual VS Code Terminal, 1 = Auto Pilot Mode
    var selectedDeckTab by remember { mutableIntStateOf(0) }

    // Manual input state & command history cycling
    var manualCommandInput by remember { mutableStateOf("") }
    var historyIndex by remember { mutableIntStateOf(-1) }

    // Auto pilot input state
    var autoPilotGoalInput by remember { mutableStateOf("") }
    var autoManualInlineCmd by remember { mutableStateOf("") }

    // Dialogs & Bottom Sheets
    var showNodeSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showVaultSheet by remember { mutableStateOf(false) }
    var showAddNodeDialog by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }
    var showRenameSessionDialog by remember { mutableStateOf(false) }
    var renameSessionName by remember { mutableStateOf("") }

    // Add node inputs
    var newNodeName by remember { mutableStateOf("") }
    var newNodeHost by remember { mutableStateOf("") }

    // New session inputs
    var newSessionCustomName by remember { mutableStateOf("") }
    var newSessionShell by remember { mutableStateOf("bash") }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(VsCodeTabActive)) {
                // Top Master Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Deck Title with Monospace Branding
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TERMINAL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = VsCodeAccentBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Active Node Pill with Status Dot
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2D2D2D),
                            modifier = Modifier.clickable { showNodeSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (connectionStatus == "Connected") VsCodeSuccessGreen
                                            else if (connectionStatus == "Connecting...") VsCodeYellow
                                            else VsCodeErrorRed
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeNode,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VsCodeTextBright
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Node",
                                    tint = VsCodeText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Mode Switcher: Manual vs Auto Pilot
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedDeckTab == 0) VsCodeAccentBlue else Color.Transparent,
                            modifier = Modifier.clickable { selectedDeckTab = 0 }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "Manual Terminal",
                                    tint = if (selectedDeckTab == 0) Color.White else VsCodeText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Terminal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedDeckTab == 0) Color.White else VsCodeText
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedDeckTab == 1) Color(0xFF6C5CE7) else Color.Transparent,
                            modifier = Modifier.clickable { selectedDeckTab = 1 }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "Auto Pilot",
                                    tint = if (selectedDeckTab == 1) Color.White else VsCodeText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Auto",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedDeckTab == 1) Color.White else VsCodeText
                                )
                            }
                        }
                    }

                    // Quick Action Icons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // History
                        IconButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Command History",
                                tint = VsCodeText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        // Vault
                        IconButton(
                            onClick = { showVaultSheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Saved Vault",
                                tint = VsCodeText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        // Clear Session
                        IconButton(
                            onClick = { viewModel.clearSessionTerminal() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Terminal",
                                tint = VsCodeText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // VS Code Session Tabs Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF252526))
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sessions.forEach { session ->
                        val isSelected = session.id == activeSessionId
                        Row(
                            modifier = Modifier
                                .background(if (isSelected) VsCodeDark else VsCodeTabInactive)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) VsCodeAccentBlue else Color.Transparent
                                )
                                .clickable { viewModel.switchSession(session.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = if (isSelected) VsCodeGreen else VsCodeText,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = session.name,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) VsCodeTextBright else VsCodeText
                            )

                            // Close session button if more than 1 session
                            if (sessions.size > 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Session",
                                    tint = VsCodeText,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { viewModel.deleteSession(session.id) }
                                )
                            }
                        }
                    }

                    // "+" New Session Button
                    IconButton(
                        onClick = { showNewSessionDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Terminal Session",
                            tint = VsCodeAccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Telemetry & Working Directory Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF181818))
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // CWD (Path)
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "cwd: ",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )
                        Text(
                            text = cwd,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = VsCodeYellow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Live Stats
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CPU $cpu", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = VsCodeGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RAM $ram", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = VsCodeOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DISK $disk", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = VsCodeText)
                    }
                }
            }
        },
        containerColor = VsCodeBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedDeckTab == 0) {
                // ==========================================
                // MANUAL VS CODE TERMINAL MODE
                // ==========================================
                Column(modifier = Modifier.fillMaxSize()) {
                    // Output Log Stream (VS Code Console)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(VsCodeBlack)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val listState = rememberLazyListState()

                        // Auto-scroll to latest log
                        LaunchedEffect(sessionLogs.size, isExecuting) {
                            if (sessionLogs.isNotEmpty()) {
                                listState.animateScrollToItem(sessionLogs.size - 1)
                            }
                        }

                        if (sessionLogs.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = VsCodeBorder,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ready for execution on $activeNode",
                                    color = Color.Gray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Type any command below or use quick chips.",
                                    color = Color.DarkGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(sessionLogs) { index, log ->
                                    VsCodeTerminalLogItem(
                                        log = log,
                                        prompt = prompt,
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString(log.command + "\n" + log.output))
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        onRerun = {
                                            viewModel.executeCommand(log.command)
                                        },
                                        onSaveToVault = {
                                            viewModel.saveCommandToVault(
                                                name = log.command.take(25),
                                                command = log.command
                                            )
                                            Toast.makeText(context, "Saved to Vault", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }

                                if (isExecuting) {
                                    item {
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 2.dp,
                                                color = VsCodeGreen
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Executing command...",
                                                color = VsCodeYellow,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quick Command Chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VsCodeTabActive)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickChips = if (osType == "windows") {
                            listOf("dir", "Get-Process", "git status", "git pull", "ipconfig", "cls")
                        } else {
                            listOf("ls -la", "git status", "git pull", "ps aux", "free -h", "uptime", "clear")
                        }

                        items(quickChips) { chipCmd ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = VsCodeDark,
                                modifier = Modifier.clickable {
                                    manualCommandInput = chipCmd
                                }
                            ) {
                                Text(
                                    text = chipCmd,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = VsCodeGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Terminal Input Line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VsCodeDark)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Prompt indicator
                        Text(
                            text = prompt.ifBlank { "$ " },
                            color = VsCodeGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        // Command input text field
                        OutlinedTextField(
                            value = manualCommandInput,
                            onValueChange = { manualCommandInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            placeholder = {
                                Text(
                                    "Enter command...",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Gray
                                )
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = VsCodeTextBright
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (manualCommandInput.isNotBlank()) {
                                        viewModel.executeCommand(manualCommandInput)
                                        manualCommandInput = ""
                                        historyIndex = -1
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VsCodeAccentBlue,
                                unfocusedBorderColor = VsCodeBorder,
                                focusedContainerColor = VsCodeBlack,
                                unfocusedContainerColor = VsCodeBlack
                            )
                        )

                        // Up & Down History Navigation Chevrons
                        IconButton(
                            onClick = {
                                if (recentCommands.isNotEmpty()) {
                                    if (historyIndex < recentCommands.size - 1) {
                                        historyIndex++
                                        manualCommandInput = recentCommands[historyIndex]
                                    }
                                }
                            },
                            modifier = Modifier.size(32.dp),
                            enabled = recentCommands.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Previous Command",
                                tint = if (recentCommands.isNotEmpty()) VsCodeText else Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (recentCommands.isNotEmpty() && historyIndex > 0) {
                                    historyIndex--
                                    manualCommandInput = recentCommands[historyIndex]
                                } else if (historyIndex == 0) {
                                    historyIndex = -1
                                    manualCommandInput = ""
                                }
                            },
                            modifier = Modifier.size(32.dp),
                            enabled = historyIndex >= 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Next Command",
                                tint = if (historyIndex >= 0) VsCodeText else Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Run Button
                        IconButton(
                            onClick = {
                                if (manualCommandInput.isNotBlank()) {
                                    viewModel.executeCommand(manualCommandInput)
                                    manualCommandInput = ""
                                    historyIndex = -1
                                }
                            },
                            enabled = manualCommandInput.isNotBlank() && !isExecuting,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (manualCommandInput.isNotBlank()) VsCodeAccentBlue else Color.DarkGray,
                                    RoundedCornerShape(6.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run Command",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // AUTO PILOT MODE: 4-STAGE SEAMLESS FLOW
                // 1. User & AI Chat interaction
                // 2. Recommended Procedure Commands (Multi-step!)
                // 3. Small inline manual command field
                // 4. Live Terminal View output
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Upper Half: User + AI Interaction & Recommended Procedures
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Section 1: User & AI Conversation Thread for this session
                        if (sessionPilotChats.isNotEmpty()) {
                            Text(
                                text = "SESSION PILOT BRIEFINGS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VsCodeYellow,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            sessionPilotChats.takeLast(3).forEach { chat ->
                                // User Goal Bubble
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp),
                                        color = Color(0xFF37474F),
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    ) {
                                        Text(
                                            text = chat.userGoal,
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                // AI Pilot Explanation Bubble
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp),
                                        color = Color(0xFF263238),
                                        modifier = Modifier.widthIn(max = 300.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.SmartToy,
                                                    contentDescription = null,
                                                    tint = Color(0xFF81D4FA),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Orbit Pilot",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF81D4FA)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = chat.aiExplanation,
                                                fontSize = 11.sp,
                                                color = VsCodeTextBright
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // AI Goal Input Field
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = VsCodeCard)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                OutlinedTextField(
                                    value = autoPilotGoalInput,
                                    onValueChange = { autoPilotGoalInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "Tell Pilot what you want done (e.g. 'Deploy git changes and build Docker')",
                                            fontSize = 11.sp
                                        )
                                    },
                                    maxLines = 3,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = VsCodeTextBright),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6C5CE7),
                                        unfocusedBorderColor = VsCodeBorder,
                                        focusedContainerColor = VsCodeBlack,
                                        unfocusedContainerColor = VsCodeBlack
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quick prompt chips
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF2D2D2D),
                                            modifier = Modifier.clickable {
                                                autoPilotGoalInput = "Check system resources and disk space"
                                            }
                                        ) {
                                            Text("Resources", fontSize = 9.sp, color = VsCodeYellow, modifier = Modifier.padding(4.dp))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF2D2D2D),
                                            modifier = Modifier.clickable {
                                                autoPilotGoalInput = "Git stage, commit, and push updates"
                                            }
                                        ) {
                                            Text("Git Sync", fontSize = 9.sp, color = VsCodeGreen, modifier = Modifier.padding(4.dp))
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (autoPilotGoalInput.isNotBlank()) {
                                                viewModel.askPilot(autoPilotGoalInput)
                                                autoPilotGoalInput = ""
                                            }
                                        },
                                        enabled = autoPilotGoalInput.isNotBlank() && !isLoading,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C5CE7)),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Ask Pilot", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Recommended Commands (Multiple Procedure Steps)
                        val suggestion = currentSuggestion
                        if (suggestion != null) {
                            val procedureSteps = if (!suggestion.commands.isNullOrEmpty()) {
                                suggestion.commands!!
                            } else {
                                listOf(SuggestedCommandItem(1, "Primary Command", suggestion.command ?: "ls"))
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VsCodeAccentBlue))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = VsCodeYellow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "RECOMMENDED PROCEDURE (${procedureSteps.size} STEPS)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VsCodeAccentBlue,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.dismissSuggestion() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    Text(
                                        text = suggestion.explanation,
                                        fontSize = 11.sp,
                                        color = VsCodeTextBright,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Divider(color = Color(0xFF2C3E50), modifier = Modifier.padding(vertical = 6.dp))

                                    // Display each step with run, edit, and copy
                                    procedureSteps.forEachIndexed { idx, step ->
                                        var editableCmd by remember(step.cmd) { mutableStateOf(step.cmd) }

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181F2C))
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Step ${step.step}: ${step.title}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = VsCodeGreen,
                                                        fontFamily = FontFamily.Monospace
                                                    )

                                                    Row {
                                                        // Run this individual step
                                                        IconButton(
                                                            onClick = { viewModel.executeCommand(editableCmd) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = "Run Step",
                                                                tint = VsCodeSuccessGreen,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        // Copy
                                                        IconButton(
                                                            onClick = {
                                                                clipboardManager.setText(AnnotatedString(editableCmd))
                                                                Toast.makeText(context, "Command copied", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.ContentCopy,
                                                                contentDescription = "Copy Command",
                                                                tint = VsCodeText,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                        // Save to Vault
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.saveCommandToVault(
                                                                    name = step.title.ifBlank { editableCmd.take(25) },
                                                                    command = editableCmd
                                                                )
                                                                Toast.makeText(context, "Saved to Vault", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.BookmarkBorder,
                                                                contentDescription = "Save to Vault",
                                                                tint = VsCodeYellow,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                // Editable Command Field
                                                OutlinedTextField(
                                                    value = editableCmd,
                                                    onValueChange = { editableCmd = it },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true,
                                                    textStyle = androidx.compose.ui.text.TextStyle(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF61AFEF)
                                                    ),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = VsCodeAccentBlue,
                                                        unfocusedBorderColor = Color(0xFF2C3E50),
                                                        focusedContainerColor = Color(0xFF131822),
                                                        unfocusedContainerColor = Color(0xFF131822)
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Run Entire Procedure button if multiple steps
                                    if (procedureSteps.size > 1) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { viewModel.executeEntireProcedure(procedureSteps) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = VsCodeSuccessGreen),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = Color.Black)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Run All ${procedureSteps.size} Procedure Steps", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 3: Small Sleek Field for Manual Input in Auto Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VsCodeTabActive, RoundedCornerShape(6.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prompt.ifBlank { "$ " },
                            color = VsCodeGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        OutlinedTextField(
                            value = autoManualInlineCmd,
                            onValueChange = { autoManualInlineCmd = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            placeholder = { Text("Manual command override...", fontSize = 10.sp, color = Color.Gray) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = VsCodeTextBright
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VsCodeAccentBlue,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = VsCodeDark,
                                unfocusedContainerColor = VsCodeDark
                            )
                        )
                        IconButton(
                            onClick = {
                                if (autoManualInlineCmd.isNotBlank()) {
                                    viewModel.executeCommand(autoManualInlineCmd)
                                    autoManualInlineCmd = ""
                                }
                            },
                            enabled = autoManualInlineCmd.isNotBlank() && !isExecuting,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = VsCodeGreen, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Section 4: Live VS Code Terminal View (Real time output)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VsCodeBlack),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VsCodeBorder))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Mini VS Code Terminal Tab Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF202020))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(VsCodeSuccessGreen))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("LIVE TERMINAL OUTPUT", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                Text("session: ${activeSession?.name ?: ""}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = VsCodeYellow)
                            }

                            val autoListState = rememberLazyListState()
                            LaunchedEffect(sessionLogs.size, isExecuting) {
                                if (sessionLogs.isNotEmpty()) {
                                    autoListState.animateScrollToItem(sessionLogs.size - 1)
                                }
                            }

                            LazyColumn(
                                state = autoListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            ) {
                                items(sessionLogs) { log ->
                                    VsCodeTerminalLogItem(
                                        log = log,
                                        prompt = prompt,
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString(log.command + "\n" + log.output))
                                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                        },
                                        onRerun = { viewModel.executeCommand(log.command) },
                                        onSaveToVault = {
                                            viewModel.saveCommandToVault(log.command.take(25), log.command)
                                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }

                                if (isExecuting) {
                                    item {
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 2.dp, color = VsCodeGreen)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Executing live on $activeNode...", fontSize = 10.sp, color = VsCodeYellow, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // MODAL BOTTOM SHEETS & DIALOGS
    // ==========================================

    // 1. Cluster Nodes Selector Sheet
    if (showNodeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNodeSheet = false },
            containerColor = VsCodeDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLUSTER COMPUTING NODES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VsCodeAccentBlue,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = { showAddNodeDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Node", tint = VsCodeGreen)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                nodesList.forEach { nodeName ->
                    val isSelected = nodeName == activeNode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF2A2D2E) else Color(0xFF1E1E1E),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, VsCodeAccentBlue) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.selectActiveClusterNode(nodeName)
                                showNodeSheet = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) VsCodeSuccessGreen else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = nodeName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) VsCodeTextBright else VsCodeText
                                    )
                                    Text(
                                        text = if (nodeName == "HF_Space_Node") "Hugging Face / Primary Container" else "Remote Workstation Agent",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = VsCodeAccentBlue
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Backend Endpoint Config Section
                var editingServerUrl by remember { mutableStateOf(false) }
                var serverUrlInput by remember { mutableStateOf(RetrofitClient.currentBaseUrl) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF182230)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VsCodeBorder))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("BACKEND SERVER / SPACE URL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VsCodeYellow, fontFamily = FontFamily.Monospace)
                            TextButton(onClick = { editingServerUrl = !editingServerUrl }) {
                                Text(if (editingServerUrl) "Close" else "Edit", fontSize = 11.sp, color = VsCodeGreen)
                            }
                        }

                        if (editingServerUrl) {
                            OutlinedTextField(
                                value = serverUrlInput,
                                onValueChange = { serverUrlInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Base URL (e.g. https://...hf.space/ or ngrok)", fontSize = 11.sp) },
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = VsCodeTextBright)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (serverUrlInput.isNotBlank()) {
                                        RetrofitClient.updateBaseUrl(serverUrlInput.trim())
                                        editingServerUrl = false
                                        viewModel.fetchBackendStatus()
                                        Toast.makeText(context, "Server URL updated!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = VsCodeAccentBlue)
                            ) {
                                Text("Save & Connect", fontSize = 11.sp)
                            }
                        } else {
                            Text(
                                text = RetrofitClient.currentBaseUrl,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = VsCodeTextBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // 2. Command History Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = VsCodeDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "COMMAND HISTORY (${recentCommands.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeYellow,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (recentCommands.isEmpty()) {
                    Text("No command history recorded yet.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                        items(recentCommands) { cmd ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = VsCodeTabInactive,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        manualCommandInput = cmd
                                        showHistorySheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cmd,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = VsCodeTextBright,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.executeCommand(cmd)
                                            showHistorySheet = false
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = VsCodeGreen)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // 3. Vault Sheet
    if (showVaultSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVaultSheet = false },
            containerColor = VsCodeDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "COMMAND & PROCEDURE VAULT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeOrange,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (vaultCommands.isEmpty()) {
                    Text("Vault is empty. Save any command from Terminal or Auto Pilot.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                        items(vaultCommands) { vaultItem ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF262626),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = vaultItem.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VsCodeYellow
                                        )
                                        Text(
                                            text = vaultItem.command,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = VsCodeText
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                viewModel.executeCommand(vaultItem.command)
                                                showVaultSheet = false
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Execute", tint = VsCodeGreen)
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteCommandFromVault(vaultItem.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // 4. Create Session Dialog
    if (showNewSessionDialog) {
        AlertDialog(
            onDismissRequest = { showNewSessionDialog = false },
            title = { Text("New Terminal Session") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSessionCustomName,
                        onValueChange = { newSessionCustomName = it },
                        label = { Text("Session Name (e.g. 2: python, setup)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("bash", "powershell", "zsh", "node").forEach { shell ->
                            FilterChip(
                                selected = newSessionShell == shell,
                                onClick = { newSessionShell = shell },
                                label = { Text(shell, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createNewSession(
                            customName = if (newSessionCustomName.isNotBlank()) newSessionCustomName else null,
                            shell = newSessionShell
                        )
                        newSessionCustomName = ""
                        showNewSessionDialog = false
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewSessionDialog = false }) { Text("Cancel") }
            }
        )
    }

    // 5. Connect Node Dialog
    if (showAddNodeDialog) {
        AlertDialog(
            onDismissRequest = { showAddNodeDialog = false },
            title = { Text("Connect Remote Cluster Node") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newNodeName,
                        onValueChange = { newNodeName = it },
                        label = { Text("Node Name (e.g. Workstation_1)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newNodeHost,
                        onValueChange = { newNodeHost = it },
                        label = { Text("IP / Hostname (e.g. 192.168.1.100)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNodeName.isNotBlank() && newNodeHost.isNotBlank()) {
                            viewModel.addNewClusterNode(newNodeName.trim(), newNodeHost.trim(), 8888)
                            newNodeName = ""
                            newNodeHost = ""
                            showAddNodeDialog = false
                            showNodeSheet = false
                        }
                    }
                ) { Text("Connect") }
            },
            dismissButton = {
                TextButton(onClick = { showAddNodeDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// Single VS Code Log Item Component
@Composable
fun VsCodeTerminalLogItem(
    log: TerminalLogEntryEntity,
    prompt: String,
    onCopy: () -> Unit,
    onRerun: () -> Unit,
    onSaveToVault: () -> Unit
) {
    val timeStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Color(0xFF141414), RoundedCornerShape(4.dp))
            .padding(6.dp)
    ) {
        // Command Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit code status dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (log.exitCode == 0) VsCodeSuccessGreen else VsCodeErrorRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = log.prompt.ifBlank { prompt },
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeGreen
                )
                Text(
                    text = log.command,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = VsCodeTextBright
                )
            }

            // Duration & Timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (log.durationMs > 0) {
                    Text(
                        text = "${log.durationMs}ms",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = timeStr,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.width(4.dp))

                // Actions
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onCopy() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Rerun",
                    tint = VsCodeGreen,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onRerun() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Vault",
                    tint = VsCodeYellow,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onSaveToVault() }
                )
            }
        }

        // Command Output
        if (log.output.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            val isError = log.exitCode != 0 || log.output.startsWith("Error", ignoreCase = true)
            Text(
                text = log.output,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isError) VsCodeErrorRed else VsCodeText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            )
        }
    }
}
