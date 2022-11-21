@file:Suppress("UnstableApiUsage")

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.dagger.hilt)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.kapt)
}

android {
  namespace = "com.evanisnor.handyauth.example"

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
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
  }
  kotlinOptions {
    languageVersion =
      Regex("(\\d+\\.\\d+)\\.\\d+").find(libs.versions.kotlin.get())!!.groupValues[1]
    jvmTarget = libs.versions.jvm.get()
  }
}

dependencies {
  implementation(project(":client"))

  implementation(libs.bundles.example.androidx.framework)
  implementation(libs.google.material)

  implementation(libs.google.dagger.hilt.android)
  kapt(libs.google.dagger.hilt.android.compiler)
}
