// ================================================================================
// FILE: Pocket_Orbit/app/build.gradle.kts
// VERSION: 1.1.0 | SYSTEM: Orbit Life-OS v4.0.0
// IDENTITY: The Funding Account / App Gradle
// VIBE: Added Material Icons Extended for better UI semantics. 📈
// ================================================================================

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.pocket_orbit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pocket_orbit"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "4.0-Orbit-Life-OS"

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
}

dependencies {
    val room_version = "2.6.1"
    val retrofit_version = "2.9.0"
    val nav_version = "2.7.7"

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // 🔥 Added for better icon support (WifiOff, CloudOff, etc)
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:$nav_version")

    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")
    
    // 🔥 Added WorkManager for background sync and notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}