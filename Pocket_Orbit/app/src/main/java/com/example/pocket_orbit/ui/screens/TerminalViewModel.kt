// ==========================================
// IDENTITY: The Orchestrator / Terminal Pilot Cluster ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/TerminalViewModel.kt
// VERSION: 1.1.0 | SYSTEM: Multi-Node Node Mapping Tracking
// VIBE: Sync and route cluster targets with beautiful state updates. 🖥️📡
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket_orbit.network.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TerminalViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _terminalOutput = MutableStateFlow("Terminal initialized...\nPS C:\\Users\\Administrator> ")
    val terminalOutput: StateFlow<String> = _terminalOutput.asStateFlow()

    private val _cpu = MutableStateFlow("0%")
    val cpu: StateFlow<String> = _cpu.asStateFlow()

    private val _ram = MutableStateFlow("0%")
    val ram: StateFlow<String> = _ram.asStateFlow()

    private val _disk = MutableStateFlow("0%")
    val disk: StateFlow<String> = _disk.asStateFlow()

    private val _cwd = MutableStateFlow("C:\\Users\\Administrator")
    val cwd: StateFlow<String> = _cwd.asStateFlow()

    private val _activeNode = MutableStateFlow("HF_Space_Node")
    val activeNode: StateFlow<String> = _activeNode.asStateFlow()

    private val _nodesList = MutableStateFlow<List<String>>(listOf("HF_Space_Node"))
    val nodesList: StateFlow<List<String>> = _nodesList.asStateFlow()

    private val _vaultCommands = MutableStateFlow<List<VaultCommandItem>>(emptyList())
    val vaultCommands: StateFlow<List<VaultCommandItem>> = _vaultCommands.asStateFlow()

    private val _currentSuggestion = MutableStateFlow<TerminalSuggestionResponse?>(null)
    val currentSuggestion: StateFlow<TerminalSuggestionResponse?> = _currentSuggestion.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val secretToken = "Bearer 3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4"

    init {
        refreshAll()
        startPeriodicRefresh()
    }

    fun refreshAll() {
        fetchStatus()
        fetchVault()
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                fetchStatus()
            }
        }
    }

    fun fetchStatus() {
        viewModelScope.launch {
            try {
                val response = apiService.getTerminalStatus(secretToken)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    _terminalOutput.value = data.output
                    _cpu.value = "${data.telemetry.cpu}%"
                    _ram.value = "${data.telemetry.ram}%"
                    _disk.value = "${data.telemetry.disk}%"
                    _cwd.value = data.telemetry.cwd
                    _activeNode.value = data.active_node
                    _nodesList.value = data.nodes
                }
            } catch (e: Exception) {
                // Network glitch or backend offline
            }
        }
    }

    fun executeCommand(command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            try {
                apiService.executeCommand(secretToken, TerminalCommandRequest(command))
                fetchStatus()
            } catch (e: Exception) {
                _terminalOutput.value += "\nError executing command: ${e.message}\nPS C:\\Users\\Administrator> "
            }
        }
    }

    fun selectActiveClusterNode(nodeName: String) {
        viewModelScope.launch {
            try {
                apiService.selectActiveNode(secretToken, SelectNodeRequest(nodeName))
                fetchStatus()
            } catch (e: Exception) {
                // Handle err
            }
        }
    }

    fun addNewClusterNode(name: String, host: String, port: Int) {
        viewModelScope.launch {
            try {
                apiService.connectNewNode(secretToken, ConnectNodeRequest(name, host, port))
                fetchStatus()
            } catch (e: Exception) {
                // Handle err
            }
        }
    }

    fun askPilot(userGoal: String) {
        if (userGoal.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.suggestCommand(secretToken, TerminalSuggestionRequest(userGoal))
                if (response.isSuccessful && response.body() != null) {
                    _currentSuggestion.value = response.body()
                }
            } catch (e: Exception) {
                _currentSuggestion.value = TerminalSuggestionResponse(
                    command = "# Error",
                    explanation = "Failed to communicate with Pilot Agent."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchVault() {
        viewModelScope.launch {
            try {
                val response = apiService.getVaultCommands(secretToken)
                if (response.isSuccessful && response.body() != null) {
                    _vaultCommands.value = response.body()!!.commands
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun saveSuggestionToVault() {
        val suggestion = _currentSuggestion.value ?: return
        viewModelScope.launch {
            try {
                apiService.saveToVault(
                    secretToken, 
                    VaultSaveRequest(
                        name = suggestion.explanation.take(25) + "...",
                        cmd = suggestion.command
                    )
                )
                fetchVault()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun clearTerminal() {
        viewModelScope.launch {
            try {
                apiService.clearTerminal(secretToken)
                fetchStatus()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun dismissSuggestion() {
        _currentSuggestion.value = null
    }
}
