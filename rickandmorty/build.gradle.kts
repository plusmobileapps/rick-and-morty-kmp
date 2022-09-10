plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.squareup.sqldelight")
    id("maven-publish")
    kotlin("plugin.serialization")
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
        summary = "Rick and Morty SDK"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Deps.Jetbrains.coroutines)
                implementation(Deps.Jetbrains.serialization)
                implementation(Deps.RushWolf.multiplatformSettings)
                implementation(Deps.Ktor.core)
                implementation(Deps.Ktor.kotlinxSerialization)
                implementation(Deps.Ktor.logging)
                implementation(Deps.Ktor.contentNegotiation)
                implementation(Deps.SqlDelight.coroutines)
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
                implementation(Deps.SqlDelight.androidDriver)
            }
        }
        val androidTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(Deps.Ktor.darwinEngine)
                implementation(Deps.SqlDelight.nativeDriver)
            }
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

sqldelight {
    database("MyDatabase") {
        packageName = "com.plusmobileapps.rickandmorty.db"
        sourceFolders = listOf("sqldelight")
    }
}