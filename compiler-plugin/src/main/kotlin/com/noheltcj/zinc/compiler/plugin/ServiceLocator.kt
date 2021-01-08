package com.noheltcj.zinc.compiler.plugin

import com.noheltcj.zinc.compiler.plugin.compilation.generator.DataClassBuilderGenerator
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration

internal object ServiceLocator {
    private val dataClassBuilderGenerator = DataClassBuilderGenerator()

    val generators = setOf(
        dataClassBuilderGenerator
    )

    fun provideMessageCollector(configuration: CompilerConfiguration) = configuration.get(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        MessageCollector.NONE
    )
}
