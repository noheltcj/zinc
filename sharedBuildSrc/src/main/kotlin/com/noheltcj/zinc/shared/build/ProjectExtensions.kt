package com.noheltcj.zinc.shared.build

import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import java.io.File
import java.util.*

/**
 * Configures the plugins for the project.
 */
fun Project.plugins(configure: PluginContainer.() -> Unit) =
    with(plugins, configure)

fun Project.loadProjectProperties() {
    if (properties["zincPropertiesLoaded"] == null) {
        val props = Properties()

        props.load(
            if (rootProject.name == "gradle-plugin") {
                file("${rootDir.absolutePath.substringBeforeLast("/")}${File.separator}project.properties")
                    .inputStream()
            } else {
                file("${rootDir.absolutePath}${File.separator}project.properties")
                    .inputStream()
            }
        )

        props.setProperty("zincPropertiesLoaded", "true")

        props.forEach { entry ->
            extensions.extraProperties.set(entry.key as String, entry.value)
        }
    }
}

fun Project.loadStringProperty(key: String): String {
    return requireNotNull(properties[key]) as String
}
