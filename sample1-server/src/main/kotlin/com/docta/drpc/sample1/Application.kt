package com.docta.drpc.sample1

import com.docta.drpc.server.installDrpc
import com.docta.drpc.sample1.config.configureDI
import com.docta.drpc.sample1.config.configureSerialization
import com.docta.drpc.sample1.di.main1Module
import com.docta.drpc.sample1.presentation.routing.configureRouting1
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(
        factory = Netty,
        port = System.getenv("PORT")?.toIntOrNull() ?: 8080,
        host = "0.0.0.0",
        module = Application::appModule
    ).start(wait = true)
}

fun Application.appModule() {
    configureSerialization()
    configureDI(main1Module)
    installDrpc()
    configureRouting1()
}
