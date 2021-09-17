rootProject.buildFileName = "build.gradle.kts"

include(
    ":example",
    ":core",
    ":compiler-plugin"
)

includeBuild("sharedBuildSrc")

includeBuild("gradle-plugin") {
    dependencySubstitution {
        substitute(module("com.noheltcj.zinc:gradle-plugin")).using(project(":"))
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenLocal()
    }
}
