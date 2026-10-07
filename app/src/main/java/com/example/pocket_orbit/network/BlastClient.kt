// ==========================================
// IDENTITY: The Antenna / Android WS Client
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/BlastClient.kt
// VIBE: Ready to catch those TP hits and blast them to your phone. 🔔🚀
// ==========================================

package com.example.pocket_orbit.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class BlastClient(private val url: String) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder().url(url).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i("BlastClient", "Connected to Orbit VM! 🪐")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.i("BlastClient", "INCOMING BLAST: $text")
                try {
                    val json = JSONObject(text)
                    val type = json.getString("type")
                    val data = json.getJSONObject("data")

                    if (type == "TRADE_UPDATE") {
                        Log.w("Forex Guardian", data.getString("message"))
                    }
                } catch (e: Exception) {
                    Log.e("BlastClient", "Failed to parse Blast: ${e.message}")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i("BlastClient", "Disconnected. Reconnecting in 5s...")
            }
        }
        webSocket = client.newWebSocket(request, listener)
    }

    fun disconnect() {
        webSocket?.close(1000, "User went to sleep.")
    }
}