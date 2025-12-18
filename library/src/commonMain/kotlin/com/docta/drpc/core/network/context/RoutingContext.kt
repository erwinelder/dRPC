package com.docta.drpc.core.network.context

import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

class RoutingContext(
    val context: RoutingContext,
    val parameters: Map<String, JsonElement>
) : DrpcContext {

    val call: RoutingCall
        get() = context.call


    inline fun <reified T> get(index: Int): T {
        return parameters[index.toString()]?.let { Json.Default.decodeFromJsonElement(it) }
            ?: throw IllegalArgumentException("Parameter at index $index is missing or not of type ${T::class.simpleName}")
    }

}