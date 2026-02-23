package com.docta.drpc.processor.client

import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.processor.core.model.ContextMetadata
import com.docta.drpc.processor.core.model.ServiceMetadata
import com.docta.drpc.processor.core.utils.collectImportQualifiedNames
import com.docta.drpc.processor.core.utils.writePackageAndImports
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import java.io.Writer

object RpcClientGenerator {

    fun generateControllerImpl(
        codeGenerator: CodeGenerator,
        serviceMetadata: ServiceMetadata,
        functions: List<KSFunctionDeclaration>,
        dependencies: Dependencies
    ) {
        val controllerImplName = "${serviceMetadata.controllerName}Impl"
        val contextMetadata = ContextMetadata.Companion.fromKClass(kClass = DrpcContext::class)

        val imports = functions.collectImportQualifiedNames() + setOf(
            contextMetadata.qualifiedName,
            "com.docta.drpc.client.network.asCallParameter",
            "com.docta.drpc.client.network.callPost",
            "io.ktor.client.HttpClient"
        )

        val out = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = serviceMetadata.packageName,
            fileName = controllerImplName
        )

        out.writer().use { w ->
            w.writePackageAndImports(packageName = serviceMetadata.packageName, imports = imports)

            w.appendLine("class ${controllerImplName}(")
            w.appendLine("    private val httpClient: HttpClient")
//            w.appendLine("    private val webSocketClient: HttpClient") // TODO-websocket-support
            w.appendLine(") : ${serviceMetadata.controllerName} {")
            w.appendLine()

            for (function in functions) {
                w.generateOverridingFunctions(contextType = contextMetadata, function = function)
            }

            w.appendLine("}")
        }
    }

    private fun Writer.generateOverridingFunctions(
        contextType: ContextMetadata,
        function: KSFunctionDeclaration
    ) {
        val functionName = function.simpleName.asString()
        val isSuspend = function.modifiers.contains(Modifier.SUSPEND)

        val funParams = function.parameters.joinToString(",\n        ") { param ->
            val name = param.name?.asString() ?: "p"
            val type = param.type.resolve().toString()

            "$name: $type"
        }

        val returnType = function.returnType?.resolve()?.toString() ?: "kotlin.Unit"
        val returnsUnit = returnType == "kotlin.Unit" || returnType == "Unit"

        val paramNames = function.parameters.map { it.name?.asString() ?: "p" }
        val callParams = paramNames.joinToString(",\n            ") { "$it.asCallParameter()" }

        appendLine("    context(context: ${contextType.simpleName})")
        append("    override ")
        if (isSuspend) append("suspend ")
        appendLine("fun $functionName(")
        if (funParams.isNotBlank()) appendLine("        $funParams")
        if (returnsUnit) {
            appendLine("    ) {")
        } else {
            appendLine("    ): $returnType {")
        }
        appendLine("        return httpClient.callPost(")
        append("            url = absoluteHttpUrl + ${functionName}Path")
        if (callParams.isNotBlank()) appendLine(",\n            $callParams") else appendLine()
        appendLine("        )")
        appendLine("    }")
        appendLine()
    }

}