package com.noheltcj.zinc.compiler.plugin.configuration

import java.io.File
import org.jetbrains.kotlin.config.CompilerConfiguration

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
