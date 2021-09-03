plugins {
    id("defaults")

    kotlin("kapt")
}

dependencies {
    api(project(":core"))

    compileOnly(Dependencies.project.google.auto.annotations)
    compileOnly(Dependencies.project.kotlin.compilerEmbedded)

    kapt(Dependencies.project.google.auto.processor)

    testImplementation(Dependencies.project.kotlin.annotationProcessingEmbedded)
    testImplementation(Dependencies.project.kotlin.compilerEmbedded)

    testImplementation(Dependencies.test.compileTesting)
}
