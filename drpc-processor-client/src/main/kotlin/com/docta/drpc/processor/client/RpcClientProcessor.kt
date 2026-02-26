package com.docta.drpc.processor.client

import com.docta.drpc.processor.core.RpcCoreGenerator
import com.docta.drpc.processor.core.model.ServiceMetadata
import com.docta.drpc.processor.core.utils.getRpcServices
import com.docta.drpc.processor.core.utils.getServiceDependencies
import com.docta.drpc.processor.core.utils.getServiceFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class RpcClientProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val (services, invalidSymbols) = resolver.getRpcServices()

        services.forEach { it.generateCode() }

        return invalidSymbols
    }

    private fun KSClassDeclaration.generateCode() {
        val serviceMetadata = ServiceMetadata.fromService(service = this)
        val functions = getServiceFunctions()
        val dependencies = getServiceDependencies()

        RpcCoreGenerator.generateController(
            codeGenerator = codeGenerator,
            serviceMetadata = serviceMetadata,
            functions = functions,
            dependencies = dependencies
        )

        RpcClientGenerator.generateControllerImpl(
            codeGenerator = codeGenerator,
            serviceMetadata = serviceMetadata,
            functions = functions,
            dependencies = dependencies
        )
    }

}