package com.noheltcj.zinc.gradle.plugin

open class ZincGradleExtension {
    private var _enabled: Boolean? = null
    private var _productionSources: Set<String>? = null

    /**
     * Not intended for use by consumers. This is internal state only intended for the gradle plugin to use.
     */
    internal val configuredSourceSets: MutableSet<ZincGradlePlugin.ZincSourceSetDescriptor> = mutableSetOf()

    var enabled: Boolean
    get() = _enabled ?: true
    set(value) { _enabled = value }

    /**
     * Configure the source sets that will contribute
     */
    var productionSourceSetNames: Set<String>
        get() = _productionSources ?: setOf("main")
        set(value) { _productionSources = value }
}
