plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
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

        // TEMPORÄRER WORKAROUND: Verwende Web Client ID für App UND Server
        // bis das Android Client ID Problem in Google Console gelöst ist
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"871578886913-glsn59sioeea2t0qjdkevr1mhiiivfvj.apps.googleusercontent.com\"")

        // Web Client ID - für den Server (gleiche wie oben)
        buildConfigField("String", "WEB_CLIENT_ID", "\"871578886913-glsn59sioeea2t0qjdkevr1mhiiivfvj.apps.googleusercontent.com\"")

        // Android Client ID - für späteren Gebrauch wenn Google Console Problem gelöst
        buildConfigField("String", "ANDROID_CLIENT_ID", "\"871578886913-8kr48rb5qhqfl00h2etd39smadtre5qe.apps.googleusercontent.com\"")

        // SHA-1 Debug Info
        buildConfigField("String", "DEBUG_SHA1", "\"E3:D2:D2:1E:06:1D:14:ED:D1:4B:5D:22:38:48:7D:65:E6:D5:AA:2C\"")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Hilt dependencies
    implementation(libs.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    //javax.inject
    implementation(libs.javax.inject)

    //Google Identity
    implementation(libs.play.services.identity)
    implementation(libs.play.services.auth)

    // Retrofit dependencies
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

}