rootProject.buildFileName = "build.gradle.kts"
rootProject.name = "gradle-plugin"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}