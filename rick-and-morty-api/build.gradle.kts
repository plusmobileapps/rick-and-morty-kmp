plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("maven-publish")
    kotlin("plugin.serialization")
    id("convention.publication")
}

group = "com.plusmobileapps"
version = Deps.LIBRARY_VERSION

kotlin {
    android {
        publishLibraryVariants("release", "debug")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Rick and Morty KMP API Http Client"
        homepage = "https://github.com/plusmobileapps/rick-and-morty-kmp"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "rick-and-morty-api"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Deps.Jetbrains.coroutines)
                implementation(Deps.Jetbrains.serialization)
                implementation(Deps.Ktor.core)
                implementation(Deps.Ktor.kotlinxSerialization)
                implementation(Deps.Ktor.logging)
                implementation(Deps.Ktor.contentNegotiation)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(Deps.Jetbrains.coroutinesTesting)
                implementation(Deps.Ktor.mockEngine)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(Deps.Ktor.androidEngine)
            }
        }
        val androidTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependencies {
                implementation(Deps.Ktor.darwinEngine)
            }
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    compileSdk = Deps.Android.compileSDK
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = Deps.Android.minSDK
        targetSdk = Deps.Android.targetSDK
    }
}