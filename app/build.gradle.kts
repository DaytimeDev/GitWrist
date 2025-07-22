plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.watchappgesture"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.watchappgesture"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.13"

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
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.material3.android)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.v100alpha05)
    implementation(libs.room.common.jvm)
    implementation(libs.wear.remote.interactions)
    implementation(libs.compose.navigation)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // Dependencies for GitHub authentication
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.google.android.gms:play-services-wearable:18.1.0") // Opening links on the phone

    // New Storage Library
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // For handling json
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")

    // Loading in images from urls
    implementation("io.coil-kt:coil-compose:2.7.0")

    // QR Code Generator for GitHub authentication
    implementation("com.google.zxing:core:3.5.3")

    // The compose material 3 library for things such as icons
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material3:material3-window-size-class:1.3.2")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha18")


}