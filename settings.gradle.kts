pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "Rick_and_Morty_KMP"
include(":sample:shared")
include(":rick-and-morty-api")
includeBuild("convention-plugins")
include(":sample:androidapp")
