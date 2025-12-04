plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

android {
    namespace = "com.example.mobiledev"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mobiledev"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation(libs.generativeai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.firebase.auth.ktx)       // Firebase Auth
    implementation(libs.firebase.firestore.ktx)  // Firebase Firestore
    implementation(libs.firebase.storage.ktx)    // Firebase Storage
    implementation(libs.play.services.location)  // Google Location Services
    implementation("io.coil-kt:coil-compose:2.6.0") // Coil for image loading
    implementation("io.coil-kt:coil-base:2.6.0")
    implementation("io.coil-kt:coil-gif:2.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.8") // For Icons.Filled.Chat
    implementation("androidx.compose.material:material:1.6.8") // For pull-to-refresh

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1") // Use ksp for annotation processing
    implementation("androidx.room:room-ktx:2.6.1") // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-paging:2.6.1") // Paging 3 Integration

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

}
