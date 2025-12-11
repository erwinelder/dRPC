package com.docta.dRPC.core.network

import com.docta.dRPC.core.network.websocket.WebSocketSessionClientContext
import com.docta.dRPC.core.network.websocket.WebSocketSessionContext
import com.docta.dRPC.core.network.websocket.WebSocketSessionServerContext
import com.docta.dRPC.core.result.error.DrpcError
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

inline fun <reified T> T.asCallParameter(): JsonElement {
    return Json.encodeToJsonElement(this)
}

suspend inline fun <reified T> DefaultWebSocketServerSession.send(value: T) {
    println("Sending value over WebSocket: $value")
    send(content = Json.encodeToString(value = value))
}

suspend inline fun <T> DefaultWebSocketServerSession.send(
    serializer: KSerializer<T>,
    value: T
) {
    println("Sending value over WebSocket: $value")
    send(content = Json.encodeToString(serializer = serializer, value = value))
}

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


suspend inline fun <reified R : Any> HttpClient.callPost(
    url: String,
    vararg parameters: JsonElement
): R {
    println("Calling URL: $url with parameters: ${parameters.joinToString(", ")}")
    return post(urlString = url) {
        contentType(ContentType.Application.Json)
        setBody(
            parameters
                .mapIndexed { index, parameter ->
                    index.toString() to parameter
                }
                .toMap()
        )
    }.let {
        println("Request response: ${it.bodyAsText()}")
        Json.decodeFromString(it.bodyAsText())
    }
}

suspend inline fun HttpClient.callWebSocket(
    url: String,
    crossinline processor: suspend DefaultClientWebSocketSession.() -> Unit,
    vararg parameters: JsonElement
) {
    println("Calling URL: $url with parameters: ${parameters.joinToString(", ")}")
    return webSocket(
        urlString = url,
        request = {
            contentType(ContentType.Application.Json)
            setBody(
                parameters
                    .mapIndexed { index, parameter ->
                        index.toString() to parameter
                    }
                    .toMap()
            )
        },
        block = {
            println("Starting to listen to WebSocket at $url")
            processor()
        }
    )
}


inline fun <reified R : Any> Route.processPostRoute(
    path: String,
    crossinline processor: suspend CallProcessor.() -> R
): Route {
    return post(path) {
        processPostCall { processor() }
    }
}

inline fun <ID, OD, E : DrpcError> Route.processWebSocketRoute(
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

inline fun <ID, OD, E : DrpcError> DefaultWebSocketServerSession.processWebSocketCall(
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


fun <ID, OD, E : DrpcError> WebSocketSessionContext<ID, OD, E>.asServerContext(): WebSocketSessionServerContext<ID, OD, E> {
    return this as? WebSocketSessionServerContext ?: throw IllegalStateException(
        "Server service functions working with web sockets must be called in a server context."
    )
}

fun <ID, OD, E : DrpcError> WebSocketSessionContext<ID, OD, E>.asClientContext(): WebSocketSessionClientContext<ID, OD, E> {
    return this as? WebSocketSessionClientContext ?: throw IllegalStateException(
        "Client request functions working with web sockets must be called in a client context."
    )
}