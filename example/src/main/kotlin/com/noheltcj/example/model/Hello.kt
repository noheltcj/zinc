package com.noheltcj.example.model

import java.util.UUID

data class Hello(
    val id: String,
    val label: String = "weirdly complex nightmare of a default: ${
        UUID.randomUUID().toString() + "...idk"
    }",
    val world: World
)
