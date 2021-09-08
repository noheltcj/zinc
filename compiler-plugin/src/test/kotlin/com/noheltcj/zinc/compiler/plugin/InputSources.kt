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
            dataClassWithDefaultedValuesUsingExternalPackages,
            dataClassWithDefaultedValuesUsingNothingReceiverInExternalPackages,
            dataClassWithId,
            dataClassWithMultipleSameTypeFields,
            javaWidget,
            primaryExternalPackageDefaults
        )
    }

    private const val randomUUIDExpression = "\${UUID.randomUUID().toString()}"

    val primaryExternalPackageDefaults = Source(
        fileName = "PrimaryExternalPackageDefaults",
        packageName = "com.noheltcj.external.primary",
        contents = """
            |import java.util.UUID
            |import kotlin.properties.Delegates 
            |
            |val primaryNothingSiteRandomStringProperty: String get() = UUID.randomUUID().toString()
            |fun primaryNothingSiteRandomStringFunction() = UUID.randomUUID().toString()
            |
            |object PrimaryExternalPackageDefaults {
            |    fun externalFunction() = primaryNothingSiteRandomStringProperty
            |    val externalProperty = primaryNothingSiteRandomStringProperty
            |    val externalPropertyGetter get() = primaryNothingSiteRandomStringProperty
            |    var externalDelegatedProperty: String by Delegates.notNull()
            |    
            |    init {
            |        externalDelegatedProperty = primaryNothingSiteRandomStringProperty 
            |    }
            |}
        """.trimMargin()
    )

    val dataClassInRoot = Source(
        fileName = "InRoot",
        packageName = "",
        contents = """
            |data class InRoot(val id: String)
        """.trimMargin()
    )

    val dataClassWithDefaultedValuesUsingExternalPackages = Source(
        fileName = "DefaultedValuesExternal",
        contents = """
            |import com.noheltcj.external.primary.*
            |
            |data class DefaultedValuesExternal(
            |    val defaultedByFunction: String = PrimaryExternalPackageDefaults.externalFunction(),
            |    val defaultedByProperty: String = PrimaryExternalPackageDefaults.externalProperty,
            |    val defaultedByPropertyGetter: String = PrimaryExternalPackageDefaults.externalPropertyGetter,
            |    val defaultedByPropertyDelegate: String = PrimaryExternalPackageDefaults.externalDelegatedProperty,
            |)
        """.trimMargin()
    )

    val dataClassWithDefaultedValuesUsingNothingReceiverInExternalPackages = Source(
        fileName = "DefaultedValuesExternal",
        contents = """
            |import com.noheltcj.external.primary.*
            |
            |data class DefaultedValuesExternal(
            |    val defaultedByNothingSiteProperty: String = primaryNothingSiteRandomStringProperty,
            |    val defaultedByNothingSiteFunction: String = primaryNothingSiteRandomStringFunction()
            |)
        """.trimMargin()
    )

    val dataClassWithDefaultedValues = Source(
        fileName = "DefaultedValues",
        packageName = "com.noheltcj.external",
        contents = """
            |import java.util.UUID
            |
            |data class DefaultedValues(
            |    val defaultedLong: Long = 1L,
            |    val defaultedString: String = "defaultString",
            |    val defaultedRandomString: String = "$randomUUIDExpression interpolated",
            |    val defaultedAny: Any = true
            |)
        """.trimMargin()
    )

    val dataClassWithId = Source(
        fileName = "Widget",
        contents = """
            |data class Widget(val id: String)
        """.trimMargin()
    )

    val dataClassWithMultipleSameTypeFields = Source(
        fileName = "MultipleSameTypeFields",
        contents = """
            |data class MultipleSameTypeFields(val id: String, val name: String)
        """.trimMargin()
    )

    val dataClassWithBuildable = Source(
        fileName = "Composite",
        packageName = "com.noheltcj.zinc.entity.composite",
        contents = """
            |import com.noheltcj.zinc.entity.*
            |
            |data class Composite(val widget: Widget)
        """.trimMargin()
    )

    val javaWidget = Source(
        fileName = "JavaWidget",
        contents = """
            |public class JavaWidget {
            |    public void something() {
            |        // Here for posterity
            |    }
            |}
        """.trimMargin(),
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
                |${
                    if (packageName.isNotEmpty()) {
                        "package $packageName${
                            if (type == SourceType.Java) ";"
                            else ""
                        }"
                    } else {
                        ""
                    }
                }
                |
                |$contents
            """.trimMargin()

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
