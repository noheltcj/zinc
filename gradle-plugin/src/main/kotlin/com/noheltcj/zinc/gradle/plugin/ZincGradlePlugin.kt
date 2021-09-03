package com.noheltcj.zinc.gradle.plugin

import java.io.File
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.FilesSubpluginOption
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class ZincGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val project = kotlinCompilation.target.project
        val extension = requireNotNull(project.extensions.findByType(ZincGradleExtension::class.java))

        if (!extension.enabled) {
            return false
        }

        val compilationName = kotlinCompilation.name
        if (compilationName == PROD_COMPILATION_NAME) {
            return false
        }

        val compilationSourceSetNames = kotlinCompilation.kotlinSourceSets.map { it.name }
        extension.productionSourceSetNames.forEach { configuredSourceSetName ->
            if (compilationSourceSetNames.contains(configuredSourceSetName)) {
                return true
            }
        }

        return false
    }

    override fun getCompilerPluginId(): String = "com.noheltcj.zinc.compiler-plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.noheltcj.zinc",
        artifactId = "compiler-plugin",
        version = "0.1.0"
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        println("Applying to compilation: ${kotlinCompilation.compilationName}")

        val project = kotlinCompilation.target.project
        val extension = project.extensions.findByType(ZincGradleExtension::class.java) ?: ZincGradleExtension()

        val generatedSourcesDirectory = File(project.buildDir, zincProdGenDir)

        extension.productionSourceSetNames.forEach { configuredProductionSourceSetName ->
            kotlinCompilation.kotlinSourceSets.firstOrNull { it.name == configuredProductionSourceSetName }
                ?.also { kotlinSourceSet ->
                    println(
                        "Adding generated sources at $generatedSourcesDirectory to configured source set: " +
                            kotlinSourceSet.name
                    )
                    kotlinSourceSet.kotlin.srcDirs += generatedSourcesDirectory
                    kotlinSourceSet.dependencies {
                        implementation("com.noheltcj.zinc:core:0.1.0")
                    }
            }
        }

        return project.provider {
            listOf(
                SubpluginOption(key = "convert_data_classes", value = extension.enabled.toString()),
                FilesSubpluginOption(key = "generated_sources_directory", files = setOf(generatedSourcesDirectory))
            )
        }
    }

    override fun apply(target: Project): Unit = with(target) {
        extensions.create("zinc", ZincGradleExtension::class.java)
    }

    companion object {
        private const val PROD_COMPILATION_NAME = "zinc-prod"

        private val zincProdGenDir = "zinc${File.separator}generated${File.separator}source${File.separator}prod${File.separator}"
    }
}
