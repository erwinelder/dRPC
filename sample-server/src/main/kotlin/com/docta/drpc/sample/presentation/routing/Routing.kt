package com.docta.drpc.sample.presentation.routing

import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.sample.domain.service.TestService
import com.docta.drpc.sample.domain.service.TestServiceImpl
import com.docta.drpc.sample1.domain.service.Test1Service
import com.docta.drpc.sample2.domain.service.Test2Service
import com.docta.drpc.server.registerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    routing {

        get("/health") {
            call.respondText("App is running!")
        }

        registerService<TestService> { TestServiceImpl() }

        post("/ping-service1") {
            val test1Service = this@configureRouting.get<Test1Service>()

            callCatching { test1Service.ping() }

            call.respond(HttpStatusCode.OK)
        }

        post("/ping-service2") {
            val test2Service = this@configureRouting.get<Test2Service>()

            callCatching { test2Service.ping() }

            call.respond(HttpStatusCode.OK)
        }

    }
}