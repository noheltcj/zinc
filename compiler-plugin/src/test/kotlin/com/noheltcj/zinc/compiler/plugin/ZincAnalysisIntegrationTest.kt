package com.noheltcj.zinc.compiler.plugin

import com.google.common.truth.Truth.assertThat
import com.noheltcj.zinc.compiler.plugin.InputSources.createCompilation
import com.noheltcj.zinc.compiler.plugin.InputSources.dataClassInRoot
import com.noheltcj.zinc.compiler.plugin.InputSources.dataClassWithBuildable
import com.noheltcj.zinc.compiler.plugin.InputSources.dataClassWithDefaultedValues
import com.noheltcj.zinc.compiler.plugin.InputSources.dataClassWithId
import com.noheltcj.zinc.compiler.plugin.InputSources.dataClassWithMultipleSameTypeFields
import com.noheltcj.zinc.compiler.plugin.InputSources.javaWidget
import com.noheltcj.zinc.compiler.plugin.builder.ArgumentsBuilder
import com.noheltcj.zinc.compiler.plugin.configuration.Option
import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.kotlin.konan.file.File
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.CachingMode
import org.spekframework.spek2.style.specification.describe

object ZincAnalysisIntegrationTest : Spek({
    val arguments by memoized(CachingMode.TEST) { ArgumentsBuilder() }
    val compilation by memoized(CachingMode.TEST) { createCompilation() }

    val sourcesDir by memoized(CachingMode.TEST) { "${compilation.workingDir.path}${File.separator}sources" }

    describe("configuration") {
        beforeEachTest {
            compilation.sources()
        }

        describe("when generated sources directory is not configured") {
            beforeEachTest {
                compilation.pluginOptions = arguments
                    .withDataClassGenerationEnabled(true)
                    .build()

                compilation.sources(dataClassWithId)
            }

            it("should exit with a compilation error") {
                assertThat(compilation.compile().exitCode)
                    .isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
            }

            it("should have a useful message") {
                assertThat(compilation.compile().messages)
                    .contains("${TestConstants.requiredPluginOptionPrefix}:${Option.GENERATED_SOURCES_DIR.key}")
            }
        }

        describe("given generated sources directory is configured") {
            beforeEachTest {
                compilation.pluginOptions = arguments
                    .withGeneratedSourcesDirectory(sourcesDir)
                    .build()
            }

            it("should exit successfully") {
                assertThat(compilation.compile().exitCode)
                    .isEqualTo(KotlinCompilation.ExitCode.OK)
            }

            it("should generate nothing") {
                assertThat(compilation.compile().pluginGeneratedClassPaths())
                    .isEmpty()
            }

            describe("given a valid source file") {
                beforeEachTest {
                    compilation.sources(dataClassWithId)
                }

                it("should generate in the specified directory") {
                    compilation.compile()
                    assertThat(File("$sourcesDir/com/noheltcj/zinc/entity/WidgetBuilder.kt").exists)
                        .isTrue()
                }
            }
        }
    }

    describe("data class builder generation") {
        beforeEachTest {
            compilation.pluginOptions = arguments
                .withDataClassGenerationEnabled(true)
                .withGeneratedSourcesDirectory(sourcesDir)
                .build()
        }

        describe("given a single java class") {
            beforeEachTest {
                compilation.sources(javaWidget)
            }

            it("should exit successfully") {
                assertThat(compilation.compile().exitCode)
                    .isEqualTo(KotlinCompilation.ExitCode.OK)
            }

            it("should generate nothing") {
                assertThat(compilation.compile().pluginGeneratedClassPaths())
                    .isEmpty()
            }
        }

        describe("given a single data class") {
            beforeEachTest {
                compilation.sources(dataClassWithId)
            }

            it("should exit successfully") {
                assertThat(compilation.compile().exitCode)
                    .isEqualTo(KotlinCompilation.ExitCode.OK)
            }

            it("should generate a single builder and companion") {
                assertThat(compilation.compile().pluginGeneratedClassNames())
                    .containsExactly("WidgetBuilder.class", "WidgetBuilder\$Companion.class")
            }
        }

        describe("given a single data class in the root package") {
            beforeEachTest {
                compilation.sources(dataClassInRoot)
            }

            it("should exit successfully") {
                assertThat(compilation.compile().exitCode)
                    .isEqualTo(KotlinCompilation.ExitCode.OK)
            }

            it("should generate a single builder and companion") {
                assertThat(compilation.compile().pluginGeneratedClassNames())
                    .containsExactly("InRootBuilder.class", "InRootBuilder\$Companion.class")
            }
        }

        describe("given a single data class in the root package") {
            beforeEachTest {
                compilation.sources(dataClassInRoot)
            }

            it("should exit successfully") {
                assertThat(compilation.compile().exitCode)
                    .isEqualTo(KotlinCompilation.ExitCode.OK)
            }

            it("should generate a single builder and companion") {
                assertThat(compilation.compile().pluginGeneratedClassNames())
                    .containsExactly("InRootBuilder.class", "InRootBuilder\$Companion.class")
            }
        }

        describe("given a single data class with multiple fields of the same type") {
            beforeEachTest {
                compilation.sources(dataClassWithMultipleSameTypeFields)
            }

            it("should exit successfully") {
                assertThat(compilation.compile().exitCode)
                    .isEqualTo(KotlinCompilation.ExitCode.OK)
            }

            it("should generate a single builder and companion") {
                assertThat(compilation.compile().pluginGeneratedClassNames())
                    .containsExactly(
                        "MultipleSameTypeFieldsBuilder.class",
                        "MultipleSameTypeFieldsBuilder\$Companion.class"
                    )
            }
        }

        describe("given a data class that references another buildable class") {
            beforeEachTest {
                compilation.sources(dataClassWithId, dataClassWithBuildable)
            }

            it("should exit successfully") {
                assertThat(compilation.compile().exitCode)
                    .isEqualTo(KotlinCompilation.ExitCode.OK)
            }

            it("should generate two builders and companions") {
                assertThat(compilation.compile().pluginGeneratedClassNames())
                    .containsExactly(
                        "WidgetBuilder.class",
                        "WidgetBuilder\$Companion.class",
                        "CompositeBuilder.class",
                        "CompositeBuilder\$Companion.class"
                    )
            }
        }

        describe("given a data class with a defaulted value") {
            beforeEachTest {
                compilation.sources(dataClassWithDefaultedValues)
            }

            it("should exit successfully") {
                assertThat(compilation.compile().exitCode)
                    .isEqualTo(KotlinCompilation.ExitCode.OK)
            }

            it("should generate a single builder and companion") {
                assertThat(compilation.compile().pluginGeneratedClassNames())
                    .containsExactly(
                        "DefaultedValuesBuilder.class",
                        "DefaultedValuesBuilder\$Companion.class"
                    )
            }
        }
    }
})
