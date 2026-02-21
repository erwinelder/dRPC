package com.docta.drpc.kspProcessor

import com.docta.drpc.core.annotation.Rpc
import com.docta.drpc.core.network.context.DrpcContext
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.Writer

class RpcProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Rpc::class.qualifiedName!!)
        val (valid, invalid) = symbols.partition { it.validate() }

        valid.filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE }
            .forEach { service ->
                generateControllerWithImpl(service = service)
            }

        return invalid
    }

    private fun generateControllerWithImpl(service: KSClassDeclaration) {
        val packageName = service.packageName.asString()
        val serviceName = service.simpleName.asString()
        val baseName = serviceName.removeSuffix("Service")
        val controllerName = "${baseName}RestController"
        val controllerImplName = "${controllerName}Impl"

        val baseHttpUrl = service.annotations
            .firstOrNull { it.shortName.asString() == "Rpc" }
            ?.arguments
            ?.firstOrNull { it.name?.asString() == "serviceBaseHttpUrl" }
            ?.value
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: error("@Rpc(serviceBaseHttpUrl) is required and must be non-blank for $serviceName")

        val contextType = ContextTypeInfo.fromKClass(kClass = DrpcContext::class)

        val functions = service.getAllFunctions()
            .filter { it.isAbstract }
            .filter { it.simpleName.asString() !in setOf("equals", "hashCode", "toString") }
            .toList()

        val dependencies = Dependencies(
            aggregating = false,
            sources = listOfNotNull(service.containingFile).toTypedArray()
        )


        generateController(
            packageName = packageName,
            serviceName = serviceName,
            baseName = baseName,
            controllerName = controllerName,
            baseHttpUrl = baseHttpUrl,
            functions = functions,
            dependencies = dependencies
        )

        generateControllerImpl(
            packageName = packageName,
            controllerName = controllerName,
            controllerImplName = controllerImplName,
            contextType = contextType,
            functions = functions,
            dependencies = dependencies
        )

    }

    private fun generateController(
        packageName: String,
        serviceName: String,
        baseName: String,
        controllerName: String,
        baseHttpUrl: String,
        functions: List<KSFunctionDeclaration>,
        dependencies: Dependencies
    ) {
        val controllerOut = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = packageName,
            fileName = controllerName
        )

        controllerOut.writer().use { w ->
            w.appendLine("package $packageName")
            w.appendLine()

            w.appendLine("interface $controllerName : $serviceName {")
            w.appendLine()
            w.appendLine("    val serviceBaseHttpUrl: String")
            w.appendLine("        get() = \"$baseHttpUrl\"")
            w.appendLine()
            w.appendLine("    val serviceRoute: String")
            w.appendLine("        get() = \"/$baseName\"")
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

    private fun generateControllerImpl(
        packageName: String,
        controllerName: String,
        controllerImplName: String,
        contextType: ContextTypeInfo,
        functions: List<KSFunctionDeclaration>,
        dependencies: Dependencies
    ) {
        val imports = collectImportQualifiedNames(functions = functions).toMutableSet().apply {
            add(contextType.qualifiedName)
            add("com.docta.drpc.core.network.client.callPost")
            add("com.docta.drpc.core.network.asCallParameter")
            add("io.ktor.client.HttpClient")
        }

        val controllerImplOut = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = packageName,
            fileName = controllerImplName
        )

        controllerImplOut.writer().use { w ->
            w.appendLine("package $packageName")
            w.writeImports(packageName = packageName, imports = imports)

            w.appendLine("class ${controllerImplName}(")
            w.appendLine("    private val httpClient: HttpClient")
//            w.appendLine("    private val webSocketClient: HttpClient") // TODO-websocket-support
            w.appendLine(") : $controllerName {")
            w.appendLine()

            for (function in functions) {
                generateOverridingFunctions(w = w, contextType = contextType, function = function)
            }

            w.appendLine("}")
        }
    }

    private fun generateOverridingFunctions(
        w: Writer,
        contextType: ContextTypeInfo,
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

        val paramNames = function.parameters.map { it.name?.asString() ?: "p" }
        val callParams = paramNames.joinToString(",\n            ") { "$it.asCallParameter()" }

        w.appendLine("    context(context: ${contextType.simpleName})")
        w.append("    override ")
        if (isSuspend) w.append("suspend ")
        w.appendLine("fun $functionName(")
        if (funParams.isNotBlank()) w.appendLine("        $funParams")
        w.appendLine("    ): $returnType {")
        w.appendLine("        return httpClient.callPost(")
        w.append("            url = absoluteHttpUrl + ${functionName}Path")
        if (callParams.isNotBlank()) w.appendLine(",\n            $callParams") else w.appendLine()
        w.appendLine("        )")
        w.appendLine("    }")
        w.appendLine()
    }


    private fun collectImportQualifiedNames(functions: List<KSFunctionDeclaration>): Set<String> {
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
        for (function in functions) {
            function.returnType?.resolve()?.let(::addType)
            function.parameters.forEach { p -> addType(p.type.resolve()) }
        }

        return imports
    }

    private fun Writer.writeImports(packageName: String, imports: Set<String>) {
        appendLine()

        val all = imports
            .filter { !it.startsWith("$packageName.") }
            .sorted()

        all.forEach { appendLine("import $it") }
        if (all.isNotEmpty()) appendLine()
    }

}