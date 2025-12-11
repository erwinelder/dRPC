package com.docta.dRPC.core.network.websocket

import com.docta.dRPC.core.result.error.DrpcError

interface WebSocketSessionContext<out MD, out BD, out BE : DrpcError>