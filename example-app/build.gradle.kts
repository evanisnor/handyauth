plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.evanisnor.handyauth.example"
        minSdk = 27
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["redirectUriScheme"] = "com.evanisnor.freshwaves"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        languageVersion = "1.7"
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":client"))

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.fragment:fragment-ktx:1.5.4")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
}