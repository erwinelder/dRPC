package com.docta.drpc.sample2.presentation.routing

import com.docta.drpc.server.service.registerService
import com.docta.drpc.sample2.domain.service.Test2Service
import com.docta.drpc.sample2.domain.service.Test2ServiceImpl
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting2() {
    routing {

        get("/health") {
            call.respondText("App is running!")
        }

        registerService<Test2Service> { Test2ServiceImpl() }

    }
}