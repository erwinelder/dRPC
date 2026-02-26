package com.docta.drpc.processor.core.utils

import com.docta.drpc.core.annotation.Rpc
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.Writer


/**
 * Scans the resolver for classes annotated with @Rpc and returns a pair of:
 * 1. List of valid KSClassDeclaration for services
 * 2. List of invalid KSAnnotated that failed validation (e.g. not interfaces)
 */
fun Resolver.getRpcServices(): Pair<List<KSClassDeclaration>, List<KSAnnotated>> {
    val symbols = getSymbolsWithAnnotation(Rpc::class.qualifiedName!!)
    val (valid, invalid) = symbols.partition { it.validate() }

    val services = valid.filterIsInstance<KSClassDeclaration>().filter { it.classKind == ClassKind.INTERFACE }

    return services to invalid
}


fun KSClassDeclaration.getServiceDependencies(): Dependencies {
    return Dependencies(
        aggregating = false,
        sources = listOfNotNull(containingFile).toTypedArray()
    )
}


fun KSClassDeclaration.getServiceFunctions(): List<KSFunctionDeclaration> {
    return getAllFunctions()
        .filter { it.isAbstract }
        .filter { it.simpleName.asString() !in setOf("equals", "hashCode", "toString") }
        .toList()
}


fun List<KSFunctionDeclaration>.collectImportQualifiedNames(): Set<String> {
    val imports = linkedSetOf<String>()

    fun addType(type: KSType) {
        val declaration = type.declaration

        // Import the top-level declared type (class/interface/typealias)
        val qualifiedName = declaration.qualifiedName?.asString()
        if (qualifiedName != null) {
            // Skip kotlin.*
            if (!qualifiedName.startsWith("kotlin.")) imports.add(qualifiedName)
        }

        // Recurse into generic arguments
        type.arguments.forEach { arg ->
            val argType = arg.type?.resolve()
            if (argType != null) addType(argType)
        }

        // Expand typealiases
        if (declaration is KSTypeAlias) {
            val expanded = declaration.type.resolve()
            addType(expanded)
        }
    }

    // Add all param/return types from functions
    forEach { function ->
        function.returnType?.resolve()?.let(::addType)
        function.parameters.forEach { p -> addType(p.type.resolve()) }
    }

    return imports
}

fun Writer.writePackageAndImports(packageName: String, imports: List<String>) {
    writePackageAndImports(
        packageName = packageName,
        imports = imports.toSet()
    )
}

fun Writer.writePackageAndImports(packageName: String, imports: Set<String>) {
    appendLine("package $packageName")
    appendLine()

    val all = imports
        .filterNot { it.substringBeforeLast(".") == packageName }
        .sorted()

    all.forEach { appendLine("import $it") }
    if (all.isNotEmpty()) appendLine()
}