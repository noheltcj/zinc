package com.noheltcj.zinc.gradle.plugin

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.FilesSubpluginOption
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.mockito.BDDMockito.given
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

object ZincGradlePluginTests : Spek({
    val plugin by memoized { ZincGradlePlugin() }

    describe("applyToCompilation") {
        val mockCompilation by memoized { mock<KotlinCompilation<*>>() }
        val mockTarget by memoized { mock<KotlinTarget>() }
        val mockProject by memoized { mock<Project>() }

        beforeEachTest {
            given(mockCompilation.target).willReturn(mockTarget)
            given(mockTarget.project).willReturn(mockProject)
        }

        it("should produce correct sub-plugin options") {
            assertThat(plugin.applyToCompilation(mockCompilation).get())
                .isEqualTo(
                    listOf(
                        SubpluginOption(key = "convert_data_classes", value = "true"),
                        FilesSubpluginOption(key = "generated_sources_directory", files = setOf(File(""))),
                    )
                )
        }
    }
})