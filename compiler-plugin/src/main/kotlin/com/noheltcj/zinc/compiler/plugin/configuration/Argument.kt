package com.noheltcj.zinc.compiler.plugin.configuration

import java.io.File
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

sealed class Argument<T : Any>(
    override val optionName: String,
    override val valueDescription: String,
    override val description: String,
    override val required: Boolean,
    override val allowMultipleOccurrences: Boolean
) : AbstractCliOption {

    abstract val configurationKey: CompilerConfigurationKey<T>
    abstract val default: T

    fun getValue(configuration: CompilerConfiguration): T =
        configuration.get(configurationKey) ?: default

    fun putValue(configuration: CompilerConfiguration, value: T) {
        configuration.put(configurationKey, value)
    }

    object ConvertDataClassesEnabled : Argument<Boolean>(
        optionName = Option.CONVERT_DATA_CLASSES.key,
        valueDescription = "<true|false>",
        description = "Generate builders for all data classes. Default value is true.",
        required = false,
        allowMultipleOccurrences = false
    ) {
        override val default: Boolean = true
        override val configurationKey: CompilerConfigurationKey<Boolean> =
            CompilerConfigurationKey.create(Option.CONVERT_DATA_CLASSES.key)
    }

    object GeneratedSourcesDirectory : Argument<File>(
        optionName = Option.GENERATED_SOURCES_DIR.key,
        valueDescription = "A valid path to place generated files in",
        description = "Directory to place generated sources in.",
        required = true,
        allowMultipleOccurrences = false
    ) {
        override val default: File get() {
            throw IllegalStateException("GeneratedSourcesDirectory must be specified.")
        }
        override val configurationKey: CompilerConfigurationKey<File> =
            CompilerConfigurationKey.create(Option.GENERATED_SOURCES_DIR.key)
    }

    companion object {
        fun fromOptionKey(key: String) = when (key) {
            Option.CONVERT_DATA_CLASSES.key -> ConvertDataClassesEnabled
            Option.GENERATED_SOURCES_DIR.key -> GeneratedSourcesDirectory
            else -> throw IllegalArgumentException()
        }

        fun fromOption(option: AbstractCliOption): Argument<*> = fromOptionKey(option.optionName)
    }
}
