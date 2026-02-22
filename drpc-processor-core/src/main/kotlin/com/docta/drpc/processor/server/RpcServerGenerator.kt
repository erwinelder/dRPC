package com.docta.drpc.processor.server

import com.docta.drpc.processor.core.model.ServiceMetadata
import com.docta.drpc.processor.core.utils.writePackageAndImports
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.Writer

object RpcServerGenerator {

    fun generateRoutingBinder(
        codeGenerator: CodeGenerator,
        serviceMetadata: ServiceMetadata,
        functions: List<KSFunctionDeclaration>,
        dependencies: Dependencies
    ) {
        val imports = setOf(
            "io.ktor.server.routing.Routing",
            "io.ktor.server.routing.route",
            "com.docta.drpc.core.network.server.processPostRoute",
            "com.docta.drpc.server.RpcBinder",
            serviceMetadata.serviceQualifiedName,
            serviceMetadata.controllerQualifiedName
        )

        val out = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = serviceMetadata.packageName,
            fileName = serviceMetadata.binderName
        )

        out.writer().use { w ->
            w.writePackageAndImports(packageName = serviceMetadata.packageName, imports = imports)

            w.appendLine("class ${serviceMetadata.binderName} : RpcBinder<${serviceMetadata.serviceName}> {")
            w.appendLine()
            w.appendLine("    override fun bind(routing: Routing, service: ${serviceMetadata.serviceName}) {")
            w.appendLine("        routing.route(\"/${serviceMetadata.baseName}\") {")
            w.appendLine()

            functions.forEach { function ->
                w.writePostRouteBlock(function = function)
            }

            w.appendLine("        }")
            w.appendLine("    }")
            w.appendLine()
            w.appendLine("}")
        }
    }

    private fun Writer.writePostRouteBlock(function: KSFunctionDeclaration) {
        val functionName = function.simpleName.asString()

        appendLine("            processPostRoute(\"/$functionName\") {")
        appendLine("                service.$functionName(")

        // parameters: name = get(index)
        function.parameters.forEachIndexed { index, param ->
            val pName = param.name?.asString() ?: "p$index"
            val comma = if (index < function.parameters.lastIndex) "," else ""
            appendLine("                    $pName = get($index)$comma")
        }

        appendLine("                )")
        appendLine("            }")
        appendLine()
    }


    fun generateInstaller(
        codeGenerator: CodeGenerator,
        services: List<KSClassDeclaration>
    ) {
        val installerPackage = "com.docta.drpc.server"

        val servicesMetadata = mutableListOf<ServiceMetadata>()
        val containingFiles = mutableListOf<KSFile>()

        services.forEach { service ->
            val metadata = ServiceMetadata.fromService(service = service)
            servicesMetadata.add(metadata)
            service.containingFile?.let { containingFiles.add(it) }
        }

        generateInstaller(
            codeGenerator = codeGenerator,
            installerPackage = installerPackage,
            services = servicesMetadata,
            containingFiles = containingFiles
        )
    }

    fun generateInstaller(
        codeGenerator: CodeGenerator,
        installerPackage: String,
        services: List<ServiceMetadata>,
        containingFiles: List<KSFile>
    ) {
        if (services.isEmpty()) return

        val installerFileName = "DrpcInstallerGenerated"

        val imports = services.flatMap { service ->
            setOf(
                "io.ktor.server.application.Application",
                "com.docta.drpc.server.DrpcInstaller",
                "com.docta.drpc.server.DrpcInstallerHolder",
                service.serviceQualifiedName,
                service.binderQualifiedName
            )
        }

        val dependencies = Dependencies(
            aggregating = true,
            sources = containingFiles.distinct().toTypedArray()
        )

        val out = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = installerPackage,
            fileName = installerFileName
        )

        out.writer().use { w ->
            w.writePackageAndImports(packageName = installerPackage, imports = imports)

            w.appendLine("object DrpcInstallerGenerated : DrpcInstaller {")
            w.appendLine()
            w.appendLine("    init {")
            w.appendLine("        DrpcInstallerHolder.installer = this")
            w.appendLine("    }")
            w.appendLine()
            w.appendLine("    override fun install(application: Application) {")
            services.forEach { s ->
                w.appendLine("        RpcRegistry.binders[${s.serviceName}::class] =")
                w.appendLine("            ${s.binderName}()")
            }
            w.appendLine("    }")
            w.appendLine()
            w.appendLine("}")
        }
    }

}