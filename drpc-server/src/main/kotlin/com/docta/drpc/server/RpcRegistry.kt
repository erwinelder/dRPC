package com.docta.drpc.server

import kotlin.reflect.KClass

object RpcRegistry {
    val binders: MutableMap<KClass<*>, RpcBinder<*>> = mutableMapOf()
}