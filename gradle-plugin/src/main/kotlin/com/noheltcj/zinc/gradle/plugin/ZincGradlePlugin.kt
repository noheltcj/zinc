package com.noheltcj.zinc.gradle.plugin

import java.io.File
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.FilesSubpluginOption
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class ZincGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val project = kotlinCompilation.target.project
        val extension = requireNotNull(project.extensions.findByType(ZincGradleExtension::class.java))

        if (!extension.enabled) {
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

        val newlyConfiguredSourceSets = configureApplicableSourceSets(
            compilation = kotlinCompilation,
            existingSourceSets = extension.configuredSourceSets,
            includedSourceSetNames = extension.productionSourceSetNames
        )

        extension.configuredSourceSets += newlyConfiguredSourceSets

        kotlinCompilation.dependencies {
            implementation("com.noheltcj.zinc:core:0.1.0")
        }

        return project.provider {
            listOf(
                SubpluginOption(
                    key = "convert_data_classes",
                    value = extension.enabled.toString()
                ),
                FilesSubpluginOption(
                    key = "generated_sources_directory",
                    files = newlyConfiguredSourceSets.map { it.generatedSourcesDirectory }
                )
            )
        }
    }

    override fun apply(target: Project): Unit = with(target) {
        extensions.create("zinc", ZincGradleExtension::class.java)
    }

    private fun configureApplicableSourceSets(
        compilation: KotlinCompilation<*>,
        existingSourceSets: Set<ZincSourceSetDescriptor>,
        includedSourceSetNames: Set<String>
    ): Set<ZincSourceSetDescriptor> =
        compilation.allKotlinSourceSets
            .asSequence()
            .plus(compilation.defaultSourceSet)
            .map { detectedSourceSet ->
                createSourceSetDescriptor(
                    projectBuildDir = compilation.target.project.buildDir,
                    sourceSet = detectedSourceSet
                ) to detectedSourceSet
            }
            .filter { (descriptor, _) ->
                includedSourceSetNames.contains(descriptor.name) && !existingSourceSets.contains(descriptor)
            }
            .onEach { (descriptor, correspondingSourceSet) ->
                println(
                    "Adding generated sources at ${descriptor.generatedSourcesDirectory} to newly configured source " +
                        "set: ${correspondingSourceSet.name}"
                )
                correspondingSourceSet.kotlin.srcDir(descriptor.generatedSourcesDirectory)
            }
            .map { (descriptor, _) -> descriptor }
            .toSet()

    private fun createSourceSetDescriptor(projectBuildDir: File, sourceSet: KotlinSourceSet) = ZincSourceSetDescriptor(
        name = sourceSet.name,
        generatedSourcesDirectory = File(
            projectBuildDir,
            "zinc${File.separator}generated${File.separator}${sourceSet.name}${File.separator}prod${File.separator}"
        )
    )

    data class ZincSourceSetDescriptor(
        val name: String,
        val generatedSourcesDirectory: File
    )
}
