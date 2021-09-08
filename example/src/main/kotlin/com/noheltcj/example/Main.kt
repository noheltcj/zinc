package com.noheltcj.example

import com.noheltcj.example.model.HelloBuilder.Companion.buildHello
import com.noheltcj.example.model.WorldBuilder.Companion.buildWorld
import java.util.UUID

object Main {
    @JvmStatic
    fun main(vararg args: String) {
        val helloWorld = buildHello {
            id(randomStringProperty)
            label("Hello")
            world(
                buildWorld {
                    label("World")
                }
            )
        }

        println(helloWorld)
    }

    val randomStringProperty get() = UUID.randomUUID().toString()
    fun randomStringFunction() = UUID.randomUUID().toString()
}
