plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.kodein.mock.mockmp") version Deps.KosiLibs.MOCK_KMP_VERSION
    id("paging.publication")
}


group = "com.plusmobileapps"
version = Deps.PAGING_VERSION

kotlin {
    android {
        publishLibraryVariants("release", "debug")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Paging helper KMM library"
        homepage = "https://github.com/plusmobileapps/rick-and-morty-kmp"
        version = Deps.PAGING_VERSION
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "paging"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Deps.Jetbrains.coroutines)
                implementation(Deps.PlusMobileApps.konnectivity)
                implementation(Deps.RushWolf.multiplatformSettings)
                implementation(Deps.Jetbrains.dateTime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(Deps.Jetbrains.coroutinesTesting)
                implementation(Deps.CashApp.turbine)
                implementation(Deps.RushWolf.multiplatformSettingsTest)
                implementation(Deps.Kotest.assertionCore)
            }
        }
        val androidMain by getting
        val androidTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
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
    namespace = "com.plusmobileapps.paging"
    compileSdk = Deps.Android.compileSDK
    defaultConfig {
        minSdk = Deps.Android.minSDK
        targetSdk = Deps.Android.targetSDK
    }
}

mockmp {
    usesHelper = true
}

tasks.getByName<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest>("iosX64Test") {
    deviceId = "iPhone 14"
}