plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.google.ksp)
  alias(libs.plugins.jetbrains.dokka)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.parcelize)
  id("maven-publish")
}

android {
  compileSdk = 33

  defaultConfig {
    minSdk = 26
    targetSdk = 33
    consumerProguardFiles("consumer-rules.pro")
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  packagingOptions {
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
}

afterEvaluate {
  publishing {
    publications {
      create<MavenPublication>("release") {
        from(components["release"])
        group = "com.evanisnor.handyauth"
        artifactId = "client"
        version = project.version.toString()
      }
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
  testImplementation(libs.junit)
  testImplementation(libs.google.test.truth)

  androidTestImplementation(libs.bundles.client.androidx.instrumentationtest)
  androidTestImplementation(libs.google.test.truth)
  androidTestImplementation(libs.square.test.mockwebserver)
  androidTestImplementation(libs.jetbrains.test.coroutines)
  kspAndroidTest(libs.square.moshi.kotlin.codegen)
}
