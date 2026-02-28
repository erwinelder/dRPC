package com.docta.drpc.client

import io.ktor.client.*

/**
 * Creates an RPC client for the specified service interface [S] (service must be annotated with `@Rpc`).
 *
 * @param baseHttpUrl The URL of the server to connect to. For example, for a service `AuthService` registered on the
 * server under the root path `/` and running on 0.0.0.0:8080, the base URL would be `http://0.0.0.0:8080`.
 * @param httpClient A configured instance of Ktor's [HttpClient] to be used for making HTTP requests.
 *
 * @return An implementation of the service interface [S] that can be used to call the server's RPC functions.
 *
 * @throws IllegalStateException If no dRPC client factory is registered for the specified service interface.
 * This can happen if you haven't applied the `drpc-processor` or `drpc-processor-client` KSP processor, or if the
 * service interface is not annotated with `@Rpc`.
 */
inline fun <reified S : Any> rpcClient(
    baseHttpUrl: String,
    httpClient: HttpClient
): S {
    tryLoadGeneratedClientFactoryRegistry()

    @Suppress("UNCHECKED_CAST")
    val factory = DrpcClientFactories.map[S::class] as? DrpcClientFactory<S>
        ?: error("No dRPC client factory registered for service ${S::class.qualifiedName}. " +
                "Ensure you applied `drpc-processor` or `drpc-processor-client` via KSP and that the service interface is annotated with @Rpc.")
    return factory.create(baseHttpUrl = baseHttpUrl, httpClient = httpClient)
}


expect fun tryLoadGeneratedClientFactoryRegistry()
