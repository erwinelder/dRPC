package com.docta.drpc.processor

import com.docta.drpc.processor.core.utils.getTargetPlatformFromKspOptions
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class RpcProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RpcProcessor(
            codeGenerator = environment.codeGenerator,
            targetEnvironment = environment.platforms.getTargetPlatformFromKspOptions()
        )
    }

}