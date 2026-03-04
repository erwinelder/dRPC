package com.docta.drpc.server

import io.ktor.server.application.Application

interface DrpcBinderRegistryProvider {

    fun install(application: Application)

}