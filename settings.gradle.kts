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
includeBuild("publishing-plugins/rick-and-morty-api-publish")
includeBuild("publishing-plugins/paging-publish")
include(":sample:androidapp")
include(":paging")
