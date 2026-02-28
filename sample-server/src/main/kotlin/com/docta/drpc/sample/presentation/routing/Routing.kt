package com.docta.drpc.sample.presentation.routing

import com.docta.drpc.client.rpcClient
import com.docta.drpc.core.network.HttpClientType
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.sample.domain.service.TestService
import com.docta.drpc.sample.domain.service.TestServiceImpl
import com.docta.drpc.sample1.domain.service.Test1Service
import com.docta.drpc.server.service.registerService
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    routing {

        get("/health") {
            call.respondText("App is running!")
        }

        registerService<TestService> { TestServiceImpl() }

        post("/ping-service1") {
            val httpClient = this@configureRouting.get<HttpClient>(qualifier = named(enum = HttpClientType.Http))
            val rpcClient = rpcClient<Test1Service>(
                baseHttpUrl = "http://0.0.0.0:8080",
                httpClient = httpClient
            )

            callCatching { rpcClient.ping() }

            call.respond(HttpStatusCode.OK)
        }

    }
}