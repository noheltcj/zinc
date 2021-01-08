plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

gradlePlugin {
    plugins.register("defaults") {
        id = "com.noheltcj.zinc.defaults"
        implementationClass = "com.noheltcj.zinc.shared.build.GlobalDefaultsPlugin"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

