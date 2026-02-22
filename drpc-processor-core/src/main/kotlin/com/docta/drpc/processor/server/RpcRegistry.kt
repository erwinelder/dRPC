package com.docta.drpc.processor.server

import kotlin.reflect.KClass

object RpcRegistry {
    @PublishedApi internal val binders: MutableMap<KClass<*>, RpcBinder<*>> = mutableMapOf()
}