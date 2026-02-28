package com.docta.drpc.client

import java.util.*

@Volatile
private var installed = false

actual fun tryLoadGeneratedClientFactoryRegistry() {
    if (installed) return

    synchronized(DrpcClientFactories) {
        if (installed) return
        ServiceLoader.load(DrpcClientFactoryRegistryProvider::class.java).forEach { it.install() }
        installed = true
    }
}