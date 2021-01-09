package com.noheltcj.zinc.compiler.plugin.compilation.generator

import java.io.File
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

interface CodeGenerator {
    fun shouldGenerate(
        bindingContext: BindingContext,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>
    ): Evaluation

    fun generate(
        generatedFilesDirectory: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>
    ): List<VirtualFileData>

    data class VirtualFileData(
        val file: File,
        val source: String
    )

    enum class Evaluation {
        SHOULD_GENERATE,
        SHOULD_NOT_GENERATE
    }
}
