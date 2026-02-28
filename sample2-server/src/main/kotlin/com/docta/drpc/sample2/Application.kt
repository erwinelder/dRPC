package com.docta.drpc.sample2

import com.docta.drpc.server.installDrpc
import com.docta.drpc.sample2.config.configureDI
import com.docta.drpc.sample2.config.configureSerialization
import com.docta.drpc.sample2.di.main2Module
import com.docta.drpc.sample2.presentation.routing.configureRouting2
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
    configureDI(main2Module)
    installDrpc()
    configureRouting2()
}
