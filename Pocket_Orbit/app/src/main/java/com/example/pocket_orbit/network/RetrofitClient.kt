// ==========================================
// IDENTITY: The Engine Room / Retrofit Client
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/RetrofitClient.kt
// COMPONENT: Android Networking
// ROLE: Builds the HTTP client that actually fires requests to your VM.
// VIBE: Firing off API requests faster than a news candle spikes on NFP. 🕯️⚡
// ==========================================

package com.example.pocket_orbit.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 is the Android Emulator's way of saying "localhost"
    // Change this to your laptop's actual IP when testing on a real phone!
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // We set strict timeouts because if the VM doesn't reply in 15 seconds,
    // your trade is probably already in drawdown anyway. 💀
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}