// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/ui/screens/TerminalScreen.kt
// VERSION: 2.0.0 | SYSTEM: Collapsible Sidebar Cluster Vault Config System
// IDENTITY: Features a hidden sidebar triggered via three dots to view commands & connect machines.
// ================================================================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(viewModel: TerminalViewModel) {
    val output by viewModel.terminalOutput.collectAsState()
    val cpu by viewModel.cpu.collectAsState()
    val ram by viewModel.ram.collectAsState()
    val disk by viewModel.disk.collectAsState()
    val cwd by viewModel.cwd.collectAsState()
    val activeNode by viewModel.activeNode.collectAsState()
    val nodesList by viewModel.nodesList.collectAsState()
    val vaultCommands by viewModel.vaultCommands.collectAsState()
    val suggestion by viewModel.currentSuggestion.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var manualCommand by remember { mutableStateOf("") }
    var pilotPrompt by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Node creation inputs
    var showAddNodeDialog by remember { mutableStateOf(false) }
    var newNodeName by remember { mutableStateOf("") }
    var newNodeHost by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(260.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Clusters Block
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CLUSTER NODES 🖥️", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { showAddNodeDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Node Target")
                    }
                }
                
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp).padding(horizontal = 12.dp)) {
                    items(nodesList.size) { index ->
                        val nodeName = nodesList[index]
                        val isSelected = nodeName == activeNode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    viewModel.selectActiveClusterNode(nodeName)
                                    scope.launch { drawerState.close() }
                                }
                                .padding(10.dp)
                        ) {
                            Text(text = nodeName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Vault Block
                Text("📂 COMMAND VAULT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (vaultCommands.isEmpty()) {
                        Text("No saved commands.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                    } else {
                        vaultCommands.forEach { vaultItem ->
                            Button(
                                onClick = {
                                    viewModel.executeCommand(vaultItem.cmd)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(text = "🚀 ${vaultItem.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TERMINAL DECK [ $activeNode ]", fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Three dots collapse selector")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(12.dp)
            ) {
                // Telemetry Header Card
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column { Text("CPU", fontSize = 10.sp); Text(cpu, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                        Column { Text("RAM", fontSize = 10.sp); Text(ram, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) }
                        Column { Text("DISK", fontSize = 10.sp); Text(disk, fontWeight = FontWeight.Bold) }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Directory: $cwd", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("🛠️ Manual", fontSize = 12.sp) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("🤖 Auto Pilot", fontSize = 12.sp) })
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedTab == 0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        LaunchedEffect(output) { scrollState.animateScrollTo(scrollState.maxValue) }
                        Text(text = output, color = Color(0xFF00FF00), fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.fillMaxSize().verticalScroll(scrollState))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = manualCommand, onValueChange = { manualCommand = it }, modifier = Modifier.weight(1f), placeholder = { Text("Enter command...", fontSize = 12.sp) }, singleLine = true)
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(onClick = { if (manualCommand.isNotBlank()) { viewModel.executeCommand(manualCommand); manualCommand = "" } }) { Text("Run") }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(onClick = { viewModel.clearTerminal() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Clear") }
                    }
                } else {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        OutlinedTextField(value = pilotPrompt, onValueChange = { pilotPrompt = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Ask the pilot...") })
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = { viewModel.askPilot(pilotPrompt) }, modifier = Modifier.fillMaxWidth()) { Text("Ask Pilot 🚀") }

                        Card(modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp)) {
                            Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                                if (suggestion == null) {
                                    Text("No current suggestions.", fontSize = 12.sp, color = Color.Gray)
                                } else {
                                    Text(text = "Action: ${suggestion!!.explanation}", fontSize = 12.sp)
                                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF2D2D2D)).padding(8.dp)) {
                                        Text(text = suggestion!!.command, color = Color(0xFF61AFEF), fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Button(onClick = { viewModel.executeCommand(suggestion!!.command); viewModel.dismissSuggestion(); pilotPrompt = "" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28A745))) { Text("Execute") }
                                        Button(onClick = { viewModel.saveSuggestionToVault() }) { Text("Save") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNodeDialog) {
        AlertDialog(
            onDismissRequest = { showAddNodeDialog = false },
            title = { Text("Connect to Remote Computer Agent") },
            text = {
                Column {
                    OutlinedTextField(value = newNodeName, onValueChange = { newNodeName = it }, label = { Text("Node Hostname Name") })
                    OutlinedTextField(value = newNodeHost, onValueChange = { newNodeHost = it }, label = { Text("IP Address / Endpoint") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newNodeName.isNotBlank() && newNodeHost.isNotBlank()) {
                        viewModel.addNewClusterNode(newNodeName, newNodeHost, 8888)
                        newNodeName = ""
                        newNodeHost = ""
                        showAddNodeDialog = false
                    }
                }) { Text("CONNECT") }
            },
            dismissButton = { TextButton(onClick = { showAddNodeDialog = false }) { Text("CANCEL") } }
        )
    }
}
