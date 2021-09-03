import com.noheltcj.zinc.shared.build.loadStringProperty

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.17.0")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
    }
}

plugins {
    id("defaults")
    id("com.gradle.plugin-publish") version "0.12.0"

    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        register("zinc-gradle-plugin") {
            id = loadStringProperty("zincGradlePluginId")
            implementationClass = "com.noheltcj.zinc.gradle.plugin.ZincGradlePlugin"
        }
    }
}

pluginBundle {
    tags = listOf("kotlin", "kotlin-compiler-plugin")
}

mavenPublish {
//    nexus {
//        groupId = loadStringProperty("releaseProfile")
//    }
//
//    targets {
//        val uploadArchivesTarget: com.vanniktech.maven.publish.MavenPublishTarget = requireNotNull(
//            findByName("uploadArchives")
//        )
//
//        uploadArchivesTarget.releaseRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
//    }
}

dependencies {
    implementation(Dependencies.plugins.kotlinGradle)
    implementation(Dependencies.project.kotlin.gradlePluginApi)
    implementation(Dependencies.project.kotlin.stdlib)

    testImplementation(Dependencies.test.compileTesting)
    testImplementation(Dependencies.test.mockito)
}

pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
            // Until gradle no longer includes an old version of kotlin
            allWarningsAsErrors = false
        }
    }
}
