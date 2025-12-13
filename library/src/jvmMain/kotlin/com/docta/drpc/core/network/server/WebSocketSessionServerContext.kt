package com.docta.drpc.core.network.server

import com.docta.drpc.core.network.send
import com.docta.drpc.core.network.websocket.WebSocketSessionContext
import com.docta.drpc.core.result.ResultData
import com.docta.drpc.core.result.error.DrpcError
import io.ktor.server.websocket.*
import kotlinx.serialization.KSerializer

data class WebSocketSessionServerContext<MD, BD, BE : DrpcError>(
    val session: DefaultWebSocketServerSession,
    val mobileDataSerializer: KSerializer<MD>,
    val backendDataSerializer: KSerializer<BD>,
    val backendErrorSerializer: KSerializer<BE>
) : WebSocketSessionContext<MD, BD, BE> {

    suspend fun sendData(data: BD) {
        session.send(
            serializer = ResultData.serializer(
                backendDataSerializer,
                backendErrorSerializer
            ),
            value = ResultData.Success(data = data)
        )
    }

    suspend fun sendError(error: BE) {
        session.send(
            serializer = ResultData.serializer(
                backendDataSerializer,
                backendErrorSerializer
            ),
            value = ResultData.Error(error = error)
        )
    }

    suspend inline fun receive(processor: suspend (MD) -> Unit) {
        session.receive(serializer = mobileDataSerializer, processor = processor)
    }

}