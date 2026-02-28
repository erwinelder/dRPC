package com.docta.drpc.sample1.di

import com.docta.drpc.client.rpcClient
import com.docta.drpc.core.network.HttpClientType
import com.docta.drpc.sample1.domain.service.Test1Service
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val main1Module = module {

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
        rpcClient<Test1Service>(
            baseHttpUrl = "http://0.0.0.0:8080",
            httpClient = get(qualifier = named(enum = HttpClientType.Http))
        )
    }

}