package com.noheltcj.zinc.compiler.plugin.builder

import com.noheltcj.zinc.compiler.plugin.configuration.Argument
import com.noheltcj.zinc.compiler.plugin.extension.ArgumentExtensions.toPluginOption
import com.tschuchort.compiletesting.PluginOption

data class ArgumentsBuilder(
    private var dataClassGenerationEnabled: Boolean? = null,
    private var generatedSourcesDirectory: String? = null
) {
    fun withDataClassGenerationEnabled(value: Boolean): ArgumentsBuilder {
        dataClassGenerationEnabled = value
        return this
    }

    fun withGeneratedSourcesDirectory(value: String): ArgumentsBuilder {
        generatedSourcesDirectory = value
        return this
    }

    fun build() = mutableListOf<PluginOption>().apply {
        dataClassGenerationEnabled
            ?.let { Argument.ConvertDataClassesEnabled.toPluginOption(it.toString()) }
            ?.run { add(this) }
        generatedSourcesDirectory
            ?.let { Argument.GeneratedSourcesDirectory.toPluginOption(it) }
            ?.run { add(this) }
    }
}
