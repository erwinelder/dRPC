package com.docta.drpc.processor.server

import com.docta.drpc.processor.core.model.ServiceMetadata
import com.docta.drpc.processor.core.utils.getPackagePrefix
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
            "com.docta.drpc.server.network.processPostRoute",
            "com.docta.drpc.server.DrpcBinder",
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

            w.appendLine("class ${serviceMetadata.binderName} : DrpcBinder<${serviceMetadata.serviceName}> {")
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


    fun generateBinderRegistries(
        codeGenerator: CodeGenerator,
        services: List<KSClassDeclaration>
    ) {
        val servicesMetadata = mutableListOf<ServiceMetadata>()
        val containingFiles = mutableListOf<KSFile>()

        services.forEach { service ->
            val metadata = ServiceMetadata.fromService(service = service)
            servicesMetadata.add(metadata)
            service.containingFile?.let { containingFiles.add(it) }
        }

        generateBinderRegistries(
            codeGenerator = codeGenerator,
            servicesMetadata = servicesMetadata,
            containingFiles = containingFiles
        )
    }

    fun generateBinderRegistries(
        codeGenerator: CodeGenerator,
        servicesMetadata: List<ServiceMetadata>,
        containingFiles: List<KSFile>
    ) {
        if (servicesMetadata.isEmpty()) return

        val installerPackagePrefix = servicesMetadata.getPackagePrefix()
            ?: "com.docta.drpc.client"
        val installerPackage = "$installerPackagePrefix.drpc.generated"

        val installerFileName = "DrpcBinderRegistryProviderGenerated"

        val imports = servicesMetadata.flatMap { service ->
            setOf(
                "io.ktor.server.application.Application",
                "com.docta.drpc.server.DrpcBinderRegistryProvider",
                "com.docta.drpc.server.DrpcBinderRegistry",
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

            w.appendLine("@Suppress(\"unused\")")
            w.appendLine("class DrpcBinderRegistryProviderGenerated : DrpcBinderRegistryProvider {")
            w.appendLine()
            w.appendLine("    override fun install(application: Application) {")
            servicesMetadata.forEach { s ->
                w.appendLine("        DrpcBinderRegistry.register(")
                w.appendLine("            service = ${s.serviceName}::class,")
                w.appendLine("            binder = ${s.binderName}()")
                w.appendLine("        )")
            }
            w.appendLine("    }")
            w.appendLine()
            w.appendLine("}")
        }

        generateInstallerProviderResource(
            codeGenerator = codeGenerator,
            containingFiles = containingFiles,
            providerFqn = "$installerPackage.$installerFileName"
        )
    }

    private fun generateInstallerProviderResource(
        codeGenerator: CodeGenerator,
        containingFiles: List<KSFile>,
        providerFqn: String
    ) {
        val dependencies = Dependencies(
            aggregating = true,
            sources = containingFiles.distinct().toTypedArray()
        )
        val registryFqn = "com.docta.drpc.server.DrpcBinderRegistryProvider"
        val serviceFilePath = "META-INF/services/$registryFqn"

        val file = runCatching {
            codeGenerator.createNewFileByPath(
                dependencies = dependencies,
                path = serviceFilePath,
                extensionName = ""
            )
        }.getOrNull() ?: return // If the file already exists, we can assume it's identical and skip writing it again

        file.writer().use { w ->
            w.appendLine(providerFqn)
        }
    }

}