object Dependencies {
    val targets = Targets
    object Targets {
        const val jvm = "org.jetbrains.kotlin.jvm"
        const val gradlePlugin = "org.gradle.java-gradle-plugin"
    }

    val plugins = Plugins
    object Plugins {
        const val idea = "idea"

        const val kotlinGradle= "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"

        const val mavenPublish = "com.vanniktech.maven.publish"

        const val ktlint = "org.jlleitschuh.gradle.ktlint"
    }

    val project = Project
    object Project {
        const val core = "com.noheltcj.zinc:core:${Versions.zinc}"

        val kotlin = Kotlin
        object Kotlin {
            const val annotationProcessingEmbedded = "org.jetbrains.kotlin:kotlin-annotation-processing-embeddable:${Versions.kotlin}"
            const val compilerEmbedded = "org.jetbrains.kotlin:kotlin-compiler-embeddable:${Versions.kotlin}"
            const val gradlePluginApi = "org.jetbrains.kotlin:kotlin-gradle-plugin-api:${Versions.kotlin}"
            const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
            const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
        }

        val google = Google
        object Google {
            val auto = AutoService
            object AutoService {
                const val annotations = "com.google.auto.service:auto-service-annotations:${Versions.autoService}"
                const val processor = "com.google.auto.service:auto-service:${Versions.autoService}"
            }
        }
    }

    val test = Test
    object Test {
        const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:${Versions.test.compileTesting}"

        const val truth = "com.google.truth:truth:${Versions.test.truth}"

        val spek = Spek
        object Spek {
            const val jvm = "org.spekframework.spek2:spek-dsl-jvm:${Versions.test.spek}"
            const val runner = "org.spekframework.spek2:spek-runner-junit5:${Versions.test.spek}"
        }
    }
}