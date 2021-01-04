package com.noheltcj.zinc.compiler.plugin.compilation.generator

import com.noheltcj.zinc.compiler.plugin.compilation.extension.requireFqName
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.getPossiblyQualifiedCallExpression
import org.jetbrains.kotlin.resolve.BindingContext
import java.io.File

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
                    ?.takeIf { it.count() > 0 }
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
        .takeIf(String::isNotEmpty)
        ?.let { "$generatedDisclaimer\n\npackage $it" }
        ?: generatedDisclaimer

    private fun importsSource(module: ModuleDescriptor, psiClass: KtClassOrObject) =
        filterToKtUserTypeConstructorParams(psiClass)
            .map { "import ${requireNotNull(it.typeReference).requireFqName(module)}" }
            .plus("import com.noheltcj.zinc.core.BuilderProperty")
            .plus("import com.noheltcj.zinc.core.ZincBuilder")
            .sorted()
            .joinToString(separator = "\n")

    private fun classSource(psiClass: KtClassOrObject): String {
        val className = requireNotNull(psiClass.name)
        val builderName = "${className}Builder"

        val parameterMetadata = psiClass.primaryConstructorParameters
            .map { param ->
                ConstructorParameterMetadata(
                    propertyName = requireNotNull(param.name),
                    typeName = requireNotNull(param.typeReference).text,
                    default = param.defaultValue
                        ?.let { defaultExpression ->
                            ConstructorParameterMetadata.DefaultValue.Some(
                                expression = defaultExpression.getPossiblyQualifiedCallExpression()?.text
                                    ?: defaultExpression.text
                            )
                        }
                        ?: ConstructorParameterMetadata.DefaultValue.None,
                )
            }

        val parameterDefinitionsSource = parameterMetadata.joinToString(separator = "\n\n") { metadata ->
            "\tprivate var _${metadata.propertyName}: ${metadata.typeName} by BuilderProperty<${metadata.typeName}>(${
                when (val default = metadata.default) {
                    ConstructorParameterMetadata.DefaultValue.None -> ""
                    is ConstructorParameterMetadata.DefaultValue.Some -> "\n\t\tvalue = ${default.expression}"
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
            "\t\tprivate const val ${metadata.propertyName}Description = \n\t\t\t" +
                "\"\$builderName property \\\"${metadata.propertyName}\\\" with type: ${metadata.typeName} and ${
                    when (val default = metadata.default) {
                        ConstructorParameterMetadata.DefaultValue.None -> "no default value"
                        is ConstructorParameterMetadata.DefaultValue.Some -> "default value expression \\\"${default.expression}\\\""
                    }
                }\""
        }.plus(
            "\t\tinline fun build${className}(crossinline block: $builderName.() -> Unit): $className =\n" +
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