package com.noheltcj.zinc.compiler.plugin.compilation.generator

import com.noheltcj.zinc.compiler.plugin.compilation.extension.recurseTreeForInstancesOf
import com.noheltcj.zinc.compiler.plugin.compilation.extension.requireFqName
import java.io.File
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForReceiver
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector
import org.jetbrains.kotlin.resolve.BindingContext

class DataClassBuilderGenerator : CodeGenerator {

    override fun shouldGenerate(
        bindingContext: BindingContext,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>
    ): CodeGenerator.Evaluation {

        // TODO: Evaluate this with the presence of other compiler plugins and annotation processors
        val kotlinClasses = projectFiles
            .filter { it.fileType == KotlinFileType.INSTANCE }
            .flatMap { it.classesAndInnerClasses() }

        if (kotlinClasses.isEmpty()) {
            return CodeGenerator.Evaluation.SHOULD_NOT_GENERATE
        }

        val dataClasses = kotlinClasses
            .filter { it.modifierList?.text?.contains("data") == true }

        if (dataClasses.isEmpty()) {
            return CodeGenerator.Evaluation.SHOULD_NOT_GENERATE
        }

        return CodeGenerator.Evaluation.SHOULD_GENERATE
    }

    override fun generate(
        generatedFilesDirectory: File,
        module: ModuleDescriptor,
        projectFiles: Collection<KtFile>
    ): List<CodeGenerator.VirtualFileData> {

        val dataClasses = projectFiles
            .filter { it.fileType == KotlinFileType.INSTANCE }
            .flatMap { file ->
                file.classesAndInnerClasses()
                    .filter { it.modifierList?.text?.contains("data") == true }
            }

        return dataClasses
            .filter { ktClass ->
                // Filter to discoverable names only
                ktClass.name
                    ?.filter { it.isLetterOrDigit() }
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { true }
                    ?: false
            }
            .map { ktClass ->
                val packageName = generatePackageName(ktClass)
                val className = generateClassName(ktClass)

                val directory = File(generatedFilesDirectory, packageName.replace('.', File.separatorChar))
                val file = File(directory, "$className.kt")

                check(file.parentFile.exists() || file.parentFile.mkdirs()) {
                    "Could not generate package directory: ${file.parentFile}"
                }

                val source = """${headerAndPackageSource(ktClass)}    
                        
${importsSource(module, ktClass)}

${classSource(ktClass)}
""".trimIndent()

                file.writeText(source)

                CodeGenerator.VirtualFileData(file = file, source = source)
            }
    }

    private fun KtFile.classesAndInnerClasses(): Sequence<KtClassOrObject> {
        val children = findChildrenByClass(KtClassOrObject::class.java)

        return generateSequence(children.toList()) { list ->
            list
                .flatMap { it.declarations.filterIsInstance<KtClassOrObject>() }
                .ifEmpty { null }
        }.flatMap { it }
    }

    private fun headerAndPackageSource(psiClass: KtClassOrObject) = generatePackageName(psiClass)
        .takeIf { pkg -> pkg.isNotEmpty() && pkg != "<root>" }
        ?.let { "$generatedDisclaimer\n\npackage $it" }
        ?: generatedDisclaimer

    private fun importsSource(module: ModuleDescriptor, psiClass: KtClassOrObject) =
        filterToKtUserTypeConstructorParams(psiClass)
            .asSequence()
            // TODO: Resolve case where generic is used in constructor
            .map { "import ${requireNotNull(it.typeReference).requireFqName(module)}" to it }
            .flatMap { (import, parameter) ->
                extractAdditionalParameterImports(module, parameter)
                    .plus(import)
            }
            .plus("import com.noheltcj.zinc.core.BuilderProperty")
            .plus("import com.noheltcj.zinc.core.ZincBuilder")
            .plus(psiClass.containingKtFile.importDirectives.map { it.text })
            .distinct()
            .sorted()
            .joinToString(separator = "\n")

