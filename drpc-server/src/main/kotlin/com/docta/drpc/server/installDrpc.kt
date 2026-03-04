package com.docta.drpc.server

import io.ktor.server.application.Application
import java.util.*

/**
 * Installs the dRPC server framework into the Ktor [Application]. It allows to register dRPC services to handle
 * incoming RPC calls.
 */
fun Application.installDrpc() {
    synchronized(DrpcBinderRegistry) {
        ServiceLoader.load(
            DrpcBinderRegistryProvider::class.java,
            DrpcBinderRegistryProvider::class.java.classLoader
        ).forEach { it.install(this) }
    }
}
