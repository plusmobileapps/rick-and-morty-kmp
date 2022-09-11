import Deps.Compose.VERSION

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = Deps.Android.compileSDK

    defaultConfig {
        applicationId = "com.plusmobileapps.rickandmorty.androidapp"
        minSdk = Deps.Android.minSDK
        targetSdk = Deps.Android.targetSDK
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Deps.Compose.COMPILER_VERSION
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":sample:shared"))
    implementation(Deps.ArkIvanov.Decompose.extensionsCompose)
    implementation("io.coil-kt:coil:2.2.1")
    implementation("io.coil-kt:coil-compose:2.2.1")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.compose.ui:ui:$VERSION")
    implementation("androidx.compose.material3:material3:1.0.0-beta01")
    implementation("androidx.compose.ui:ui-tooling-preview:$VERSION")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.5.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$VERSION")
    debugImplementation("androidx.compose.ui:ui-tooling:$VERSION")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$VERSION")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += arrayOf(
            "-opt-in=com.arkivanov.decompose.ExperimentalDecomposeApi",
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
}