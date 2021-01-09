package com.noheltcj.zinc.compiler.plugin

import com.noheltcj.zinc.compiler.plugin.InputSources.allSources
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File

fun KotlinCompilation.sources(vararg sources: InputSources.Source) {
    this.sources = sources
        .map(InputSources.Source::toSourceFile)
}

fun KotlinCompilation.Result.pluginGeneratedClasses(vararg sources: InputSources.Source = allSources): List<File> {
    val inputSourceFiles = sources.map { "${it.packagePath}/${it.fileName}" }
    return compiledClassAndResourceFiles
        .filter { it.extension == "class" }
        .filter { compiledFile ->
            !inputSourceFiles.any {
                compiledFile.path.contains("$it.class")
            }
        }
}

fun KotlinCompilation.Result.pluginGeneratedClassNames(vararg sources: InputSources.Source = allSources) =
    pluginGeneratedClasses(*sources)
        .map { it.name }

fun KotlinCompilation.Result.pluginGeneratedClassPaths(vararg sources: InputSources.Source = allSources) =
    pluginGeneratedClasses(*sources)
        .map { it.path }
