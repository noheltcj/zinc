plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal()
}

gradlePlugin {
    plugins.register("defaults-plugin") {
        id = "defaults"
        implementationClass = "com.noheltcj.zinc.shared.build.GlobalDefaultsPlugin"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

