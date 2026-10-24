// ==========================================
// IDENTITY: The Orchestrator / VS Code Terminal & Pilot Cluster ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/TerminalViewModel.kt
// VERSION: 3.0.0 | SYSTEM: Session-Based Multi-Node VS Code Deck
// VIBE: Full terminal session persistence, live output, and AI procedure orchestration. 🖥️✨
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket_orbit.BuildConfig
import com.example.pocket_orbit.data.*
import com.example.pocket_orbit.network.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.pocket_orbit.network.offline.*
import android.content.Context
import java.util.UUID

class TerminalViewModel(
    apiService: ApiService? = null,
    private val terminalDao: TerminalDao
) : ViewModel() {

    // Dynamic resolution ensures commands ALWAYS target the user's custom Backend Server / Space URL
    private val apiService: ApiService
        get() = RetrofitClient.apiService

    private val gson = Gson()
    private val secretToken = "Bearer ${BuildConfig.ORBIT_SECRET_TOKEN}"

    // Sessions
    val allSessions: StateFlow<List<TerminalSessionEntity>> = terminalDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSessionId = MutableStateFlow<String>("")
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    private val _activeSession = MutableStateFlow<TerminalSessionEntity?>(null)
    val activeSession: StateFlow<TerminalSessionEntity?> = _activeSession.asStateFlow()

    // Logs & Output for the active session
    private val _sessionLogs = MutableStateFlow<List<TerminalLogEntryEntity>>(emptyList())
    val sessionLogs: StateFlow<List<TerminalLogEntryEntity>> = _sessionLogs.asStateFlow()

    private val _terminalOutput = MutableStateFlow<String>("")
    val terminalOutput: StateFlow<String> = _terminalOutput.asStateFlow()

    // Pilot Chats for the active session
    private val _sessionPilotChats = MutableStateFlow<List<TerminalPilotChatEntity>>(emptyList())
    val sessionPilotChats: StateFlow<List<TerminalPilotChatEntity>> = _sessionPilotChats.asStateFlow()

    // Command History & Vault
    val recentCommands: StateFlow<List<String>> = terminalDao.getRecentCommands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localVaultCommands: StateFlow<List<TerminalCommandVaultEntity>> = terminalDao.getVaultCommands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Telemetry & Cluster State
    private val _cpu = MutableStateFlow("0%")
    val cpu: StateFlow<String> = _cpu.asStateFlow()

    private val _ram = MutableStateFlow("0%")
    val ram: StateFlow<String> = _ram.asStateFlow()

    private val _disk = MutableStateFlow("0%")
    val disk: StateFlow<String> = _disk.asStateFlow()

    private val _cwd = MutableStateFlow("~")
    val cwd: StateFlow<String> = _cwd.asStateFlow()

    private val _osType = MutableStateFlow("linux")
    val osType: StateFlow<String> = _osType.asStateFlow()

    private val _prompt = MutableStateFlow("$ ")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _activeNode = MutableStateFlow("Main_HF_Space")
    val activeNode: StateFlow<String> = _activeNode.asStateFlow()

    private val _nodesList = MutableStateFlow<List<String>>(listOf("Main_HF_Space"))
    val nodesList: StateFlow<List<String>> = _nodesList.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Connected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // Pilot Suggestions
    private val _currentSuggestion = MutableStateFlow<TerminalSuggestionResponse?>(null)
    val currentSuggestion: StateFlow<TerminalSuggestionResponse?> = _currentSuggestion.asStateFlow()

    // Attached Terminal Log for Auto Pilot context
    private val _attachedLog = MutableStateFlow<AttachedTerminalLog?>(null)
    val attachedLog: StateFlow<AttachedTerminalLog?> = _attachedLog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    // Offline Direct Link (Hotspot, USB Tethering, & Bluetooth)
    private val _isScanningSubnet = MutableStateFlow(false)
    val isScanningSubnet: StateFlow<Boolean> = _isScanningSubnet.asStateFlow()

    private val _scanProgress = MutableStateFlow(0 to 0)
    val scanProgress: StateFlow<Pair<Int, Int>> = _scanProgress.asStateFlow()

    private val _discoveredNodes = MutableStateFlow<List<DiscoveredOfflineNode>>(emptyList())
    val discoveredNodes: StateFlow<List<DiscoveredOfflineNode>> = _discoveredNodes.asStateFlow()

    private val _activeInterfaces = MutableStateFlow<List<NetworkInterfaceInfo>>(emptyList())
    val activeInterfaces: StateFlow<List<NetworkInterfaceInfo>> = _activeInterfaces.asStateFlow()

    val btConnectionState: StateFlow<BluetoothConnectionState> = BluetoothNodeManager.connectionState
    val pairedBtDevices: StateFlow<List<BluetoothDeviceInfo>> = BluetoothNodeManager.pairedDevices

    init {
        initializeSessions()
        startPeriodicTelemetry()
    }

    private fun initializeSessions() {
        viewModelScope.launch {
            val existing = allSessions.first { true }
            if (existing.isEmpty()) {
                val defaultSessionId = UUID.randomUUID().toString()
                val defaultSession = TerminalSessionEntity(
                    id = defaultSessionId,
                    name = "1: bash",
                    nodeName = "HF_Space_Node",
                    shellType = "bash",
                    createdAt = System.currentTimeMillis(),
                    lastActiveTimestamp = System.currentTimeMillis()
                )
                terminalDao.insertSession(defaultSession)
                _activeSessionId.value = defaultSessionId
                _activeSession.value = defaultSession
                observeCurrentSession(defaultSessionId)
            } else {
                val latest = existing.first()
                _activeSessionId.value = latest.id
                _activeSession.value = latest
                observeCurrentSession(latest.id)
            }
            fetchBackendStatus()
        }
    }

    private fun observeCurrentSession(sessionId: String) {
        viewModelScope.launch {
            terminalDao.getLogsForSession(sessionId).collect { logs ->
                _sessionLogs.value = logs
                rebuildTerminalOutput(logs)
            }
        }
        viewModelScope.launch {
            terminalDao.getPilotChatsForSession(sessionId).collect { chats ->
                _sessionPilotChats.value = chats
            }
        }
    }

    private fun rebuildTerminalOutput(logs: List<TerminalLogEntryEntity>) {
        val sb = StringBuilder()
        val sessionName = _activeSession.value?.name ?: "Terminal"
        val shell = _activeSession.value?.shellType ?: "bash"
        sb.append("Welcome to Orbit VS Code Terminal Deck [${_activeNode.value} :: $sessionName ($shell)]\n")
        sb.append("Type commands below or use Auto Pilot mode for AI assisted operations.\n\n")

        logs.forEach { entry ->
            val p = if (entry.prompt.isNotBlank()) entry.prompt else _prompt.value
            sb.append(p).append(entry.command).append("\n")
            if (entry.output.isNotBlank()) {
                sb.append(entry.output)
                if (!entry.output.endsWith("\n")) sb.append("\n")
            }
        }
        sb.append(_prompt.value)
        _terminalOutput.value = sb.toString()
    }

    private fun startPeriodicTelemetry() {
        viewModelScope.launch {
            while (true) {
                delay(4000)
                fetchBackendStatus()
            }
        }
    }

    fun fetchBackendStatus() {
        if (_activeNode.value.startsWith("BT:")) {
            viewModelScope.launch {
                val status = BluetoothNodeManager.queryStatus()
                if (status != null) {
                    _connectionStatus.value = "Connected (Bluetooth)"
                    _cpu.value = "${status.optDouble("cpu_usage", 0.0).toInt()}%"
                    _ram.value = "${status.optDouble("ram_usage", 0.0).toInt()}%"
                    _disk.value = "${status.optDouble("disk_usage", 0.0).toInt()}%"
                    _cwd.value = status.optString("cwd", _cwd.value)
                    _osType.value = status.optString("system", _osType.value)
                } else if (!BluetoothNodeManager.isConnected()) {
                    _connectionStatus.value = "Bluetooth Disconnected"
                }
            }
            return
        }
        viewModelScope.launch {
            try {
                val response = apiService.getTerminalStatus(secretToken)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    _connectionStatus.value = "Connected"
                    _cpu.value = "${data.telemetry.cpu.toInt()}%"
                    _ram.value = "${data.telemetry.ram.toInt()}%"
                    _disk.value = "${data.telemetry.disk.toInt()}%"
                    _cwd.value = data.telemetry.cwd
                    _osType.value = data.os ?: "linux"
                    _prompt.value = data.prompt ?: if (data.os == "windows") "${data.telemetry.cwd}> " else "${data.telemetry.cwd.substringAfterLast('/')}$ "
                    _activeNode.value = data.active_node
                    _nodesList.value = data.nodes

                    // If terminal has no logs yet, make sure prompt is clean
                    if (_sessionLogs.value.isEmpty()) {
                        rebuildTerminalOutput(emptyList())
                    }
                } else {
                    _connectionStatus.value = "Connecting..."
                }
            } catch (e: Exception) {
                _connectionStatus.value = "Offline"
            }
        }
    }

    // ==========================================
    // SESSION MANAGEMENT
    // ==========================================

    fun createNewSession(customName: String? = null, shell: String = "bash") {
        viewModelScope.launch {
            val sessions = allSessions.value
            val nextNum = sessions.size + 1
            val name = customName ?: "$nextNum: $shell"
            val newSessionId = UUID.randomUUID().toString()
            val newSession = TerminalSessionEntity(
                id = newSessionId,
                name = name,
                nodeName = _activeNode.value,
                shellType = shell,
                createdAt = System.currentTimeMillis(),
                lastActiveTimestamp = System.currentTimeMillis()
            )
            terminalDao.insertSession(newSession)
            switchSession(newSessionId)
        }
    }

    fun switchSession(sessionId: String) {
        viewModelScope.launch {
            val session = allSessions.value.find { it.id == sessionId } ?: return@launch
            _activeSessionId.value = sessionId
            _activeSession.value = session
            terminalDao.touchSession(sessionId, System.currentTimeMillis())
            observeCurrentSession(sessionId)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            terminalDao.deleteSession(sessionId)
            terminalDao.clearLogsForSession(sessionId)
            terminalDao.clearPilotChatsForSession(sessionId)

            val remaining = allSessions.value.filter { it.id != sessionId }
            if (remaining.isNotEmpty()) {
                switchSession(remaining.first().id)
            } else {
                createNewSession()
            }
        }
    }

    fun renameSession(sessionId: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            terminalDao.renameSession(sessionId, newName)
            if (_activeSessionId.value == sessionId) {
                _activeSession.value = _activeSession.value?.copy(name = newName)
            }
        }
    }

    // ==========================================
    // COMMAND EXECUTION (LIVE TERMINAL)
    // ==========================================

    fun executeCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isBlank()) return
        val currentSessionId = _activeSessionId.value
        val currentPrompt = _prompt.value
        val currentCwd = _cwd.value

        viewModelScope.launch {
            _isExecuting.value = true
            val startTime = System.currentTimeMillis()
            var execOutput = ""
            var exitCode = 0

            try {
                if (_activeNode.value.startsWith("BT:")) {
                    val btRes = BluetoothNodeManager.executeCommand(trimmed)
                    execOutput = btRes.output
                    exitCode = btRes.exitCode
                    if (btRes.cwd.isNotBlank()) _cwd.value = btRes.cwd
                } else {
                    val response = apiService.executeCommand(secretToken, TerminalCommandRequest(trimmed))
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        execOutput = body.output ?: body.message ?: "Done."
                        if (body.cwd != null) _cwd.value = body.cwd
                        if (body.prompt != null) _prompt.value = body.prompt
                    } else {
                        execOutput = "Command exited with status ${response.code()}: ${response.message()}"
                        exitCode = 1
                    }
                }
            } catch (e: Exception) {
                execOutput = "Execution error: ${e.message ?: "Network unreachable"}"
                exitCode = 1
            } finally {
                val duration = System.currentTimeMillis() - startTime
                _isExecuting.value = false

                // Record into Room database
                val entry = TerminalLogEntryEntity(
                    sessionId = currentSessionId,
                    command = trimmed,
                    output = execOutput,
                    exitCode = exitCode,
                    cwd = currentCwd,
                    prompt = currentPrompt,
                    timestamp = System.currentTimeMillis(),
                    durationMs = duration
                )
                terminalDao.insertLogEntry(entry)
                terminalDao.touchSession(currentSessionId, System.currentTimeMillis())

                // Quick status refresh to update cwd/ram/cpu
                fetchBackendStatus()
            }
        }
    }

    fun clearSessionTerminal() {
        val currentSessionId = _activeSessionId.value
        viewModelScope.launch {
            terminalDao.clearLogsForSession(currentSessionId)
            rebuildTerminalOutput(emptyList())
            try {
                apiService.clearTerminal(secretToken)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // ==========================================
    // AUTO PILOT (USER + AI CHAT & PROCEDURES)
    // ==========================================

    fun attachLogEntry(entry: TerminalLogEntryEntity) {
        _attachedLog.value = AttachedTerminalLog(
            sourceCommand = entry.command,
            output = entry.output,
            exitCode = entry.exitCode,
            timestamp = entry.timestamp
        )
    }

    fun attachLatestOutput(): Boolean {
        val latest = _sessionLogs.value.lastOrNull { it.output.isNotBlank() }
        return if (latest != null) {
            attachLogEntry(latest)
            true
        } else {
            false
        }
    }

    fun attachLatestError(): Boolean {
        val latestError = _sessionLogs.value.lastOrNull {
            it.exitCode != 0 || 
            it.output.contains("error", ignoreCase = true) || 
            it.output.contains("failed", ignoreCase = true) ||
            it.output.contains("fatal", ignoreCase = true)
        }
        return if (latestError != null) {
            attachLogEntry(latestError)
            true
        } else {
            // If no explicit error flag, fallback to the latest output
            attachLatestOutput()
        }
    }

    fun attachCustomLog(command: String, output: String) {
        if (output.isNotBlank()) {
            _attachedLog.value = AttachedTerminalLog(
                sourceCommand = if (command.isNotBlank()) command else "Custom Log Snippet",
                output = output,
                exitCode = 0,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun clearAttachedLog() {
        _attachedLog.value = null
    }

    fun askPilot(userGoal: String, explicitLog: String? = null) {
        val trimmed = userGoal.trim()
        if (trimmed.isBlank()) return
        val currentSessionId = _activeSessionId.value
        val logToSend = (explicitLog ?: _attachedLog.value?.output)?.takeIf { it.isNotBlank() }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.suggestCommand(
                    secretToken, 
                    TerminalSuggestionRequest(
                        user_goal = trimmed,
                        terminal_output = logToSend
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val suggestion = response.body()!!
                    _currentSuggestion.value = suggestion

                    // Store chat record into Room for this session
                    val commandsJson = gson.toJson(suggestion.commands ?: emptyList<SuggestedCommandItem>())
                    val chatEntry = TerminalPilotChatEntity(
                        sessionId = currentSessionId,
                        userGoal = trimmed,
                        aiExplanation = suggestion.explanation,
                        recommendedCommandsJson = commandsJson,
                        attachedLogs = logToSend,
                        timestamp = System.currentTimeMillis()
                    )
                    terminalDao.insertPilotChat(chatEntry)
                    terminalDao.touchSession(currentSessionId, System.currentTimeMillis())
                    
                    // Clear the attached log after sending
                    _attachedLog.value = null
                } else {
                    _currentSuggestion.value = TerminalSuggestionResponse(
                        explanation = "Pilot was unable to generate a plan (${response.code()}).",
                        command = "echo 'Pilot error'",
                        commands = listOf(SuggestedCommandItem(1, "Check status", "uptime"))
                    )
                }
            } catch (e: Exception) {
                _currentSuggestion.value = TerminalSuggestionResponse(
                    explanation = "Communication with Orbit Pilot failed: ${e.message}",
                    command = "ls -la",
                    commands = listOf(SuggestedCommandItem(1, "Inspect Directory", "ls -la"))
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun executeEntireProcedure(commands: List<SuggestedCommandItem>) {
        if (commands.isEmpty()) return
        viewModelScope.launch {
            for (step in commands) {
                executeCommand(step.cmd)
                delay(600) // slight delay so live terminal stream flows naturally
            }
            dismissSuggestion()
        }
    }

    fun dismissSuggestion() {
        _currentSuggestion.value = null
    }

    // ==========================================
    // COMMAND VAULT
    // ==========================================

    fun saveCommandToVault(name: String, command: String, category: String = "General") {
        if (command.isBlank()) return
        viewModelScope.launch {
            val vaultEntry = TerminalCommandVaultEntity(
                name = if (name.isNotBlank()) name else command.take(30),
                command = command,
                category = category,
                timestamp = System.currentTimeMillis()
            )
            terminalDao.insertVaultCommand(vaultEntry)
            try {
                apiService.saveToVault(secretToken, VaultSaveRequest(vaultEntry.name, command, category))
            } catch (e: Exception) {
                // Local save already succeeded
            }
        }
    }

    fun deleteCommandFromVault(id: Int) {
        viewModelScope.launch {
            terminalDao.deleteVaultCommand(id)
        }
    }

    // ==========================================
    // CLUSTER NODE MANAGEMENT
    // ==========================================

    fun selectActiveClusterNode(nodeName: String) {
        if (nodeName.startsWith("BT:")) {
            _activeNode.value = nodeName
            _connectionStatus.value = if (BluetoothNodeManager.isConnected()) "Connected (Bluetooth)" else "Offline (BT)"
            _prompt.value = "${nodeName.removePrefix("BT:")}> "
            fetchBackendStatus()
            return
        }
        viewModelScope.launch {
            try {
                val response = apiService.selectActiveNode(secretToken, SelectNodeRequest(nodeName))
                if (response.isSuccessful) {
                    _activeNode.value = nodeName
                    fetchBackendStatus()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun addNewClusterNode(name: String, host: String, port: Int = 8888) {
        viewModelScope.launch {
            try {
                val response = apiService.connectNewNode(secretToken, ConnectNodeRequest(name, host, port))
                if (response.isSuccessful) {
                    selectActiveClusterNode(name)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun disconnectClusterNode(name: String) {
        if (name.startsWith("BT:")) {
            disconnectBluetoothNode()
            _nodesList.value = _nodesList.value.filter { it != name }
            return
        }
        viewModelScope.launch {
            try {
                val response = apiService.disconnectNode(secretToken, name)
                if (response.isSuccessful) {
                    fetchBackendStatus()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // ==========================================
    // OFFLINE DIRECT LINK (HOTSPOT, USB, & BLUETOOTH)
    // ==========================================

    fun scanOfflineSubnet(customPrefix: String? = null) {
        viewModelScope.launch {
            _isScanningSubnet.value = true
            _activeInterfaces.value = OfflineNetworkScanner.getActiveOfflineInterfaces()
            try {
                val results = OfflineNetworkScanner.scanSubnet(
                    customPrefix = customPrefix,
                    onProgress = { current, total ->
                        _scanProgress.value = current to total
                    }
                )
                _discoveredNodes.value = results
            } catch (e: Exception) {
                // Ignore
            } finally {
                _isScanningSubnet.value = false
            }
        }
    }

    fun connectDiscoveredNode(node: DiscoveredOfflineNode) {
        addNewClusterNode(node.name, node.ip, node.port)
    }

    fun loadPairedBluetoothDevices(context: Context) {
        BluetoothNodeManager.loadPairedDevices(context)
    }

    fun connectBluetoothNode(device: BluetoothDeviceInfo) {
        viewModelScope.launch {
            val success = BluetoothNodeManager.connect(device.address, device.name)
            if (success) {
                val btNodeName = "BT:${device.name.replace(" ", "_")}"
                if (!_nodesList.value.contains(btNodeName)) {
                    _nodesList.value = _nodesList.value + btNodeName
                }
                _activeNode.value = btNodeName
                _connectionStatus.value = "Connected (Bluetooth)"
                _prompt.value = "${device.name}> "
                fetchBackendStatus()
            }
        }
    }

    fun disconnectBluetoothNode() {
        BluetoothNodeManager.disconnect()
        if (_activeNode.value.startsWith("BT:")) {
            _activeNode.value = "Main_HF_Space"
            _connectionStatus.value = "Connected"
            fetchBackendStatus()
        }
    }

    // Helper to decode stored procedure json
    fun parseRecommendedCommands(json: String): List<SuggestedCommandItem> {
        return try {
            val type = object : TypeToken<List<SuggestedCommandItem>>() {}.type
            gson.fromJson<List<SuggestedCommandItem>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class AttachedTerminalLog(
    val sourceCommand: String,
    val output: String,
    val exitCode: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
