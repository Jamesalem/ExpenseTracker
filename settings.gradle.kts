// settings.gradle.kts
@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    plugins {
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
        // *** REMOVED: KSP plugin removed for Kapt migration ***
        // id("com.google.devtools.ksp") version "2.2.0-1.0.20"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS") // Optional for Gradle 8+

rootProject.name = "ExpenseTracker"
include(":app")
