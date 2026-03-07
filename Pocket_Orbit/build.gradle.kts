// ================================================================================
// FILE: Pocket_Orbit/build.gradle.kts
// VERSION: 1.0.1 | SYSTEM: Orbit Life-OS v4.0.0
// IDENTITY: The Holding Company / Root Gradle
// VIBE: Upgraded to Kotlin 2.0.0 to dodge that compiler margin call. 🚀
// ================================================================================

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.2" apply false

    // 🔥 THE FIX: Moving the trendline up to Kotlin 2.0.0
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false

    // 🔥 THE NEW SAUCE: Kotlin 2.0 requires this specific Compose Compiler plugin
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}