package com.docta.drpc.core.network.client

import com.docta.drpc.core.network.websocket.WebSocketSessionContext
import io.ktor.client.plugins.websocket.*

data class WebSocketSessionClientContext<MD, BD, BE>(
    val processor: suspend DefaultClientWebSocketSession.() -> Unit
) : WebSocketSessionContext<MD, BD, BE>