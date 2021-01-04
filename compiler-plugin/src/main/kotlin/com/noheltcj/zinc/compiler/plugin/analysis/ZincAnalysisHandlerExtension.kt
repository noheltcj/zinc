package com.noheltcj.zinc.compiler.plugin.analysis

import com.noheltcj.zinc.compiler.plugin.ServiceLocator
import com.noheltcj.zinc.compiler.plugin.compilation.generator.CodeGenerator
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import java.io.File

internal class ZincAnalysisHandlerExtension(
    private val generators: Set<CodeGenerator> = ServiceLocator.generators,
    private val generatedCodeDirectory: File
) : AnalysisHandlerExtension {

    var didGenerate: Boolean = false

    override fun doAnalysis(
        project: Project,
        module: ModuleDescriptor,
        projectContext: ProjectContext,
        files: Collection<KtFile>,
        bindingTrace: BindingTrace,
        componentProvider: ComponentProvider
    ): AnalysisResult? = if (!didGenerate && generators.isNotEmpty()) {
        AnalysisResult.EMPTY
    } else {
        // Nothing to do
        null
    }

    override fun analysisCompleted(
        project: Project,
        module: ModuleDescriptor,
        bindingTrace: BindingTrace,
        files: Collection<KtFile>
    ): AnalysisResult? {
        return if (!didGenerate) {
            didGenerate = true

            val readyGenerators = generators.filter { generator ->
                val evaluation = generator.shouldGenerate(
                    bindingContext = bindingTrace.bindingContext,
                    module = module,
                    projectFiles = files
                )

                when (evaluation) {
                    CodeGenerator.Evaluation.SHOULD_GENERATE -> true
                    CodeGenerator.Evaluation.SHOULD_NOT_GENERATE -> false
                }
            }

            val psiManager = PsiManager.getInstance(project)
            readyGenerators
                .flatMap { generator ->
                    generator.generate(
                        generatedFilesDirectory = generatedCodeDirectory,
                        module = module,
                        projectFiles = files
                    )
                }
                .forEach { virtualFileData ->
                    psiManager.findFile(
                        LightVirtualFile(
                            virtualFileData.file.relativeTo(generatedCodeDirectory).path,
                            KotlinFileType.INSTANCE,
                            virtualFileData.source
                        )
                    )
                }

            AnalysisResult.RetryWithAdditionalRoots(
                bindingContext = bindingTrace.bindingContext,
                moduleDescriptor = module,
                additionalJavaRoots = emptyList(),
                additionalKotlinRoots = generatedCodeDirectory.listFiles()?.toList() ?: emptyList(),
                addToEnvironment = false
            )
        } else {
            null
        }
    }
}
