package com.docta.drpc.server

import io.ktor.server.application.Application
import java.util.*

/**
 * Installs the dRPC server framework into the Ktor [Application]. It allows to register dRPC services to handle
 * incoming RPC calls.
 *
 * @throws IllegalStateException If the dRPC installer was not found. This can happen if you haven't applied the
 * `drpc-processor` or `drpc-processor-server` KSP processor in this module.
 */
fun Application.installDrpc() {
    synchronized(DrpcBinderRegistry) {
        val loader = DrpcBinderRegistryProvider::class.java.classLoader
        ServiceLoader.load(DrpcBinderRegistryProvider::class.java, loader).forEach { it.install(this) }
    }
}
