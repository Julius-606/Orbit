// ================================================================================
// FILE: Pocket_Orbit/build.gradle.kts
// VERSION: 1.0.3 | SYSTEM: Orbit Life-OS v4.0.0
// IDENTITY: The Holding Company / Root Gradle
// VIBE: Centralizing plugin management via Version Catalog. 🚀
// ================================================================================

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
