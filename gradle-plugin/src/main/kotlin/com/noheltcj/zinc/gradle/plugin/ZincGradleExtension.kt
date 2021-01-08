package com.noheltcj.zinc.gradle.plugin

open class ZincGradleExtension {
    private var _enabled: Boolean? = null
    private var _mainSources: Set<String>? = null

    var enabled: Boolean
    get() = _enabled ?: true
    set(value) { _enabled = value }

    var mainSourceSetNames: Set<String>
        get() = _mainSources ?: setOf("main")
        set(value) { _mainSources = value }
}
