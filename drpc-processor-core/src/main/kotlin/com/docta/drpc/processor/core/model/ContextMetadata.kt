package com.docta.drpc.processor.core.model

import kotlin.reflect.KClass

data class ContextMetadata(
    val qualifiedName: String,
    val simpleName: String
) {
    companion object {

        fun fromKClass(kClass: KClass<*>): ContextMetadata {
            return ContextMetadata(
                qualifiedName = kClass.qualifiedName ?: error("KClass must have a qualified name"),
                simpleName = kClass.simpleName ?: error("KClass must have a simple name")
            )
        }

    }
}