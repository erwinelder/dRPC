package com.docta.drpc.processor.client

import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.processor.core.DrpcTargetEnvironment
import com.docta.drpc.processor.core.model.ContextMetadata
import com.docta.drpc.processor.core.model.ServiceMetadata
import com.docta.drpc.processor.core.utils.collectImportQualifiedNames
import com.docta.drpc.processor.core.utils.writePackageAndImports
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
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
        val contextMetadata = ContextMetadata.fromKClass(kClass = DrpcContext::class)

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
            w.appendLine("    override val baseHttpUrl: String,")
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

    fun generateRpcClientFactory(
        codeGenerator: CodeGenerator,
        serviceMetadata: ServiceMetadata,
        dependencies: Dependencies
    ) {
        val imports = setOf(
            "io.ktor.client.HttpClient",
            "com.docta.drpc.client.DrpcClientFactory",
            "${serviceMetadata.controllerQualifiedName}Impl",
            serviceMetadata.serviceQualifiedName
        )

        val out = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = serviceMetadata.packageName,
            fileName = serviceMetadata.clientFactoryName
        )

        out.writer().use { w ->
            w.writePackageAndImports(packageName = serviceMetadata.packageName, imports = imports)

            w.appendLine("object ${serviceMetadata.clientFactoryName} : DrpcClientFactory<${serviceMetadata.serviceName}> {")
            w.appendLine()
            w.appendLine("    override fun create(baseHttpUrl: String, httpClient: HttpClient): ${serviceMetadata.serviceName} {")
            w.appendLine("        return ${serviceMetadata.controllerName}Impl(")
            w.appendLine("            baseHttpUrl = baseHttpUrl,")
            w.appendLine("            httpClient = httpClient")
            w.appendLine("        )")
            w.appendLine("    }")
            w.appendLine()
            w.appendLine("}")
        }
    }


    fun generateClientFactoryRegistry(
        codeGenerator: CodeGenerator,
        targetEnvironment: DrpcTargetEnvironment,
        services: List<KSClassDeclaration>
    ) {
        val servicesMetadata = mutableListOf<ServiceMetadata>()
        val containingFiles = mutableListOf<KSFile>()

        services.forEach { service ->
            val metadata = ServiceMetadata.fromService(service = service)
            servicesMetadata.add(metadata)
            service.containingFile?.let { containingFiles.add(it) }
        }

        generateClientFactoryRegistry(
            codeGenerator = codeGenerator,
            targetEnvironment = targetEnvironment,
            servicesMetadata = servicesMetadata,
            containingFiles = containingFiles
        )
    }

    fun generateClientFactoryRegistry(
        codeGenerator: CodeGenerator,
        targetEnvironment: DrpcTargetEnvironment,
        servicesMetadata: List<ServiceMetadata>,
        containingFiles: List<KSFile>
    ) {
        val installerPackagePrefix = servicesMetadata
            .map { it.packageName }
            .reduceOrNull { acc, string ->
                if (acc == string) acc else acc.take(acc.commonPrefixWith(string).length)
            }
            ?.takeIf { it.isNotBlank() }
            ?: "com.docta.drpc.client"
        val installerPackage = "$installerPackagePrefix.drpc.generated"
        val installerPackagePrefixSnakeCase = installerPackagePrefix.replace(".", "_")

        if (servicesMetadata.isEmpty()) return

        val registryFileName = "DrpcClientFactoryRegistryGenerated"

        val imports = servicesMetadata.flatMap { service ->
            setOfNotNull(
                "kotlin.native.EagerInitialization".takeIf { targetEnvironment == DrpcTargetEnvironment.Ios },
                "kotlin.OptIn".takeIf { targetEnvironment == DrpcTargetEnvironment.Ios },
                "kotlin.ExperimentalStdlibApi".takeIf { targetEnvironment == DrpcTargetEnvironment.Ios },
                "com.docta.drpc.client.DrpcClientFactoryRegistryProvider",
                "com.docta.drpc.client.DrpcClientFactories",
                service.serviceQualifiedName,
                service.clientFactoryQualifiedName
            )
        }

        val dependencies = Dependencies(
            aggregating = true,
            sources = containingFiles.distinct().toTypedArray()
        )

        val out = codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = installerPackage,
            fileName = registryFileName
        )

        out.writer().use { w ->
            w.writePackageAndImports(packageName = installerPackage, imports = imports)

            if (targetEnvironment == DrpcTargetEnvironment.Android || targetEnvironment == DrpcTargetEnvironment.Jvm) {
                w.appendLine("@Suppress(\"unused\")")
                w.appendLine("class DrpcClientFactoryRegistryGenerated : DrpcClientFactoryRegistryProvider {")
                w.appendLine()
                w.appendLine("    override fun install() {")
                servicesMetadata.forEach { s ->
                    w.appendLine("        DrpcClientFactories.register(")
                    w.appendLine("            service = ${s.serviceName}::class,")
                    w.appendLine("            factory = ${s.clientFactoryName}")
                    w.appendLine("        )")
                }
                w.appendLine("    }")
                w.appendLine()
                w.appendLine("}")
                w.appendLine()
            }
            if (targetEnvironment == DrpcTargetEnvironment.Ios) {
                val registerFunName = "__drpcRegisterClientFactories_$installerPackagePrefixSnakeCase"
                val initValName = "__drpcClientFactoriesInit_$installerPackagePrefixSnakeCase"
                w.appendLine("@OptIn(ExperimentalStdlibApi::class)")
                w.appendLine("internal fun $registerFunName() {")
                servicesMetadata.forEach { s ->
                    w.appendLine("    DrpcClientFactories.register(")
                    w.appendLine("        service = ${s.serviceName}::class,")
                    w.appendLine("        factory = ${s.clientFactoryName}")
                    w.appendLine("    )")
                }
                w.appendLine("}")
                w.appendLine()
                w.appendLine("@EagerInitialization")
                w.appendLine("private val $initValName = run {")
                w.appendLine("    $registerFunName()")
                w.appendLine("}")
                w.appendLine()
            }
        }

        if (targetEnvironment == DrpcTargetEnvironment.Android || targetEnvironment == DrpcTargetEnvironment.Jvm) {
            generateClientFactoryRegistryProviderResource(
                codeGenerator = codeGenerator,
                containingFiles = containingFiles,
                providerFqn = "$installerPackage.$registryFileName"
            )
        }
    }

    private fun generateClientFactoryRegistryProviderResource(
        codeGenerator: CodeGenerator,
        containingFiles: List<KSFile>,
        providerFqn: String
    ) {
        val dependencies = Dependencies(
            aggregating = true,
            sources = containingFiles.distinct().toTypedArray()
        )
        val registryFqn = "com.docta.drpc.client.DrpcClientFactoryRegistryProvider"
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