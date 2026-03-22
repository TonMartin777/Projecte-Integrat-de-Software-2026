plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.ub.pis2526.projecte"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.ub.pis2526.projecte"
        minSdk = 28
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // Llibreries base d'Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)

    // --- CONFIGURACIÓ FIREBASE ---
    // El BoM gestiona les versions automàticament per a totes les llibreries de Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // No cal posar versions aquí perquè ja les gestiona el BoM
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")  // Realtime Database
    implementation("com.google.firebase:firebase-firestore") // Cloud Firestore

    // --- ALTRES ---
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}