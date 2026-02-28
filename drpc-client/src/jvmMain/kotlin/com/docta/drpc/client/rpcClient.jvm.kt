package com.docta.drpc.client

actual fun tryLoadGeneratedClientFactoryRegistry() {
    val fqn = "com.docta.drpc.client.DrpcClientFactoryRegistryGenerated"
    runCatching { Class.forName(fqn) }
}