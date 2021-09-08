package com.noheltcj.zinc.compiler.plugin.compilation.extension

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassifierDescriptorWithTypeParameters
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.findTypeAliasAcrossModuleDependencies
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtTypeArgumentList
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.supertypes

/**
 * Thanks Anvil developers,
 * This file has been augmented, but retains quite a bit of code that was originally written by developers on the
 * Anvil project: https://github.com/square/anvil/blob/1bd03e885939a40bf3313f1a67b0de0178d43694/compiler/src/main/java/com/squareup/anvil/compiler/codegen/PsiUtils.kt
 */
fun PsiElement.requireFqName(
    module: ModuleDescriptor
): FqName {
    val containingKtFile = parentsWithSelf
        .filterIsInstance<KtClassOrObject>()
        .first()
        .containingKtFile

    fun failTypeHandling(): Nothing = throw IllegalArgumentException(
        "Don't know how to handle Psi element: $text"
    )

    val classReference = when (this) {
        is KtDotQualifiedExpression -> return FqName(text)
        is KtNameReferenceExpression -> getReferencedName()
        is KtUserType -> {
            val isGenericType = children.any { it is KtTypeArgumentList }
            if (isGenericType) {
                referencedName ?: failTypeHandling()
            } else {
                val text = text

                // Sometimes a KtUserType is a fully qualified name. Give it a try and return early.
                if (text.contains(".") && text[0].isLowerCase()) {
                    module
                        .resolveClassByFqName(FqName(text), NoLookupLocation.FROM_BACKEND)
                        ?.let { return it.fqNameSafe }
                }

                // We can't use referencedName here. For inner classes like "Outer.Inner" it would only
                // return "Inner", whereas text returns "Outer.Inner", what we expect.
                text
            }
        }
        is KtTypeReference -> {
            val children = children
            if (children.size == 1) {
                try {
                    // Could be a KtNullableType or KtUserType.
                    return children[0].requireFqName(module)
                } catch (e: IllegalArgumentException) {
                    // Fallback to the text representation.
                    text
                }
            } else {
                text
            }
        }
        is KtNullableType -> return innerType?.requireFqName(module) ?: failTypeHandling()
        is KtAnnotationEntry -> return typeReference?.requireFqName(module) ?: failTypeHandling()
        else -> failTypeHandling()
    }

    // E.g. OuterClass.InnerClass
    val classReferenceOuter = classReference.substringBefore(".")

    val importPaths = containingKtFile.importDirectives.mapNotNull { it.importPath }

    // First look in the imports for the reference name. If the class is imported, then we know the
    // fully qualified name.
    importPaths
        .filter { it.alias == null && it.fqName.shortName().asString() == classReference }
        .also { matchingImportPaths ->
            when {
                matchingImportPaths.size == 1 ->
                    return matchingImportPaths[0].fqName
                matchingImportPaths.size > 1 ->
                    return matchingImportPaths.first { importPath ->
                        module.resolveClassByFqName(importPath.fqName, NoLookupLocation.FROM_BACKEND) != null
                    }.fqName
            }
        }

    importPaths
        .filter { it.alias == null && it.fqName.shortName().asString() == classReferenceOuter }
        .also { matchingImportPaths ->
            when {
                matchingImportPaths.size == 1 ->
                    return FqName("${matchingImportPaths[0].fqName.parent()}.$classReference")
                matchingImportPaths.size > 1 ->
                    return matchingImportPaths.first { importPath ->
                        val fqName = FqName("${importPath.fqName.parent()}.$classReference")
                        module.resolveClassByFqName(fqName, NoLookupLocation.FROM_BACKEND) != null
                    }.fqName
            }
        }

    // If there is no import, then try to resolve the class with the same package as this file.
    module.findClassOrTypeAlias(containingKtFile.packageFqName, classReference)
        ?.let { return it.fqNameSafe }

    // If this doesn't work, then maybe a class from the Kotlin package is used.
    module.resolveClassByFqName(FqName("kotlin.$classReference"), NoLookupLocation.FROM_BACKEND)
        ?.let { return it.fqNameSafe }

    // If this doesn't work, then maybe a class from the Kotlin collection package is used.
    module.resolveClassByFqName(FqName("kotlin.collections.$classReference"), NoLookupLocation.FROM_BACKEND)
        ?.let { return it.fqNameSafe }

    // If this doesn't work, then maybe a class from the Kotlin jvm package is used.
    module.resolveClassByFqName(FqName("kotlin.jvm.$classReference"), NoLookupLocation.FROM_BACKEND)
        ?.let { return it.fqNameSafe }

    // Or java.lang.
    module.resolveClassByFqName(FqName("java.lang.$classReference"), NoLookupLocation.FROM_BACKEND)
        ?.let { return it.fqNameSafe }

    findFqNameInSuperTypes(module, classReference)
        ?.let { return it }

    containingKtFile.importDirectives
        .asSequence()
        .filter { it.isAllUnder }
        .mapNotNull {
            // This fqName is the everything in front of the star, e.g. for "import java.io.*" it
            // returns "java.io".
            it.importPath?.fqName
        }
        .forEach { importFqName ->
            module.findClassOrTypeAlias(importFqName, classReference)?.let { return it.fqNameSafe }
        }

    // Check if it's a named import.
    containingKtFile.importDirectives
        .firstOrNull { classReference == it.importPath?.importedName?.asString() }
        ?.importedFqName
        ?.let { return it }

    // Everything else isn't supported.
    throw IllegalArgumentException(
        "Couldn't resolve FqName $classReference for Psi element: $text"
    )
}

