package com.docta.drpc.processor.core

import com.docta.drpc.processor.core.model.ServiceMetadata
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

object RpcCoreGenerator {

    fun generateController(
        codeGenerator: CodeGenerator,
        serviceMetadata: ServiceMetadata,
        functions: List<KSFunctionDeclaration>,
        dependencies: Dependencies
    ) {
        val out = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = serviceMetadata.packageName,
            fileName = serviceMetadata.controllerName
        )

        out.writer().use { w ->
            w.appendLine("package ${serviceMetadata.packageName}")
            w.appendLine()

            w.appendLine("interface ${serviceMetadata.controllerName} : ${serviceMetadata.serviceName} {")
            w.appendLine()
            w.appendLine("    abstract val serviceBaseHttpUrl: String")
            w.appendLine()
            w.appendLine("    val serviceRoute: String")
            w.appendLine("        get() = \"/${serviceMetadata.baseName}\"")
            w.appendLine()
            w.appendLine("    val absoluteHttpUrl: String")
            w.appendLine("        get() = serviceBaseHttpUrl + serviceRoute")
            w.appendLine()
            w.appendLine("    val absoluteWsUrl: String")
            w.appendLine("        get() = (serviceBaseHttpUrl + serviceRoute).let {")
            w.appendLine("            when {")
            w.appendLine("                it.startsWith(\"https://\") -> it.replaceFirst(\"https://\", \"wss://\")")
            w.appendLine("                it.startsWith(\"http://\") -> it.replaceFirst(\"http://\", \"ws://\")")
            w.appendLine("                else -> it")
            w.appendLine("            }")
            w.appendLine("        }")
            w.appendLine()
            w.appendLine()

            functions.forEach { function ->
                val functionName = function.simpleName.asString()
                w.appendLine("    val ${functionName}Path: String")
                w.appendLine("        get() = \"/${functionName}\"")
                w.appendLine()
            }

            w.appendLine("}")
        }
    }

}