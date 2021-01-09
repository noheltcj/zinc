plugins {
    application
    id("com.noheltcj.zinc")
}

zinc {
    productionSourceSetNames = setOf("main", "test")
}

application {
    @Suppress("UnstableApiUsage")
    mainClass.set("com.noheltcj.example.Main")
}
