

plugins {
    id("org.jetbrains.dokka") version "1.5.0" apply false

    id("defaults")
}

buildscript {
    dependencies {
        classpath("com.noheltcj.zinc:gradle-plugin")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.17.0")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
    }
}

val cleanGradlePlugin: Task by tasks.creating {
    dependsOn(gradle.includedBuild("gradle-plugin").task(":clean"))
}

val clean: Task by tasks.getting {
    dependsOn(cleanGradlePlugin)
}

val testGradlePlugin: Task by tasks.creating {
    dependsOn(gradle.includedBuild("gradle-plugin").task(":test"))
}

val test: Task by tasks.getting {
    // TODO: Re-enable once these tests are valuable
    // dependsOn(testGradlePlugin)
}

val ktlintCheckGradlePlugin: Task by tasks.creating {
    dependsOn(gradle.includedBuild("gradle-plugin").task(":ktlintCheck"))
}

val ktlintCheck: Task by tasks.getting {
    dependsOn(ktlintCheckGradlePlugin)
}

val ktlintFormatGradlePlugin: Task by tasks.creating {
    dependsOn(gradle.includedBuild("gradle-plugin").task(":ktlintFormat"))
}

val ktlintFormat: Task by tasks.getting {
    dependsOn(ktlintFormatGradlePlugin)
}

val publishGradlePluginLocally: Task by tasks.creating {
    dependsOn(gradle.includedBuild("gradle-plugin").task(":publishToMavenLocal"))
}

// val uploadGradlePluginArchives: Task by tasks.creating {
//    dependsOn(gradle.includedBuild("gradle-plugin").task(":uploadArchives"))
// }
//
// val uploadArchives: Task by tasks.getting {
//    dependsOn(uploadGradlePluginArchives)
// }

subprojects {
    pluginManager.withPlugin(Dependencies.targets.jvm) {
        tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
                allWarningsAsErrors = true
            }
        }
    }

//    pluginManager.withPlugin(Dependencies.plugins.mavenPublish) {
//        val mavenPublish = requireNotNull(extensions.findByType(com.vanniktech.maven.publish.MavenPublishPluginExtension::class))
//
//        mavenPublish.nexus {
//            groupId = loadStringProperty("releaseProfile")
//        }
//
//        val uploadArchivesTarget: com.vanniktech.maven.publish.MavenPublishTarget = requireNotNull(
//            mavenPublish.targets.findByName("uploadArchives")
//        )
//
//        uploadArchivesTarget.releaseRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
//        uploadArchivesTarget.snapshotRepositoryUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
//    }

    configurations.all {
        resolutionStrategy.dependencySubstitution {
//            substitute(module("com.noheltcj.zinc:compiler-plugin"))
//                .using(project(":compiler-plugin"))
        }
    }
}
