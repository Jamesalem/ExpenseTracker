// app/build.gradle.kts

// Import JvmTarget for the new compilerOptions DSL
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    // *** IMPORTANT CHANGE: Re-added kotlin("kapt") ***
    kotlin("kapt")
    // *** REMOVED: KSP plugin alias removed for Kapt migration ***
    // alias(libs.plugins.ksp)
}

hilt {
    enableAggregatingTask = true
}

android {
    namespace = "com.example.expensetracker"
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.example.expensetracker"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-Xcontext-receivers",
                "-opt-in=kotlin.RequiresOptIn",
                "-language-version", "2.0"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    // *** RE-ADDED: Kapt block for correctErrorTypes ***
    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // *** TEMPORARILY COMMENTED OUT: Places SDK ***
    // implementation(libs.places)
    coreLibraryDesugaring(libs.desugarJdkLibs)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime.livedata)

    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.hilt.android)
    // *** IMPORTANT CHANGE: Changed ksp to kapt for Hilt compiler ***
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.work)
    // *** IMPORTANT CHANGE: Changed ksp to kapt for AndroidX Hilt compiler ***
    kapt(libs.androidx.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // *** IMPORTANT CHANGE: Changed ksp to kapt for Room compiler ***
    // The noinspection comment is for IDE, not strictly needed for build but can remain
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.jetbrains.kotlinx.serialization.json)
    implementation(libs.jetbrains.kotlinx.coroutines.core)
    implementation(libs.jetbrains.kotlinx.coroutines.android)

    implementation(libs.coil.compose)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.biometric)

    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
