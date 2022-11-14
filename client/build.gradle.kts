plugins {
  id("com.android.library")
  id("kotlin-android")
  id("com.google.devtools.ksp").version("1.7.20-1.0.8")
  id("kotlin-parcelize")
  id("org.jetbrains.dokka") version "1.6.0"
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
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    languageVersion = "1.7"
    jvmTarget = "11"
    freeCompilerArgs = listOf(
      "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
      "-Xopt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
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
  implementation("androidx.appcompat:appcompat:1.5.1")
  implementation("androidx.activity:activity-ktx:1.6.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
  implementation("androidx.browser:browser:1.4.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
  implementation("com.squareup.okio:okio:3.0.0")
  implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.6")
  implementation("com.squareup.moshi:moshi:1.14.0")
  ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")

  // Debug
  debugImplementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.6")

  // Documentation
  dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.0")

  // Test
  testImplementation("androidx.test:core-ktx:1.4.0")
  testImplementation("androidx.test.ext:junit-ktx:1.1.3")
  testImplementation("junit:junit:4.13.2")
  testImplementation("com.google.truth:truth:1.1.3")

  androidTestImplementation("androidx.test:core-ktx:1.4.0")
  androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
  androidTestImplementation("com.google.truth:truth:1.1.3")
  androidTestImplementation("com.squareup.okhttp3:mockwebserver:5.0.0-alpha.6")
  androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
  kspAndroidTest("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
}
