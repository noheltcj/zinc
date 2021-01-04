package com.noheltcj.zinc.gradle.plugin

import com.noheltcj.zinc.gradle_plugin.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.jetbrains.kotlin.gradle.plugin.*
import java.io.File

class ZincGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val compilationName = kotlinCompilation.name
        if (compilationName == MAIN_COMPILATION_NAME || compilationName == TEST_COMPILATION_NAME) {
            return false
        }

        if (!kotlinCompilation.target.project.plugins.hasPlugin(ZincGradlePlugin::class.java)) {
            return false
        }

        val project = kotlinCompilation.target.project
        val extension = project.extensions.findByType(ZincGradleExtension::class.java) ?: ZincGradleExtension()

        return extension.mainSourceSetNames.contains(compilationName)
    }

    override fun getCompilerPluginId(): String = BuildConfig.zincCompilerPluginId

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = BuildConfig.zincGroupId,
        artifactId = "compiler-plugin",
        version = BuildConfig.zincVersion
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.findByType(ZincGradleExtension::class.java) ?: ZincGradleExtension()

        val sourceSets = project.extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
        val (generatedSourcesDirectory, isMain) =
            if (extension.mainSourceSetNames.contains(kotlinCompilation.name)) {
                File(project.buildDir, zincMainGenDir) to true
            } else {
                throw IllegalStateException("Test generation not implemented yet.")
            }

        val (buildGenPath, configuredSourceSetNames) = if (isMain) {
            zincMainGenDir to extension.mainSourceSetNames
        } else {
            throw IllegalStateException("Test generation not implemented yet.")
        }

        val sourceSetMap = sourceSets.asMap
        configuredSourceSetNames.forEach { configuredSourceSetName ->
            requireNotNull(sourceSetMap[configuredSourceSetName]).java
                .srcDir("build${File.separator}$buildGenPath")
        }

        project.pluginManager.withPlugin("idea") {
            val idea = project.plugins.getPlugin(it.id) as IdeaPlugin
            val module = idea.model.module
            module.generatedSourceDirs.plusAssign(generatedSourcesDirectory)
        }

        return project.provider {
            listOf(
                SubpluginOption(key = "convert_data_classes", value = extension.enabled.toString()),
                FilesSubpluginOption(key = "generated_sources_directory", files = setOf(generatedSourcesDirectory)),
            )
        }
    }

    override fun apply(target: Project): Unit = with(target) {
        extensions.create("zinc", ZincGradleExtension::class.java)

        val mainGenBuildDir = File(project.buildDir, zincMainGenDir)
        val testGenBuildDir = File(project.buildDir, zincTestGenDir)

        val sourceSets = project.extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
        sourceSets.create(MAIN_COMPILATION_NAME) { sourceSet ->
            sourceSet.java.srcDirs += mainGenBuildDir
        }
        sourceSets.create(TEST_COMPILATION_NAME) { sourceSet ->
            sourceSet.java.srcDirs += testGenBuildDir
        }

        dependencies.add(
            "implementation",
            "${BuildConfig.zincGroupId}:core:${BuildConfig.zincVersion}"
        )
    }

    companion object {
        private const val MAIN_COMPILATION_NAME = "zinc-main"
        private const val TEST_COMPILATION_NAME = "zinc-test"

        private val zincMainGenDir = "zinc${File.separator}generated${File.separator}source${File.separator}main${File.separator}"
        private val zincTestGenDir = "zinc${File.separator}generated${File.separator}source${File.separator}test${File.separator}"
    }
}