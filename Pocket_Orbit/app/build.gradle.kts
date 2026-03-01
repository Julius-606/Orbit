// ==========================================
// IDENTITY: The Funding Account / App Gradle
// FILEPATH: app/build.gradle.kts
// VIBE: Swapped KSP for KAPT because Android Studio was acting like an opp. 🛑
// ==========================================

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // The fix! KAPT works out of the box without root config drama.
}


android {
    namespace = "com.example.pocket_orbit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pocket_orbit"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "3.0-Jarvis"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    val room_version = "2.6.1"

    // Android Core & Compose
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // The Memory Card (Room DB)
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version") // Replaced ksp with kapt

    // The Antenna (OkHttp for WebSockets)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}