    private fun classSource(psiClass: KtClassOrObject): String {
        val className = requireNotNull(psiClass.name)
        val builderName = "${className}Builder"

        val parameterMetadata = getConstructorParamMetadata(psiClass)

        val parameterDefinitionsSource = parameterMetadata.joinToString(separator = "\n\n") { metadata ->
            "\tprivate var _${metadata.propertyName}: ${metadata.typeName} by BuilderProperty<${metadata.typeName}>(${
                when (val default = metadata.default) {
                    ConstructorParameterMetadata.DefaultValue.None -> ""
                    is ConstructorParameterMetadata.DefaultValue.Some -> "\n\t\tdefaultValue = ${
                        default.expression
                    },"
                } + "\n\t\tpropertyDescription = ${metadata.propertyName}Description"
            }\n\t)"
        }

        val builderFunctionsSource = parameterMetadata.joinToString(separator = "\n\n") { metadata ->
            "\tfun ${metadata.propertyName}(value: ${metadata.typeName}): $builderName {\n" +
                "\t\tthis._${metadata.propertyName} = value\n" +
                "\t\treturn this\n" +
                "\t}"
        }

        val buildFunctionSource = parameterMetadata.joinToString(
            separator = ",\n",
            prefix = "\toverride fun build() = $className(\n",
            postfix = "\n\t)"
        ) { metadata ->
            "\t\t${metadata.propertyName} = this._${metadata.propertyName}"
        }

        val companionObjectSource = parameterMetadata.joinToString(
            separator = "\n",
            prefix = "\tcompanion object {\n" +
                "\t\tprivate const val builderName = \"$builderName\"\n",
            postfix = "\n\n"
        ) { metadata ->
            "\t\tprivate val ${metadata.propertyName}Description = \n\t\t\t" +
                "\"\$builderName property \\\"${metadata.propertyName}\\\" with type: ${metadata.typeName} and ${
                    when (val default = metadata.default) {
                        ConstructorParameterMetadata.DefaultValue.None -> "no default value\""
                        is ConstructorParameterMetadata.DefaultValue.Some -> "default value expression: \" + ${
                            default.expression
                        }"
                    }
                }"
        }.plus(
            "\t\t@JvmStatic inline fun build$className(crossinline block: $builderName.() -> Unit = {}): $className =\n" +
                "\t\t\t$builderName().apply(block).build()\n" +
                "\t}"
        )

        return "class $builderName : ZincBuilder<$className> {" +
            "\n$parameterDefinitionsSource" +
            "\n\n$builderFunctionsSource" +
            "\n\n$buildFunctionSource" +
            "\n\n$companionObjectSource" +
            "\n}"
    }

    private fun generatePackageName(psiClass: KtClassOrObject): String =
        "${psiClass.containingKtFile.packageFqName}"

    private fun generateClassName(psiClass: KtClassOrObject): String =
        requireNotNull(
            psiClass.name
                ?.filter { it.isLetterOrDigit() }
                ?.plus("Builder")
        )

    private fun filterToKtUserTypeConstructorParams(psiClass: KtClassOrObject) = psiClass.primaryConstructorParameters
        .filter { it.typeReference != null }
        .filter { requireNotNull(it.typeReference).typeElement is KtUserType }

    private fun getConstructorParamMetadata(psiClass: KtClassOrObject) = psiClass.primaryConstructorParameters
        .map { param ->
            ConstructorParameterMetadata(
                propertyName = requireNotNull(param.name),
                typeName = requireNotNull(param.typeReference).text,
                default = param.defaultValue
                    ?.let { defaultExpression ->
                        ConstructorParameterMetadata.DefaultValue.Some(
                            expression = defaultExpression.getQualifiedExpressionForSelector()?.text
                                ?: defaultExpression.getQualifiedExpressionForReceiver()?.text
                                ?: defaultExpression.text
                        )
                    }
                    ?: ConstructorParameterMetadata.DefaultValue.None
            )
        }

    private fun extractAdditionalParameterImports(module: ModuleDescriptor, ktParameter: KtParameter): Sequence<String> =
        ktParameter.defaultValue
            ?.let { defaultExpression ->
                defaultExpression.recurseTreeForInstancesOf<KtNameReferenceExpression>()
                    .mapNotNull { expression ->
                        kotlin.runCatching { "import ${expression.requireFqName(module)}" }
                            .getOrNull()
                    }
            }
            ?: emptySequence()

    private data class ConstructorParameterMetadata(
        val propertyName: String,
        val typeName: String,
        val default: DefaultValue
    ) {

        sealed class DefaultValue {
            object None : DefaultValue()
            data class Some(val expression: String) : DefaultValue()
        }
    }

    companion object {
        private const val generatedDisclaimer = """/*
 * Generated by Zinc
 * Editing this file is not useful
 */"""
    }
}
