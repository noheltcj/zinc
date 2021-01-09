plugins {
    id("com.github.gmazzo.buildconfig") version "2.0.2"
}

buildConfig {
    project.properties
        .filter { entry -> entry.key.startsWith("zinc") }
        .forEach { (key, value) ->
            buildConfigField("String", key, "\"${requireNotNull(value) as String}\"")
        }
}
