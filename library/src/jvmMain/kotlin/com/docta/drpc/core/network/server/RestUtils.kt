package com.docta.drpc.core.network.server

import com.docta.drpc.core.network.CallProcessor
import com.docta.drpc.core.network.websocket.WebSocketSessionContext
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement


suspend inline fun <T> DefaultWebSocketServerSession.receive(
    serializer: KSerializer<T>,
    processor: suspend (T) -> Unit
) {
    for (frame in incoming) {
        when (frame) {
            is Frame.Text -> {
                processor(
                    Json.decodeFromString(deserializer = serializer, string = frame.readText())
                )
            }
            else -> {}
        }
    }
}


inline fun <reified R : Any> Route.processPostRoute(
    path: String,
    crossinline processor: suspend CallProcessor.() -> R
): Route {
    return post(path) {
        processPostCall { processor() }
    }
}

inline fun <ID, OD, E> Route.processWebSocketRoute(
    path: String,
    incomingDataSerializer: KSerializer<ID>,
    outgoingDataSerializer: KSerializer<OD>,
    outgoingErrorSerializer: KSerializer<E>,
    crossinline processor: suspend WebSocketSessionServerContext<ID, OD, E>.() -> Unit
) {
    webSocket(path) {
        processWebSocketCall(
            incomingDataSerializer = incomingDataSerializer,
            outgoingDataSerializer = outgoingDataSerializer,
            outgoingErrorSerializer = outgoingErrorSerializer
        ) {
            processor()
        }
    }
}


suspend inline fun <reified R : Any> RoutingContext.processPostCall(
    processor: CallProcessor.() -> R
) {
    CallProcessor(parameters = call.receive<Map<String, JsonElement>>())
        .run { processor() }
        .let { call.respond(it) }
}

inline fun <ID, OD, E> DefaultWebSocketServerSession.processWebSocketCall(
    incomingDataSerializer: KSerializer<ID>,
    outgoingDataSerializer: KSerializer<OD>,
    outgoingErrorSerializer: KSerializer<E>,
    processor: WebSocketSessionServerContext<ID, OD, E>.() -> Unit
) {
    WebSocketSessionServerContext(
        session = this,
        mobileDataSerializer = incomingDataSerializer,
        backendDataSerializer = outgoingDataSerializer,
        backendErrorSerializer = outgoingErrorSerializer
    ).run {
        processor()
    }
}


fun <ID, OD, E> WebSocketSessionContext<ID, OD, E>.asServerContext(): WebSocketSessionServerContext<ID, OD, E> {
    return this as? WebSocketSessionServerContext ?: throw IllegalStateException(
        "Server service functions working with web sockets must be called in a server context."
    )
}