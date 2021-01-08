plugins {
    application
    id("com.noheltcj.zinc")
}

zinc {
    mainSourceSetNames = setOf("main", "test")
}

application {
    @Suppress("UnstableApiUsage")
    mainClass.set("com.noheltcj.example.Main")
}
