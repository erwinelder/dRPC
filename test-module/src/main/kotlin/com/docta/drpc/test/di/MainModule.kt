package com.docta.drpc.test.di

import com.docta.drpc.core.network.HttpClientType
import com.docta.drpc.test.domain.service.TestRestControllerImpl
import com.docta.drpc.test.domain.service.TestService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val mainModule = module {

    /* ---------- Other ---------- */

    single(qualifier = named(enum = HttpClientType.Http)) {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    /* ---------- Services ---------- */

    single<TestService> {
        TestRestControllerImpl(httpClient = get(qualifier = named(enum = HttpClientType.Http)))
    }

}