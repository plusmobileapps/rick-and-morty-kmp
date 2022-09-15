plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.squareup.sqldelight")
    id("maven-publish")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
    id("org.kodein.mock.mockmp") version "1.8.1"
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
        summary = "Rick and Morty KMP SDK"
        homepage = "https://github.com/plusmobileapps/rick-and-morty-kmp"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "rickandmortysdk"
            isStatic = false
            transitiveExport = true
            linkerOpts.add("-lsqlite3")
            export(Deps.ArkIvanov.Decompose.decompose)
            export(Deps.ArkIvanov.MVIKotlin.mvikotlinMain)
            export(Deps.ArkIvanov.MVIKotlin.mvikotlinTimeTravel)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":rick-and-morty-api"))
                implementation(Deps.Jetbrains.coroutines)
                implementation(Deps.Jetbrains.serialization)
                implementation(Deps.SqlDelight.coroutines)
                implementation(Deps.RushWolf.multiplatformSettings)
                api(Deps.ArkIvanov.Decompose.decompose)
                api(Deps.ArkIvanov.MVIKotlin.mvikotlin)
                api(Deps.ArkIvanov.MVIKotlin.mvikotlinMain)
                implementation(Deps.ArkIvanov.MVIKotlin.rx)
                implementation(Deps.ArkIvanov.MVIKotlin.mviKotlinExtensionsCoroutines)
                implementation(Deps.ArkIvanov.MVIKotlin.mvikotlinLogging)
                api(Deps.ArkIvanov.MVIKotlin.mvikotlinTimeTravel)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(Deps.Jetbrains.coroutinesTesting)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(Deps.SqlDelight.androidDriver)
            }
        }
        val androidTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependencies {
                implementation(Deps.SqlDelight.nativeDriver)
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

sqldelight {
    database("MyDatabase") {
        packageName = "com.plusmobileapps.rickandmorty.db"
        sourceFolders = listOf("sqldelight")
        linkSqlite = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += arrayOf(
            "-opt-in=com.kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}

mockmp {
    // OPTIONAL!
    usesHelper = true //(2)
}
