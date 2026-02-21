package com.docta.drpc.kspProcessor

import kotlin.reflect.KClass

data class ContextTypeInfo(
    val qualifiedName: String,
    val simpleName: String
) {
    companion object {

        fun fromKClass(kClass: KClass<*>): ContextTypeInfo {
            return ContextTypeInfo(
                qualifiedName = kClass.qualifiedName ?: error("KClass must have a qualified name"),
                simpleName = kClass.simpleName ?: error("KClass must have a simple name")
            )
        }

    }
}