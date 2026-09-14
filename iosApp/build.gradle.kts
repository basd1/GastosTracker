plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.framework {
            baseName = "GastosTrackerApp"
            isStatic = true
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation(project(":di"))
            implementation(project(":presentation"))
            implementation(compose.ui)
        }
    }
}
