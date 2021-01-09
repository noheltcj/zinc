package com.noheltcj.zinc.compiler.plugin.registration

import com.google.auto.service.AutoService
import com.noheltcj.zinc.compiler.plugin.ServiceLocator
import com.noheltcj.zinc.compiler.plugin.analysis.ZincAnalysisHandlerExtension
import com.noheltcj.zinc.compiler.plugin.configuration.ResolvedConfiguration
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.impl.ExtensionPointImpl
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.ProjectExtensionDescriptor
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension

@AutoService(ComponentRegistrar::class)
class ZincComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val messageCollector = ServiceLocator.provideMessageCollector(configuration)

        messageCollector.report(CompilerMessageSeverity.INFO, "Registering Zinc components...")

        runCatching {
            val resolvedConfiguration = ResolvedConfiguration.fromConfiguration(configuration)

            if (resolvedConfiguration.isEnabled) {
                AnalysisHandlerExtension.registerExtensionAsFirst(
                    project = project,
                    extension = ZincAnalysisHandlerExtension(
                        generatedCodeDirectory = resolvedConfiguration.generatedSourcesDirectory
                    )
                )
            }
        }.onFailure {
            val message = it.message ?: "An unknown error occurred."
            messageCollector.report(CompilerMessageSeverity.ERROR, message, null)
        }
    }

    fun <T : Any> ProjectExtensionDescriptor<T>.registerExtensionAsFirst(
        project: Project,
        extension: T
    ) {
        project.extensionArea
            .getExtensionPoint(extensionPointName)
            .let { it as ExtensionPointImpl }
            .registerExtension(extension, project)
    }
}
