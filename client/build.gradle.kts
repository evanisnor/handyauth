plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 26
        targetSdk = 31
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packagingOptions {
        resources {
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
        languageVersion = "1.5"
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=kotlinx.coroutines.ObsoleteCoroutinesApi"
        )
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // Runtime
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.2")
    implementation("com.squareup.okio:okio:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.2")
    implementation("com.squareup.moshi:moshi:1.13.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")

    // Debug
    debugImplementation("com.squareup.okhttp3:logging-interceptor:4.9.2")

    // Test
    testImplementation("androidx.test:core-ktx:1.4.0")
    testImplementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.3")

    androidTestImplementation("androidx.test:core-ktx:1.4.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("com.google.truth:truth:1.1.3")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.9.2")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
    kaptAndroidTest("com.squareup.moshi:moshi-kotlin-codegen:1.12.0")

}