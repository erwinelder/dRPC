package com.docta.drpc.client.network

import com.docta.drpc.core.network.websocket.WebSocketSessionContext
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession

data class WebSocketSessionClientContext<MD, BD, BE>(
    val processor: suspend DefaultClientWebSocketSession.() -> Unit
) : WebSocketSessionContext<MD, BD, BE>