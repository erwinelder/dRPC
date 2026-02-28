package com.docta.drpc.client

import kotlin.reflect.KClass

object DrpcClientFactories {

    val map: MutableMap<KClass<*>, DrpcClientFactory<*>> = mutableMapOf()

}