fun KtNamedDeclaration.requireFqName(): FqName = requireNotNull(fqName) {
    "fqName was null for $this, $nameAsSafeName"
}

private fun PsiElement.findFqNameInSuperTypes(
    module: ModuleDescriptor,
    classReference: String
): FqName? {
    fun tryToResolveClassFqName(outerClass: FqName): FqName? =
        module
            .resolveClassByFqName(FqName("$outerClass.$classReference"), NoLookupLocation.FROM_BACKEND)
            ?.fqNameSafe

    return parents.filterIsInstance<KtClassOrObject>()
        .flatMap { clazz ->
            tryToResolveClassFqName(clazz.requireFqName())?.let { return@flatMap sequenceOf(it) }

            // At this point we can't work with Psi APIs anymore. We need to resolve the super types
            // and try to find inner class in them.
            val descriptor = module.resolveClassByFqName(clazz.requireFqName(), NoLookupLocation.FROM_BACKEND)
                ?: throw IllegalArgumentException(
                    "Couldn't resolve class descriptor for ${clazz.requireFqName()}"
                )

            listOf(descriptor.defaultType).getAllSuperTypes()
                .mapNotNull { tryToResolveClassFqName(it) }
        }
        .firstOrNull()
}

fun KotlinType.classDescriptorForType() = DescriptorUtils.getClassDescriptorForType(this)

fun List<KotlinType>.getAllSuperTypes(): Sequence<FqName> =
    generateSequence(this) { kotlinTypes ->
        kotlinTypes.ifEmpty { null }?.flatMap { it.supertypes() }
    }
        .flatMap { it.asSequence() }
        .map { DescriptorUtils.getFqNameSafe(it.classDescriptorForType()) }

fun ModuleDescriptor.findClassOrTypeAlias(
    packageName: FqName,
    className: String
): ClassifierDescriptorWithTypeParameters? {
    resolveClassByFqName(FqName("${
        if (packageName.isRoot) {
            ""
        } else {
            "${packageName.asString()}."
        }
    }$className"), NoLookupLocation.FROM_BACKEND)
        ?.let { return it }

    findTypeAliasAcrossModuleDependencies(ClassId(packageName, Name.identifier(className)))
        ?.let { return it }

    return null
}

fun PsiElement.recurseChildrenInclusive(): Sequence<PsiElement> =
    sequenceOf(this)
        .let { sequence ->
            if (children.isNotEmpty()) {
                sequence
                    .plus(
                        children.flatMap { child ->
                            child.recurseChildrenInclusive()
                        }
                    )
                    .distinct()
            } else {
                sequence
            }
        }

inline fun <reified T> PsiElement.recurseTreeForInstancesOf(): Sequence<T> {
    val children = recurseChildrenInclusive()
    return children.filterIsInstance<T>()
}
