package com.docta.drpc.test.presentation.routing

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        get("/health") {
            call.respondText("App is running!")
        }

//        presentationsManagementRouting(
//            restController = this@configureRouting.get(),
//            service = this@configureRouting.get()
//        )

    }
}