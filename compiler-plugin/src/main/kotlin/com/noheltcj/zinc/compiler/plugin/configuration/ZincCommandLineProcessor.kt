package com.noheltcj.zinc.compiler.plugin.configuration

import com.google.auto.service.AutoService
import com.noheltcj.zinc.compiler.plugin.ServiceLocator
import com.noheltcj.zinc.core.Properties
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.parsing.parseBoolean
import java.io.File

@AutoService(CommandLineProcessor::class)
class ZincCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = Properties.compilerPluginId

    override val pluginOptions: Collection<AbstractCliOption> = Option.values()
        .map { option -> Argument.fromOptionKey(option.key) }

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        val collector = ServiceLocator.provideMessageCollector(configuration)
        runCatching {
            when (val argument = Argument.fromOption(option)) {
                is Argument.ConvertDataClassesEnabled -> argument.putValue(configuration, parseBoolean(value))
                is Argument.GeneratedSourcesDirectory -> argument.putValue(configuration, File(value))
            }
        }.onFailure {
            val message = it.message ?: "Zinc: An unknown error occurred."
            collector.report(CompilerMessageSeverity.ERROR, message, null)
        }
    }
}