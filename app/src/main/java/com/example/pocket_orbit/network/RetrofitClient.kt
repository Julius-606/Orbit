// ==========================================
// IDENTITY: The Engine Room / Retrofit Client
// FILEPATH: app/src/main/java/com/example/pocket_orbit/network/RetrofitClient.kt
// COMPONENT: Android Networking
// ROLE: Builds dynamic HTTP client for Hugging Face Spaces & Ngrok tunnels.
// VIBE: Seamlessly switches endpoints, bypasses ngrok warnings, and prevents 404 deadlocks. ⚡
// ==========================================

package com.example.pocket_orbit.network

import android.content.Context
import android.content.SharedPreferences
import com.example.pocket_orbit.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val PREFS_NAME = "orbit_network_prefs"
    private const val KEY_BASE_URL = "custom_base_url"

    private var sharedPrefs: SharedPreferences? = null

    var currentBaseUrl: String = BuildConfig.ORBIT_BASE_URL
        private set

    fun initialize(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = sharedPrefs?.getString(KEY_BASE_URL, null)
        if (!saved.isNullOrBlank()) {
            currentBaseUrl = if (saved.endsWith("/")) saved else "$saved/"
        }
        rebuildRetrofit()
    }

    fun updateBaseUrl(newUrl: String) {
        if (newUrl.isBlank()) return
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        currentBaseUrl = formatted
        sharedPrefs?.edit()?.putString(KEY_BASE_URL, formatted)?.apply()
        rebuildRetrofit()
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            // Bypass Ngrok free tier browser warning interstitial page
            val request = original.newBuilder()
                .header("ngrok-skip-browser-warning", "true")
                .header("User-Agent", "PocketOrbit-Android/4.0")
                .build()
            chain.proceed(request)
        }
        .build()

    @Volatile
    private var _apiService: ApiService? = null

    val apiService: ApiService
        get() {
            return _apiService ?: synchronized(this) {
                _apiService ?: buildApiService().also { _apiService = it }
            }
        }

    private fun rebuildRetrofit() {
        synchronized(this) {
            _apiService = buildApiService()
        }
    }

    private fun buildApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
