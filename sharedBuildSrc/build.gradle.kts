plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
}

sourceSets {
    main {
        java {
            srcDir("src/main/kotlin")
        }
    }
}

gradlePlugin {
    plugins.register("defaults") {
        id = "defaults"
        implementationClass = "com.noheltcj.zinc.shared.build.GlobalDefaultsPlugin"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

