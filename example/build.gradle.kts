plugins {
    id("defaults")
    id("com.noheltcj.zinc")

    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

zinc {
    productionSourceSetNames = setOf("main")
}

application {
    @Suppress("UnstableApiUsage")
    mainClass.set("com.noheltcj.example.Main")
}
