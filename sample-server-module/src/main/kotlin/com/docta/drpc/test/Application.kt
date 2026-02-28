package com.docta.drpc.test

import com.docta.drpc.server.installDrpc
import com.docta.drpc.test.config.configureDI
import com.docta.drpc.test.config.configureSerialization
import com.docta.drpc.test.di.mainModule
import com.docta.drpc.test.presentation.routing.configureRouting
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
