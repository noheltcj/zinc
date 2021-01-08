import com.noheltcj.zinc.shared.build.loadStringProperty

plugins {
    id("defaults")
}

buildscript {
    dependencies {
        classpath("com.noheltcj.zinc:gradle-plugin")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.11.1")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
    }
}

subprojects {
    apply(plugin = "defaults")

    pluginManager.withPlugin(Dependencies.targets.jvm) {
        tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
                allWarningsAsErrors = true
            }
        }
    }

    pluginManager.withPlugin(Dependencies.plugins.mavenPublish) {
        val mavenPublish = requireNotNull(extensions.findByType(com.vanniktech.maven.publish.MavenPublishPluginExtension::class))

        mavenPublish.nexus {
            groupId = loadStringProperty("zincGroupId")
        }

        val uploadArchivesTarget: com.vanniktech.maven.publish.MavenPublishTarget = requireNotNull(
            mavenPublish.targets.findByName("uploadArchives")
        )

        uploadArchivesTarget.releaseRepositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
        uploadArchivesTarget.snapshotRepositoryUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
    }

    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.noheltcj.zinc:core"))
                .with(project(":core"))
            substitute(module("com.noheltcj.zinc:compiler-plugin"))
                .with(project(":compiler-plugin"))
        }
    }
}
