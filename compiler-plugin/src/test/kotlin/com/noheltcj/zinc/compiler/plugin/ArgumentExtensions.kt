package com.noheltcj.zinc.compiler.plugin

import com.noheltcj.zinc.compiler.plugin.configuration.Argument
import com.noheltcj.zinc.core.Properties.compilerPluginId
import com.tschuchort.compiletesting.PluginOption

object ArgumentExtensions {
    fun Argument<*>.toPluginOption(value: String) = PluginOption(
        pluginId = compilerPluginId,
        optionName = optionName,
        optionValue = value
    )
}
