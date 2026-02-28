package com.docta.drpc.server

import io.ktor.server.application.Application

/**
 * Installs the dRPC server framework into the Ktor [Application]. It allows to register dRPC services to handle
 * incoming RPC calls.
 *
 * @throws IllegalStateException If the dRPC installer was not found. This can happen if you haven't applied the
 * `drpc-processor` or `drpc-processor-server` KSP processor in this module.
 */
fun Application.installDrpc() {
    tryLoadGeneratedInstaller()

    DrpcInstallerHolder.installer?.install(this)
        ?: error(
            "dRPC installer was not found.\n" +
                    "Make sure you applied 'drpc-processor-server' or 'drpc-processor' via KSP in this module and rebuild."
        )
}

private fun tryLoadGeneratedInstaller() {
    if (DrpcInstallerHolder.installer != null) return
    val fqn = "com.docta.drpc.server.DrpcInstallerGenerated"
    runCatching { Class.forName(fqn) }
}