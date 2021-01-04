package com.noheltcj.zinc.core

/**
 * Primarily serves as an extension point
 */
interface ZincBuilder<T> {
    fun build(): T
}