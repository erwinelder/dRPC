package com.docta.drpc.core.network.websocket

import com.docta.drpc.core.result.error.DrpcError
import io.ktor.client.plugins.websocket.*

data class WebSocketSessionClientContext<MD, BD, BE : DrpcError>(
    val processor: suspend DefaultClientWebSocketSession.() -> Unit
) : WebSocketSessionContext<MD, BD, BE>