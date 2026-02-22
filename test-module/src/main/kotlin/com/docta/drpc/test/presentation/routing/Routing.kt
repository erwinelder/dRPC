package com.docta.drpc.test.presentation.routing

import com.docta.drpc.server.registerService
import com.docta.drpc.test.domain.service.TestService
import com.docta.drpc.test.domain.service.TestServiceImpl
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        get("/health") {
            call.respondText("App is running!")
        }

        registerService<TestService> { TestServiceImpl() }

    }
}