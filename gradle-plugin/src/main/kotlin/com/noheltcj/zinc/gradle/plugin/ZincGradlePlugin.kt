package com.noheltcj.zinc.gradle.plugin

import java.io.File
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.FilesSubpluginOption
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
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
                    files = extension.configuredSourceSets.map { it.generatedSourcesDirectory }
                )
            )
        }
    }

    override fun apply(target: Project): Unit = with(target) {
        val zincExtension = extensions.create("zinc", ZincGradleExtension::class.java)

        plugins.withType(KotlinBasePluginWrapper::class.java) {
            if (zincExtension.enabled) {
                val kotlinExtension = extensions.getByType(KotlinProjectExtension::class.java)
                kotlinExtension.sourceSets.forEach { sourceSet ->
                    val sourceDescriptor = createSourceSetDescriptor(buildDir, sourceSet.name)
                    if (zincExtension.productionSourceSetNames.contains(sourceSet.name)) {
                        if (zincExtension.configuredSourceSets.add(sourceDescriptor)) {
                            sourceSet.kotlin.srcDir(sourceDescriptor.generatedSourcesDirectory)
                        }
                    }
                }
            }
        }
    }

    private fun createSourceSetDescriptor(projectBuildDir: File, sourceSetName: String) = ZincSourceSetDescriptor(
        name = zincProdSourceSetName(sourceSetName),
        generatedSourcesDirectory = File(
            projectBuildDir,
            "zinc${File.separator}generated${File.separator}${sourceSetName}${File.separator}prod${File.separator}"
        )
    )

    private fun zincProdSourceSetName(shadowedSourceSetName: String) = "zinc${shadowedSourceSetName.capitalize()}Prod"

    data class ZincSourceSetDescriptor(
        val name: String,
        val generatedSourcesDirectory: File
    )
}
