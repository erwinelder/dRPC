package com.docta.drpc.core.network

import io.ktor.websocket.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json


suspend inline fun <reified T> DefaultWebSocketSession.send(value: T) {
    println("Sending value over WebSocket: $value")
    send(content = Json.encodeToString(value = value))
}

suspend inline fun <T> DefaultWebSocketSession.send(
    serializer: KSerializer<T>,
    value: T
) {
    println("Sending value over WebSocket: $value")
    send(content = Json.encodeToString(serializer = serializer, value = value))
}