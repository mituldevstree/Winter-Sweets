// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinKapt) apply false
    alias(libs.plugins.parcelize) apply false
    alias(libs.plugins.navigation.safeargs) apply false
    alias(libs.plugins.fyber.fairbid.sdk) apply false
}
true