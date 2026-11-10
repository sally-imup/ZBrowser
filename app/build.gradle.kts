import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val geckoviewVersion: String = providers.gradleProperty("geckoviewVersion").get()

android {
    namespace = "com.sally.zbrowser"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sally.zbrowser"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        // Hanya arm64 (Oppo K3 pakai arm64). Tiap ABI menambah ~100 MB karena libxul.so GeckoView.
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildFeatures {
        compose = true
    }

    // Lint bawaan AGP 8.7.3 crash dengan Kotlin 2.3 (IncompatibleClassChangeError).
    // Matikan pengecekan lint di build release sampai AGP dinaikkan.
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }
}

// Paksa androidx.core ke versi yang kompatibel dengan AGP 8.7.3.
// Versi 1.17.0 ikut terbawa dari dependency lain dan butuh AGP 8.9.1+.
configurations.all {
    resolutionStrategy {
        force("androidx.core:core:1.15.0", "androidx.core:core-ktx:1.15.0")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("org.mozilla.geckoview:geckoview:$geckoviewVersion")
}
