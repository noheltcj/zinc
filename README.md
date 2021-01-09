# Zinc
An extensible compiler plugin for Kotlin to automatically expand data classes to include their own builder DSL.

## Introduction
Zinc was originally conceived as a solution to reduce testing boilerplate, but supposedly there are production use cases too.

More information to come as this becomes more useful.

## Setup
For now, examples.
```
buildscript {
  repositories {
    mavenCentral()
  }
}

plugins {
  id("com.noheltcj.zinc") version "0.0.2"
}

zinc {
  // All optional

  // defaults to true
  enabled = true

  // defaults to setOf("main")
  mainSourceSetNames = setOf("main", "anotherSourceSet")
}
```
