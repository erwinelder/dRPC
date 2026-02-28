package com.docta.drpc.sample

import com.docta.drpc.sample.config.configureDI
import com.docta.drpc.sample.config.configureSerialization
import com.docta.drpc.sample.di.mainModule
import com.docta.drpc.sample.presentation.routing.configureRouting
import com.docta.drpc.server.installDrpc
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
    configureDI(mainModule)
    installDrpc()
    configureRouting()
}
