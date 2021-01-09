import com.noheltcj.zinc.shared.build.loadStringProperty

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.11.1")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
    }
}

plugins {
    id("com.noheltcj.zinc.defaults")
    id("com.gradle.plugin-publish") version "0.12.0"
    id("com.github.gmazzo.buildconfig") version "2.0.2"

    `java-gradle-plugin`
}

buildConfig {
    project.properties
        .filter { entry -> entry.key.startsWith("zinc") }
        .forEach { (key, value) ->
            buildConfigField("String", key, "\"${requireNotNull(value) as String}\"")
        }
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
    nexus {
        groupId = loadStringProperty("releaseProfile")
    }

    targets {
        val uploadArchivesTarget: com.vanniktech.maven.publish.MavenPublishTarget = requireNotNull(
            findByName("uploadArchives")
        )

        uploadArchivesTarget.releaseRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    }
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
