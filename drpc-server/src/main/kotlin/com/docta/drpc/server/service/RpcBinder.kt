package com.docta.drpc.server.service

import io.ktor.server.routing.Routing

interface RpcBinder<S : Any> {

    fun bind(routing: Routing, service: S)

}