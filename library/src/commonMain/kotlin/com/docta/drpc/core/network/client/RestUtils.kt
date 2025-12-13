package com.docta.drpc.core.network.client

import com.docta.drpc.core.network.websocket.WebSocketSessionContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.error.DrpcError
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement


suspend inline fun <reified R : Any> HttpClient.callPost(
    url: String,
    vararg parameters: JsonElement
): R {
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
        Json.decodeFromString(it.bodyAsText())
    }
}


suspend inline fun HttpClient.callWebSocket(
    url: String,
    crossinline processor: suspend DefaultClientWebSocketSession.() -> Unit
) {
    webSocket(urlString = url) {
        processor()
    }
}

suspend inline fun <T> DefaultClientWebSocketSession.receive(
    serializer: KSerializer<T>,
    processor: suspend (ResultData<T, DrpcError>) -> Unit
) {
    for (frame in incoming) {
        when (frame) {
            is Frame.Text -> {
                processor(
                    Json.decodeFromString(
                        deserializer = ResultData.serializer(
                            serializer,
                            DrpcError.serializer()
                        ),
                        string = frame.readText()
                    )
                )
            }
            else -> {}
        }
    }
}


fun <ID, OD, E : DrpcError> WebSocketSessionContext<ID, OD, E>.asClientContext(): WebSocketSessionClientContext<ID, OD, E> {
    return this as? WebSocketSessionClientContext ?: throw IllegalStateException(
        "Client request functions working with web sockets must be called in a client context."
    )
}