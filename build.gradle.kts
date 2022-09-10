buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Deps.Jetbrains.kotlinGradlePlugin)
        classpath(Deps.Android.androidGradlePlugin)
        classpath(Deps.SqlDelight.gradlePlugin)
        classpath(kotlin("serialization", version = Deps.Jetbrains.KOTLIN_VERSION))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}