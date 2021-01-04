package com.noheltcj.zinc.compiler.plugin

object TestConstants {
    private const val testPluginIdPrefix = "com.tschuchort.compiletesting.maincommandlineprocessor"
    private const val testFullPluginId = "$testPluginIdPrefix:${Constants.compilerPluginId}"

    const val requiredPluginOptionPrefix = "Required plugin option not present: $testFullPluginId"
}