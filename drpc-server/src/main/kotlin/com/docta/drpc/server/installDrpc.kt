package com.docta.drpc.server

import io.ktor.server.application.Application

fun Application.installDrpc() {
    tryLoadGeneratedInstaller()

    DrpcInstallerHolder.installer?.install(this)
        ?: error(
            "dRPC installer was not found.\n" +
                    "Make sure you applied drpc-processor-server via KSP in this module and rebuild."
        )
}

private fun tryLoadGeneratedInstaller() {
    if (DrpcInstallerHolder.installer != null) return
    val fqn = "com.docta.drpc.server.DrpcInstallerGenerated"
    runCatching { Class.forName(fqn) }
}