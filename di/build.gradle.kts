plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "bas.orellana.apppair.di"
    compileSdk = 34

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":presentation"))
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}