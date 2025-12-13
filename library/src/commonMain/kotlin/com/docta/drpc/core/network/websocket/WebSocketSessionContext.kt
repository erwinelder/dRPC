package com.docta.drpc.core.network.websocket

import com.docta.drpc.core.result.error.DrpcError

interface WebSocketSessionContext<out MD, out BD, out BE : DrpcError>