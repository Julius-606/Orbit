// ================================================================================
// FILE: Pocket_Orbit/app/build.gradle.kts
// VERSION: 1.1.10 | SYSTEM: Orbit Life-OS v4.0.0
// IDENTITY: The Funding Account / App Gradle
// VIBE: Kotlin 2.2.10, Room 2.6.1 and SDK 36 with KSP2 fix. 🚀
// ================================================================================

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.pocket_orbit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pocket_orbit"
        minSdk = 26
        targetSdk = 36
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

// 🔥 Fix for KSP2 "unexpected jvm signature V" error with Room 2.6.x
// Disabling generateKotlin forces Room to generate Java code, avoiding the KSP2 bug.
ksp {
    arg("room.generateKotlin", "false")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.okhttp)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    
    implementation(libs.androidx.work.runtime.ktx)
}
