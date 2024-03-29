@file:Suppress("UnstableApiUsage")

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.google.ksp)
  alias(libs.plugins.jetbrains.dokka)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.parcelize)
}

android {
  namespace = "com.evanisnor.handyauth.client"

  compileSdk = 34

  defaultConfig {
    minSdk = 26
    consumerProguardFiles("consumer-rules.pro")
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  packaging {
    resources {
      excludes.add("META-INF/versions/**/*")
      pickFirsts.add("META-INF/AL2.0")
      pickFirsts.add("META-INF/LGPL2.1")
    }
  }

  buildFeatures {
    viewBinding = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
  }
  kotlinOptions {
    languageVersion =
      Regex("(\\d+\\.\\d+)\\.\\d+").find(libs.versions.kotlin.get())!!.groupValues[1]
    jvmTarget = libs.versions.jvm.get()
    freeCompilerArgs = listOf(
      "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
      "-opt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
    )
  }

  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }
  }
}

dependencies {
  // Runtime
  implementation(libs.bundles.client.androidx.framework)
  implementation(libs.square.moshi)
  ksp(libs.square.moshi.kotlin.codegen)
  implementation(libs.square.okhttp)
  implementation(libs.square.okio)
  implementation(libs.jetbrains.coroutines.android)

  // Debug
  debugImplementation(libs.square.okhttp.logging)

  // Documentation
  dokkaHtmlPlugin(libs.jetbrains.dokka.kotlinasjava)

  // Test
  testImplementation(libs.bundles.client.androidx.test)
  testImplementation(libs.jetbrains.test.coroutines)
  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
  testImplementation(libs.google.test.truth)
  kspTest(libs.square.moshi.kotlin.codegen)

  androidTestImplementation(libs.bundles.client.androidx.test)
  androidTestImplementation(libs.google.test.truth)
  androidTestImplementation(libs.square.test.mockwebserver)
  androidTestImplementation(libs.jetbrains.test.coroutines)
  kspAndroidTest(libs.square.moshi.kotlin.codegen)
}
