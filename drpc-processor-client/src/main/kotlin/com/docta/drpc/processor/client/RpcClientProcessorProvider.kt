package com.docta.drpc.processor.client

import com.docta.drpc.processor.core.utils.getTargetPlatformFromKspOptions
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class RpcClientProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RpcClientProcessor(
            codeGenerator = environment.codeGenerator,
            targetEnvironment = environment.platforms.getTargetPlatformFromKspOptions()
        )
    }

}