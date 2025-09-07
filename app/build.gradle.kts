plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    kotlin("kapt")
}

android {
    namespace = "app.chesspresso"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.chesspresso"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Base URL für API konfigurierbar
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Obfuskation für Release-Build aktivieren
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

// AndroidX Core & Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3.lint)

// Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)

// Navigation
    implementation(libs.androidx.navigation.compose)

// Material Design
    implementation(libs.material3)

// Hilt & Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.javax.inject)

// Netzwerk (Retrofit, OkHttp, STOMP)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.java.websocket)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

// DataStore & Persistenz
    implementation(libs.androidx.datastore.preferences)

// QR-Code & Kamera
    implementation(libs.zxing.android.embedded)
    implementation(libs.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.androidx.appcompat)

// ML Kit
    implementation(libs.barcode.scanning)

// JSON/Serialization
    implementation(libs.kotlinx.serialization.json)

// Compose Icons
    implementation(libs.androidx.material.icons.extended)

// Testing
// Unit Tests
    testImplementation(libs.junit)
// Instrumentation Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
// Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}