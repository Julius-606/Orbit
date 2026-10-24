package com.example.pocket_orbit.network.offline

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.net.URL

data class NetworkInterfaceInfo(
    val name: String,
    val displayName: String,
    val ipAddress: String,
    val subnetPrefix: String, // e.g. "192.168.43"
    val interfaceType: OfflineInterfaceType
)

enum class OfflineInterfaceType {
    HOTSPOT,       // 192.168.43.x or ap0 / swlan0
    USB_TETHERING, // 192.168.42.x or rndis0 / usb0
    WIFI_LAN,      // wlan0 (192.168.1.x, etc.)
    OTHER
}

data class DiscoveredOfflineNode(
    val name: String,
    val ip: String,
    val port: Int,
    val interfaceType: OfflineInterfaceType,
    val osName: String = "Unknown",
    val cpuUsage: Double = 0.0,
    val ramUsage: Double = 0.0,
    val isDaemon: Boolean = true
) {
    val fullUrl: String get() = "http://$ip:$port"
}

object OfflineNetworkScanner {
    private const val TAG = "OfflineScanner"
    val DEFAULT_PROBE_PORTS = listOf(8888, 8000, 7860)

    /**
     * Inspects active network interfaces and returns info for Hotspot, USB tethering, and Wi-Fi.
     */
    fun getActiveOfflineInterfaces(): List<NetworkInterfaceInfo> {
        val result = mutableListOf<NetworkInterfaceInfo>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return emptyList()
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                val addrs = intf.inetAddresses ?: continue
                for (addr in addrs) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val hostAddr = addr.hostAddress ?: continue
                        val parts = hostAddr.split(".")
                        if (parts.size == 4) {
                            val prefix = "${parts[0]}.${parts[1]}.${parts[2]}"
                            val lowerName = intf.name.lowercase()

                            val type = when {
                                lowerName.contains("rndis") || lowerName.contains("usb") || prefix == "192.168.42" -> {
                                    OfflineInterfaceType.USB_TETHERING
                                }
                                lowerName.contains("ap") || lowerName.contains("softap") || prefix == "192.168.43" -> {
                                    OfflineInterfaceType.HOTSPOT
                                }
                                lowerName.contains("wlan") -> {
                                    OfflineInterfaceType.WIFI_LAN
                                }
                                else -> OfflineInterfaceType.OTHER
                            }

                            result.add(
                                NetworkInterfaceInfo(
                                    name = intf.name,
                                    displayName = intf.displayName ?: intf.name,
                                    ipAddress = hostAddr,
                                    subnetPrefix = prefix,
                                    interfaceType = type
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enumerate network interfaces: ${e.message}")
        }
        return result
    }

    /**
     * Scans the given subnet prefix (e.g. "192.168.43") or auto-detected tethering interfaces
     * across common ports (8888, 8000, 7860) using lightweight socket checks.
     */
    suspend fun scanSubnet(
        customPrefix: String? = null,
        ports: List<Int> = DEFAULT_PROBE_PORTS,
        timeoutMs: Int = 200,
        onProgress: (scannedCount: Int, total: Int) -> Unit = { _, _ -> }
    ): List<DiscoveredOfflineNode> = withContext(Dispatchers.IO) {
        val discovered = mutableListOf<DiscoveredOfflineNode>()

        val activeInterfaces = getActiveOfflineInterfaces()
        val prefixesToScan = mutableSetOf<Pair<String, OfflineInterfaceType>>()

        if (!customPrefix.isNullOrBlank()) {
            prefixesToScan.add(customPrefix.trim() to OfflineInterfaceType.OTHER)
        } else {
            // Prioritize USB Tethering (192.168.42.x) and Hotspot (192.168.43.x)
            for (info in activeInterfaces) {
                prefixesToScan.add(info.subnetPrefix to info.interfaceType)
            }
            // Always ensure standard tethering subnets are checked if no active interfaces were identified
            if (prefixesToScan.isEmpty()) {
                prefixesToScan.add("192.168.43" to OfflineInterfaceType.HOTSPOT)
                prefixesToScan.add("192.168.42" to OfflineInterfaceType.USB_TETHERING)
            }
        }

        // Candidates to scan: Gateway (1, 129) and standard peer ranges
        for ((prefix, ifType) in prefixesToScan) {
            val candidateHosts = mutableListOf<String>()

            // Quick candidates: 1 (typical phone gateway), 2 (first DHCP lease), 100, 101, 129 (USB tethering PC), etc.
            val highPriority = listOf(1, 2, 3, 4, 5, 10, 15, 20, 100, 101, 128, 129, 130, 254)
            for (lastOctet in highPriority) {
                candidateHosts.add("$prefix.$lastOctet")
            }
            // Also append full range 1..60 for fast coverage
            for (lastOctet in 6..60) {
                val ip = "$prefix.$lastOctet"
                if (!candidateHosts.contains(ip)) candidateHosts.add(ip)
            }

            val totalTasks = candidateHosts.size * ports.size
            var completedCount = 0

            // Run in concurrent chunks of 25 to avoid exhausting sockets
            val chunkSize = 25
            for (chunk in candidateHosts.chunked(chunkSize)) {
                val jobs = chunk.map { ip ->
                    async {
                        for (port in ports) {
                            if (isPortOpen(ip, port, timeoutMs)) {
                                val nodeInfo = probeNodeDetails(ip, port, ifType)
                                if (nodeInfo != null) {
                                    synchronized(discovered) {
                                        if (discovered.none { it.ip == ip && it.port == port }) {
                                            discovered.add(nodeInfo)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                jobs.awaitAll()
                completedCount += chunk.size * ports.size
                onProgress(completedCount, totalTasks)
            }
        }

        discovered
    }

    private fun isPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun probeNodeDetails(ip: String, port: Int, interfaceType: OfflineInterfaceType): DiscoveredOfflineNode? {
        // Try probing /status (lightweight daemon) or /terminal/status (full Orbit backend)
        val endpoints = listOf("http://$ip:$port/status", "http://$ip:$port/terminal/status")
        for (ep in endpoints) {
            try {
                val url = URL(ep)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 800
                    readTimeout = 800
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                }
                if (conn.responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val body = reader.readText()
                    reader.close()

                    val json = JSONObject(body)
                    val os = json.optString("system", json.optString("os", "Local Machine"))
                    val cpu = json.optDouble("cpu_usage", json.optDouble("cpu", 0.0))
                    val ram = json.optDouble("ram_usage", json.optDouble("ram", 0.0))
                    val name = json.optString("node_name", json.optString("hostname", "PC-$ip"))

                    return DiscoveredOfflineNode(
                        name = if (name.isNotBlank()) name else "Offline-Node-$ip",
                        ip = ip,
                        port = port,
                        interfaceType = interfaceType,
                        osName = os,
                        cpuUsage = cpu,
                        ramUsage = ram,
                        isDaemon = ep.endsWith("/status")
                    )
                }
            } catch (_: Exception) {
                // Try next endpoint
            }
        }

        // If port is open but endpoint didn't respond with JSON, treat as generic raw node
        return DiscoveredOfflineNode(
            name = "Offline-Device-$ip",
            ip = ip,
            port = port,
            interfaceType = interfaceType,
            osName = "Generic Node"
        )
    }
}
