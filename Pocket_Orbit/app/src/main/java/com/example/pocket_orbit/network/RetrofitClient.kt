// ==========================================
// IDENTITY: The Engine Room / Retrofit Client
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/RetrofitClient.kt
// COMPONENT: Android Networking
// ROLE: Builds the HTTP client with a multi-link failover system.
// VIBE: Hedging our bets. If local fails, we hit Ngrok. If Ngrok fails, HF Space saves the account from blowing. 🛡️📈
// VERSION: 1.1.0
// ==========================================

package com.example.pocket_orbit.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 🎯 THE HEDGE FUND PORTFOLIO: Multiple URLs to ensure we never miss a task/trade
    // Order matters: It will try index 0 first, then 1, then 2.
    // Make sure to swap the Local IP and Ngrok URL to your actual current ones!
    private val BASE_URLS = listOf(
        "http://10.0.2.2:8000/", // Local Emulator (Use your actual laptop's Wi-Fi IP if testing on physical phone, e.g., http://192.168.x.x:8000/)
        "https://YOUR_NGROK_ID.ngrok-free.app/", // Ngrok Fallback
        "https://agent606-orbit.hf.space/" // Hugging Face (The Final Boss)
    )

    // The Smart Interceptor that acts like a Trailing Stop Loss
    // If one server connection times out, it dynamically reroutes to the next one without crashing the app.
    private class FailoverInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            var exception: IOException? = null

            for (url in BASE_URLS) {
                val parsedUrl = url.toHttpUrlOrNull() ?: continue
                
                // Rewrite the request URL to point to the current fallback server
                val newUrl = request.url.newBuilder()
                    .scheme(parsedUrl.scheme)
                    .host(parsedUrl.host)
                    .port(parsedUrl.port)
                    .build()

                request = request.newBuilder().url(newUrl).build()

                try {
                    // Attempt to execute the request
                    // If the server connects (even if it returns a 404/500 error), we return the response.
                    // We ONLY failover if there is an absolute network failure/timeout (IOException).
                    return chain.proceed(request)
                } catch (e: IOException) {
                    // Server is ghosting us (offline or timing out). Save the error and loop to the next one.
                    exception = e
                }
            }
            
            // If we get here, all 3 servers are down. Account blown. 💀
            throw exception ?: IOException("Margin Call! All Orbit servers are offline. Check your Safaricom connection bro.")
        }
    }

    // We set strict timeouts. 5 seconds per attempt means a max 15-second wait if it has to check all 3.
    // You don't want the UI freezing for a whole minute just because Local and Ngrok are down.
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(FailoverInterceptor())
        .connectTimeout(5, TimeUnit.SECONDS) // Lowered so it fails over faster
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            // Retrofit needs a default URL just to compile properly, so we feed it the first one.
            // The FailoverInterceptor will handle the actual routing mid-flight! ✈️
            .baseUrl(BASE_URLS.first())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
