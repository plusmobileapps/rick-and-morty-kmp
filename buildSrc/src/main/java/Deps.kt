object Deps {

    const val LIBRARY_VERSION = "0.2"

    object Jetbrains {
        const val KOTLIN_VERSION = "1.7.20"
        const val COROUTINES_VERSION = "1.6.4"

        const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION"
        const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3"

        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION"
        const val coroutinesTesting =
            "org.jetbrains.kotlinx:kotlinx-coroutines-test:$COROUTINES_VERSION"

        const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.4.0"
    }

    object Ktor {
        const val VERSION = "2.1.0"

        const val core = "io.ktor:ktor-client-core:$VERSION"
        const val contentNegotiation = "io.ktor:ktor-client-content-negotiation:$VERSION"
        const val kotlinxSerialization = "io.ktor:ktor-serialization-kotlinx-json:$VERSION"
        const val logging = "io.ktor:ktor-client-logging:$VERSION"

        const val cioEngine = "io.ktor:ktor-client-cio:$VERSION"
        const val jvmEngine = "io.ktor:ktor-client-java:$VERSION"
        const val androidEngine = "io.ktor:ktor-client-android:$VERSION"
        const val jsEngine = "io.ktor:ktor-client-js:$VERSION"
        const val curlEngine = "io.ktor:ktor-client-curl:$VERSION"
        const val darwinEngine = "io.ktor:ktor-client-darwin:$VERSION"
        const val mockEngine = "io.ktor:ktor-client-mock:$VERSION"
    }

    object Android {
        const val compileSDK = 33
        const val minSDK = 21
        const val targetSDK = 33

        const val androidGradlePlugin = "com.android.tools.build:gradle:7.2.2"
    }

    object Compose {
        const val VERSION = "1.3.1"
        const val COMPILER_VERSION = "1.3.2"
        const val ACCOMPANIST_VERSION = "0.25.1"
        const val MATERIAL_VERSION = "1.0.0-beta03"
    }

    object Logging {
        const val napier = "io.github.aakira:napier:2.6.1"
    }

    object RushWolf {
        const val VERSION = "1.0.0-RC"
        const val multiplatformSettings = "com.russhwolf:multiplatform-settings-no-arg:$VERSION"
        const val multiplatformSettingsTest = "com.russhwolf:multiplatform-settings-test:$VERSION"
    }

    object SqlDelight {
        const val VERSION = "1.5.3"
        const val gradlePlugin = "com.squareup.sqldelight:gradle-plugin:$VERSION"
        const val androidDriver = "com.squareup.sqldelight:android-driver:$VERSION"
        const val nativeDriver = "com.squareup.sqldelight:native-driver:$VERSION"
        const val coroutines = "com.squareup.sqldelight:coroutines-extensions:$VERSION"
    }

    object ArkIvanov {
        object MVIKotlin {
            private const val VERSION = "3.0.2"
            const val rx = "com.arkivanov.mvikotlin:rx:$VERSION"
            const val mvikotlin = "com.arkivanov.mvikotlin:mvikotlin:$VERSION"
            const val mvikotlinMain = "com.arkivanov.mvikotlin:mvikotlin-main:$VERSION"
            const val mvikotlinLogging = "com.arkivanov.mvikotlin:mvikotlin-logging:$VERSION"
            const val mvikotlinTimeTravel = "com.arkivanov.mvikotlin:mvikotlin-timetravel:$VERSION"
            const val mviKotlinExtensionsCoroutines = "com.arkivanov.mvikotlin:mvikotlin-extensions-coroutines:$VERSION"
        }

        object Decompose {
            private const val VERSION = "1.0.0-alpha-04"
            const val decompose = "com.arkivanov.decompose:decompose:$VERSION"
            const val extensionsCompose = "com.arkivanov.decompose:extensions-compose-jetpack:$VERSION"
        }
    }

    object KosiLibs {
        const val MOCK_KMP_VERSION = "1.10.0"
    }

    object CashApp {
        const val turbine = "app.cash.turbine:turbine:0.12.1"
    }

    object PlusMobileApps {
        const val konnectivity = "com.plusmobileapps:konnectivity:0.1-alpha01"
    }

    object Kotest {
        const val VERSION = "5.5.4"
        const val assertionCore = "io.kotest:kotest-assertions-core:$VERSION"
    }
}