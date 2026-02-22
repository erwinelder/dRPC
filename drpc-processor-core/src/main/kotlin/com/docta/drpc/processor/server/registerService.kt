package com.docta.drpc.processor.server

import io.ktor.server.routing.Routing

inline fun <reified S : Any> Routing.registerService(noinline factory: () -> S) {
    val binder = RpcRegistry.binders[S::class] as? RpcBinder<S>
        ?: error("No dRPC binder generated for ${S::class.qualifiedName}.")
    binder.bind(this, factory())
}