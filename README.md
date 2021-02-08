# Zinc
![tests](https://github.com/noheltcj/zinc/workflows/Test/badge.svg)
![lint](https://github.com/noheltcj/zinc/workflows/Lint/badge.svg)
[![maven central](https://maven-badges.herokuapp.com/maven-central/com.noheltcj.zinc/gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.noheltcj.zinc/gradle-plugin)

An extensible compiler plugin for Kotlin to automatically expand data classes to include their own builder DSL.

## Introduction
Zinc was originally conceived as a solution to reduce testing boilerplate, but supposedly there are production use cases
too.

More information to come as this becomes more useful.

## How it works
When enabled, Zinc analyzes specified source sets and generates builders and a DSL for each data class based on the
constructor params. Fields not in the primary constructor must be set after building. There are two classes of builders 
generated: _production_ and _test_.

[See Examples](docs/hello_world_example.md)

### Production Builders
These builders are for use in production code when a builder pattern is preferable over using a constructor. All 
properties must be able to resolve a value. When a field does not have a default value, an exception will be thrown with
details about the missing parameter.

Configure the source sets that should contribute to this classification by setting the `productionSourceSetNames` field
on the plugin extension. [See Setup](#setup)

### Test Builders
_Not yet implemented._

This is the work-in-progress primary feature of this plugin. Builders in this classification will be generated with 
randomized default values for all fields. This essentially provides a clean, all fields optional way to supply fake 
data in tests.

## Setup
Artifacts can be downloaded from Maven Central.

### Gradle Kotlin Script
```kotlin
plugins {
  id("com.noheltcj.zinc") version "0.0.3"
}

zinc {
  // defaults to true
  enabled = true

  // defaults to setOf("main")
  productionSourceSetNames = setOf("main", "anotherSourceSet")
}
```

### Gradle Groovy
```groovy
plugins {
    id 'com.noheltcj.zinc' version '0.0.3'
}

zinc {
    productionSourceSetNames = ['main', 'anotherSourceSet']
}
```
