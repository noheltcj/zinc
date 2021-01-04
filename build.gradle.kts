plugins {
    id("defaults")
}

buildscript {
    dependencies {
        classpath("com.noheltcj.zinc:gradle-plugin")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.11.1")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
    }
}

subprojects {
    apply(plugin = "defaults")

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
                allWarningsAsErrors = true
            }
        }
    }

    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.noheltcj.zinc:core"))
                .with(project(":core"))
            substitute(module("com.noheltcj.zinc:compiler-plugin"))
                .with(project(":compiler-plugin"))
        }
    }
}
