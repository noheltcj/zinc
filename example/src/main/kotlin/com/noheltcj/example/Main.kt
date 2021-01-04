package com.noheltcj.example

import com.noheltcj.example.data.WidgetThingBuilder.Companion.buildWidgetThing
import com.noheltcj.example.model.RatchetBuilder

object Main {
    @JvmStatic
    fun main(vararg args: String) {
        println(buildWidgetThing {
            id("Yep, functional")
            ratchets(RatchetBuilder().id("Woot").build())
        })
    }
}
