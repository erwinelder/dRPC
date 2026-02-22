package com.docta.drpc.server

import io.ktor.server.application.Application

fun interface DrpcInstaller {

    fun install(application: Application)

}