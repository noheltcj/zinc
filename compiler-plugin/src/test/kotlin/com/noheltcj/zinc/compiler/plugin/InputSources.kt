package com.noheltcj.zinc.compiler.plugin

import com.noheltcj.zinc.compiler.plugin.configuration.ZincCommandLineProcessor
import com.noheltcj.zinc.compiler.plugin.registration.ZincComponentRegistrar
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.java
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import org.jetbrains.kotlin.config.JvmTarget

object InputSources {
    val allSources: Array<Source> by lazy {
        arrayOf(
            dataClassInRoot,
            dataClassWithBuildable,
            dataClassWithDefaultedValues,
            dataClassWithId,
            dataClassWithMultipleSameTypeFields,
            javaWidget
        )
    }

    val dataClassInRoot = Source(
        fileName = "InRoot",
        packageName = "",
        contents = """
            data class InRoot(val id: String)
        """.trimIndent()
    )

    val randomUUIDExpression = "\${UUID.randomUUID().toString()}"
    val dataClassWithDefaultedValues = Source(
        fileName = "DefaultedValues",
        contents = """
            import java.util.UUID
            
            data class DefaultedValues(
                val defaultedLong: Long = 1L,
                val defaultedString: String = "defaultString",
                val defaultedRandomString: String = "$randomUUIDExpression interpolated",
                val defaultedAny: Any = true
            )
        """.trimIndent()
    )

    val dataClassWithId = Source(
        fileName = "Widget",
        contents = """
            data class Widget(val id: String)
        """.trimIndent()
    )

    val dataClassWithMultipleSameTypeFields = Source(
        fileName = "MultipleSameTypeFields",
        contents = """
            data class MultipleSameTypeFields(val id: String, val name: String)
        """.trimIndent()
    )

    val dataClassWithBuildable = Source(
        fileName = "Composite",
        packageName = "com.noheltcj.zinc.entity.composite",
        contents = """
            import com.noheltcj.zinc.entity.*
            
            data class Composite(val widget: Widget)
        """.trimIndent()
    )

    val javaWidget = Source(
        fileName = "JavaWidget",
        contents = """
            public class JavaWidget {
                public void something() {
                    // Here for posterity
                }
            }
        """.trimIndent(),
        type = Source.SourceType.Java
    )

    fun createCompilation() = KotlinCompilation().apply {
        compilerPlugins = listOf(ZincComponentRegistrar())
        commandLineProcessors = listOf(ZincCommandLineProcessor())

        inheritClassPath = true
        jvmTarget = JvmTarget.JVM_1_8.description
    }

    data class Source(
        val fileName: String,
        val contents: String,
        val packageName: String = "com.noheltcj.zinc.entity",
        val type: SourceType = SourceType.Kotlin
    ) {
        val packagePath = packageName.split(".").joinToString(separator = "/")

        fun toSourceFile(): SourceFile {
            val name = "$fileName.${
                when (type) {
                    SourceType.Java -> "java"
                    SourceType.Kotlin -> "kt"
                }
            }"
            val contents = """
                ${
                    if (packageName.isNotEmpty()) {
                        "package $packageName${
                            if (type == SourceType.Java) ";"
                            else ""
                        }"
                    } else {
                        ""
                    }
                }
                
                $contents
            """.trimIndent()

            return when (type) {
                SourceType.Java -> java(
                    name = name,
                    contents = contents
                )
                SourceType.Kotlin -> kotlin(
                    name = name,
                    contents = contents,
                    trimIndent = true
                )
            }
        }

        enum class SourceType {
            Java, Kotlin
        }
    }
}
