package com.noheltcj.example

import com.noheltcj.example.model.HelloBuilder.Companion.buildHello
import com.noheltcj.example.model.WorldBuilder.Companion.buildWorld
import java.util.UUID

object Main {
    @JvmStatic
    fun main(vararg args: String) {
        val helloWorld = buildHello {
            id(randomId())
            label("Hello")
            world(
                buildWorld {
                    id(randomId())
                    label("World")
                }
            )
        }

        println(helloWorld)
    }

    private fun randomId() = UUID.randomUUID().toString()
}
