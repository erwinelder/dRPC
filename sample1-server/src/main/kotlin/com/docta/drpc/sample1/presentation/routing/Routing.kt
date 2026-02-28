package com.docta.drpc.sample1.presentation.routing

import com.docta.drpc.server.service.registerService
import com.docta.drpc.sample1.domain.service.Test1Service
import com.docta.drpc.sample1.domain.service.Test1ServiceImpl
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting1() {
    routing {

        get("/health") {
            call.respondText("App is running!")
        }

        registerService<Test1Service> { Test1ServiceImpl() }

    }
}