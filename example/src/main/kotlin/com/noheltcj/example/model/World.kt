package com.noheltcj.example.model

import com.noheltcj.example.Main.randomStringFunction
import com.noheltcj.example.Main.randomStringProperty

data class World(
    val id: String = "generated $randomStringProperty",
    val label: String? = randomStringFunction()
)
