package com.docta.drpc.processor.server

import com.docta.drpc.processor.core.RpcCoreGenerator
import com.docta.drpc.processor.core.model.ServiceMetadata
import com.docta.drpc.processor.core.utils.getRpcServices
import com.docta.drpc.processor.core.utils.getServiceBaseHttpUrl
import com.docta.drpc.processor.core.utils.getServiceDependencies
import com.docta.drpc.processor.core.utils.getServiceFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class RpcServerProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val (services, invalidSymbols) = resolver.getRpcServices()

        services.forEach { it.generateCode() }

        RpcServerGenerator.generateInstaller(
            codeGenerator = codeGenerator,
            services = services
        )

        return invalidSymbols
    }

    private fun KSClassDeclaration.generateCode() {
        val serviceMetadata = ServiceMetadata.fromService(service = this)
        val baseHttpUrl = getServiceBaseHttpUrl()
        val functions = getServiceFunctions()
        val dependencies = getServiceDependencies()

        RpcCoreGenerator.generateController(
            codeGenerator = codeGenerator,
            serviceMetadata = serviceMetadata,
            baseHttpUrl = baseHttpUrl,
            functions = functions,
            dependencies = dependencies
        )

        RpcServerGenerator.generateRoutingBinder(
            codeGenerator = codeGenerator,
            serviceMetadata = serviceMetadata,
            functions = functions,
            dependencies = dependencies
        )
    }

}