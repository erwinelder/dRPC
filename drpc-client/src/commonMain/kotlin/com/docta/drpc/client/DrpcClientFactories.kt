package com.docta.drpc.client

import kotlin.reflect.KClass

object DrpcClientFactories {

    val map: MutableMap<KClass<*>, DrpcClientFactory<*>> = mutableMapOf()

    fun <S : Any> register(service: KClass<S>, factory: DrpcClientFactory<S>) {
        val prev = map.put(service, factory)
        require(prev == null) {
            "Duplicate dRPC client factory registered for service ${service.qualifiedName}. " +
                    "Existing = $prev, new = $factory"
        }
    }

}