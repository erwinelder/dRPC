package com.docta.drpc.sample2.di

import com.docta.drpc.client.rpcClient
import com.docta.drpc.core.network.HttpClientType
import com.docta.drpc.sample2.domain.service.Test2Service
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val main2Module = module {

    /* ---------- Other ---------- */

    single(qualifier = named(enum = HttpClientType.Http)) {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    /* ---------- Services ---------- */

    single {
        rpcClient<Test2Service>(
            baseHttpUrl = "http://0.0.0.0:8080",
            httpClient = get(qualifier = named(enum = HttpClientType.Http))
        )
    }

}