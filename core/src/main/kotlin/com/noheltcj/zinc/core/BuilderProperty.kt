package com.noheltcj.zinc.core

import com.noheltcj.zinc.core.exception.ZincBuilderException
import kotlin.reflect.KProperty

class BuilderProperty<T>(
    defaultValue: T? = null,
    private val propertyDescription: String
) {
    private var value: T? = defaultValue

    operator fun getValue(thisRef: Any, property: KProperty<*>): T = value
        ?: throw ZincBuilderException.PropertyNotSetException(propertyDescription)

    operator fun setValue(thisRef: Any, property: KProperty<*>, newValue: T) {
        value = newValue
    }
}
