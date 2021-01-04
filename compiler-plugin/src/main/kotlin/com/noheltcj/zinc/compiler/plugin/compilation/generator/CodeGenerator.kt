package com.noheltcj.zinc.compiler.plugin.compilation.generator

import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import java.io.File

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

//class TestBuilder {
//    private var _id: String by BuilderProperty(
//        defaultValue = "",
//        propertyDescription = idDescription
//    )
//
//    fun id(value: String): TestBuilder {
//        this._id = value;
//        return this
//    }
//
//    fun build(): String = this._id
//
//    companion object {
//        private const val idDescription = "id"
//
//        inline fun buildTest(crossinline block: TestBuilder.() -> Unit): String =
//            TestBuilder().apply(block).build()
//
//        fun test() {
//            buildTest {
//                id("hi")
//            }
//
//            TestBuilder().id("hi").build()
//        }
//    }
//}