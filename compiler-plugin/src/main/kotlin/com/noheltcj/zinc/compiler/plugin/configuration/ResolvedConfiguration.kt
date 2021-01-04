package com.noheltcj.zinc.compiler.plugin.configuration

import org.jetbrains.kotlin.config.CompilerConfiguration
import java.io.File

data class ResolvedConfiguration(
    val isEnabled: Boolean,
    val generatedSourcesDirectory: File
) {
    companion object {
        fun fromConfiguration(configuration: CompilerConfiguration) = ResolvedConfiguration(
            isEnabled = Argument.ConvertDataClassesEnabled.getValue(configuration),
            generatedSourcesDirectory = Argument.GeneratedSourcesDirectory.getValue(configuration)
        )
    }
}