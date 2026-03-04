package com.docta.drpc.server

import kotlin.reflect.KClass

object DrpcBinderRegistry {

    val binders: MutableMap<KClass<*>, DrpcBinder<*>> = mutableMapOf()

    fun <S : Any> register(service: KClass<S>, binder: DrpcBinder<S>) {
        val prev = binders.put(service, binder)
        require(prev == null) {
            "Duplicate dRPC server binder registered for service ${service.qualifiedName}. " +
                    "Existing = $prev, new = $binder"
        }
    }

}