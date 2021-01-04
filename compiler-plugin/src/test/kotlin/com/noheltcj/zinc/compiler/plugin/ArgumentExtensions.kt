package com.noheltcj.zinc.compiler.plugin

import com.noheltcj.zinc.compiler.plugin.configuration.Argument
import com.tschuchort.compiletesting.PluginOption

object ArgumentExtensions {
    fun Argument<*>.toPluginOption(value: String) = PluginOption(
        pluginId = Constants.compilerPluginId,
        optionName = optionName,
        optionValue = value
    )
}