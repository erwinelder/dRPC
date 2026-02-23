package com.docta.drpc.server.service

import kotlin.reflect.KClass

object RpcRegistry {

    val binders: MutableMap<KClass<*>, RpcBinder<*>> = mutableMapOf()

}