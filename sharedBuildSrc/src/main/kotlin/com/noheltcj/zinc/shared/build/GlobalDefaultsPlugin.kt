package com.noheltcj.zinc.shared.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.repositories

class GlobalDefaultsPlugin : Plugin<Project> {

    private val publishingWhitelist = setOf(
        "compiler-plugin",
        "core",
        "gradle-plugin"
    )

    override fun apply(target: Project) {
        target.repositories {
            mavenCentral()
            mavenLocal()
            google()
        }

        target.plugins {
            apply(Dependencies.targets.jvm)

            apply(Dependencies.plugins.ktlint)
        }

        if (target.name != "zinc") {
            target.loadProjectProperties()

            target.group = target.loadStringProperty("zincGroupId")
            target.version = target.loadStringProperty("zincVersion")

            if (publishingWhitelist.contains(target.name)) {
                target.configurePublishing()
            }

            val sourceSets = target.extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
            sourceSets["main"].java.srcDir("src/main/kotlin")
            sourceSets["test"].java.srcDir("src/test/kotlin")

            target.tasks.named("test", Test::class) {
                useJUnitPlatform {
                    includeEngines = setOf("spek2")
                }
            }

            // Kotlin
            target.dependencies.add("implementation", Dependencies.project.kotlin.reflect)
            target.dependencies.add("implementation", Dependencies.project.kotlin.stdlib)

            // Assertion Library
            target.dependencies.add("testImplementation", Dependencies.test.truth)

            // Test Runner
            target.dependencies.add("testImplementation", Dependencies.test.spek.jvm)
            target.dependencies.add("testRuntimeOnly", Dependencies.test.spek.runner)

            target.tasks.named("clean", Delete::class) {
                delete.add(target.buildDir)

                target.project.gradle.includedBuilds.forEach { build ->
                    dependsOn(build.task(":clean"))
                }
            }
        }
    }

    private fun Project.configurePublishing() {
        setProperty("GROUP", group)
        setProperty("VERSION_NAME", version)
        setProperty("POM_ARTIFACT_ID", name)

        plugins {
            apply(Dependencies.plugins.mavenPublish)
        }
    }
}