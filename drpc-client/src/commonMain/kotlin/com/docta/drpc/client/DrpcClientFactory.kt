package com.docta.drpc.client

import io.ktor.client.HttpClient

interface DrpcClientFactory<S : Any> {

    fun create(
        baseHttpUrl: String,
        httpClient: HttpClient
    ): S

}