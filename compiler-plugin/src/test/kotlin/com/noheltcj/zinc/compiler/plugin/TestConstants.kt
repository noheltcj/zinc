package com.noheltcj.zinc.compiler.plugin

import com.noheltcj.zinc.core.Properties.compilerPluginId

object TestConstants {
    private const val testPluginIdPrefix = "com.tschuchort.compiletesting.maincommandlineprocessor"
    private const val testFullPluginId = "$testPluginIdPrefix:$compilerPluginId"

    const val requiredPluginOptionPrefix = "Required plugin option not present: $testFullPluginId"
}
