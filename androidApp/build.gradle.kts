plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("com.google.gms.google-services")
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":composeApp"))
                
                // Koin for Android
                val koinVersion = "3.5.3"
                implementation("io.insert-koin:koin-android:$koinVersion")

                // Firebase
                implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
                implementation("com.google.firebase:firebase-auth-ktx")
                implementation("com.google.firebase:firebase-firestore-ktx")

                // Google Play Services (required for Firebase)
                implementation("com.google.android.gms:play-services-base:18.3.0")
                implementation("com.google.android.gms:play-services-location:21.0.1")

                // Coroutines for Firebase
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

                // Mapbox for maps
                implementation("com.mapbox.maps:android:11.0.0")
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.swiftroute.app"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.swiftroute.app"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
