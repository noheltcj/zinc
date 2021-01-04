package com.noheltcj.zinc.core.exception

sealed class ZincBuilderException(message: String) : IllegalStateException(message) {
    class PropertyNotSetException(propertyDescription: String) : ZincBuilderException(
        "$propertyDescription was never set before calling \"build\"."
    )
}