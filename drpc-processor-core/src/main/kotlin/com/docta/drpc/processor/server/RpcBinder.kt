package com.docta.drpc.processor.server

import io.ktor.server.routing.Routing

interface RpcBinder<S : Any> {
    fun bind(routing: Routing, service: S)
}