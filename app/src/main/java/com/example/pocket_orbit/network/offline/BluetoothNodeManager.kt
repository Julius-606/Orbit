package com.example.pocket_orbit.network.offline

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isBonded: Boolean = true
)

sealed class BluetoothConnectionState {
    object Disconnected : BluetoothConnectionState()
    data class Connecting(val deviceName: String) : BluetoothConnectionState()
    data class Connected(val deviceName: String, val address: String) : BluetoothConnectionState()
    data class Error(val message: String) : BluetoothConnectionState()
}

data class BtCommandResult(
    val output: String,
    val exitCode: Int,
    val cwd: String = ""
)

object BluetoothNodeManager {
    private const val TAG = "BluetoothNodeManager"
    // Standard Serial Port Profile (SPP) UUID
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val _connectionState = MutableStateFlow<BluetoothConnectionState>(BluetoothConnectionState.Disconnected)
    val connectionState: StateFlow<BluetoothConnectionState> = _connectionState.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = _pairedDevices.asStateFlow()

    private var activeSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStreamReader: BufferedReader? = null
    private var listenerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    fun loadPairedDevices(context: Context): List<BluetoothDeviceInfo> {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null || !adapter.isEnabled) {
                _pairedDevices.value = emptyList()
                return emptyList()
            }
            val bonded = adapter.bondedDevices ?: emptySet()
            val list = bonded.map {
                BluetoothDeviceInfo(
                    name = it.name ?: "Unknown Device",
                    address = it.address,
                    isBonded = true
                )
            }.sortedBy { it.name }
            _pairedDevices.value = list
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching paired Bluetooth devices: ${e.message}")
            _pairedDevices.value = emptyList()
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(deviceAddress: String, deviceName: String): Boolean = withContext(Dispatchers.IO) {
        disconnect()
        _connectionState.value = BluetoothConnectionState.Connecting(deviceName)

        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
                ?: throw IllegalStateException("Bluetooth adapter not available")
            
            // Cancel discovery as it slows down connections
            try { adapter.cancelDiscovery() } catch (_: Exception) {}

            val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()

            activeSocket = socket
            outputStream = socket.outputStream
            inputStreamReader = BufferedReader(InputStreamReader(socket.inputStream))

            _connectionState.value = BluetoothConnectionState.Connected(deviceName, deviceAddress)
            Log.i(TAG, "Connected to Bluetooth Node $deviceName ($deviceAddress)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Bluetooth Node: ${e.message}")
            disconnect()
            _connectionState.value = BluetoothConnectionState.Error("Connection failed: ${e.localizedMessage ?: "Unknown error"}")
            false
        }
    }

    fun isConnected(): Boolean {
        return activeSocket?.isConnected == true
    }

    fun getConnectedDeviceName(): String? {
        val state = _connectionState.value
        return if (state is BluetoothConnectionState.Connected) state.deviceName else null
    }

    suspend fun executeCommand(command: String): BtCommandResult = withContext(Dispatchers.IO) {
        val out = outputStream ?: return@withContext BtCommandResult("Bluetooth node not connected", 1)
        val reader = inputStreamReader ?: return@withContext BtCommandResult("Bluetooth stream not readable", 1)

        try {
            val payload = JSONObject().apply {
                put("action", "execute")
                put("command", command)
            }
            // Send payload with newline delimiter
            val message = payload.toString() + "\n"
            out.write(message.toByteArray(Charsets.UTF_8))
            out.flush()

            // Read response line (JSON envelope from Python daemon)
            val responseLine = reader.readLine()
                ?: return@withContext BtCommandResult("No response from Bluetooth node", 1)

            val json = JSONObject(responseLine)
            val output = json.optString("output", "")
            val exitCode = json.optInt("exit_code", 0)
            val cwd = json.optString("cwd", "")

            BtCommandResult(output = output, exitCode = exitCode, cwd = cwd)
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth execute error: ${e.message}")
            BtCommandResult("Bluetooth error: ${e.message}", 1)
        }
    }

    suspend fun queryStatus(): JSONObject? = withContext(Dispatchers.IO) {
        val out = outputStream ?: return@withContext null
        val reader = inputStreamReader ?: return@withContext null

        try {
            val payload = JSONObject().apply {
                put("action", "status")
            }
            out.write((payload.toString() + "\n").toByteArray(Charsets.UTF_8))
            out.flush()

            val responseLine = reader.readLine() ?: return@withContext null
            JSONObject(responseLine)
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth status error: ${e.message}")
            null
        }
    }

    fun disconnect() {
        try {
            listenerJob?.cancel()
            listenerJob = null
            inputStreamReader?.close()
            outputStream?.close()
            activeSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting socket: ${e.message}")
        } finally {
            inputStreamReader = null
            outputStream = null
            activeSocket = null
            _connectionState.value = BluetoothConnectionState.Disconnected
        }
    }
}
