package com.noheltcj.example.model

import java.util.UUID

data class World(val id: String = "generated ${UUID.randomUUID()}", val label: String)
