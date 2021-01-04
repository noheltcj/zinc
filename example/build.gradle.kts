plugins {
    application
    id("com.noheltcj.zinc.gradle-plugin")
}

zinc {
    mainSourceSetNames = setOf("main", "test")
}

application {
    @Suppress("UnstableApiUsage")
    mainClass.set("com.noheltcj.example.Main")
}
