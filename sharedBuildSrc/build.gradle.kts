plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    gradlePluginPortal()
    jcenter()
}

gradlePlugin {
    plugins.register("defaults") {
        id = "defaults"
        implementationClass = "com.noheltcj.shared.build.GlobalDefaultsPlugin"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

