plugins {
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.android.library")
    id("maven-publish")
    kotlin("kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.eka.networking"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_DEBUG", "true")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_DEBUG", "false")
        }
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        buildConfig = true
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.eka.networking"
                artifactId = "eka-networking"
                version = "2.0.0"
            }
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.ok2curl)
    implementation(libs.converter.gson)
    implementation(libs.okhttp.brotli)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    api(libs.networkresponseadapter)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
}