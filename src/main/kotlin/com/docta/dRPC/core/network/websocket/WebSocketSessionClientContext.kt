package com.docta.dRPC.core.network.websocket

import com.docta.dRPC.core.result.error.DrpcError
import io.ktor.client.plugins.websocket.*

data class WebSocketSessionClientContext<MD, BD, BE : DrpcError>(
    val processor: suspend DefaultClientWebSocketSession.() -> Unit
) : WebSocketSessionContext<MD, BD, BE>