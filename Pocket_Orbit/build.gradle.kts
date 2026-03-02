// ==========================================
// IDENTITY: The Top-Level Broker / Root Gradle
// FILEPATH: Pocket_Orbit/build.gradle.kts
// SYSTEM VERSION: Android v3.0.1
// VIBE: Removed the 'libs' alias so Android Studio stops giving us margin calls. 📉->📈
// ==========================================

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Hardcoded versions so we don't need a version catalog (libs.versions.toml)
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}
