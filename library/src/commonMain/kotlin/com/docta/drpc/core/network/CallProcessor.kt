package com.docta.drpc.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

class CallProcessor(
    val parameters: Map<String, JsonElement>
) {

    inline fun <reified T> get(index: Int): T {
        return parameters[index.toString()]?.let { Json.Default.decodeFromJsonElement(it) }
            ?: throw IllegalArgumentException("Parameter at index $index is missing or not of type ${T::class.simpleName}")
    }

}