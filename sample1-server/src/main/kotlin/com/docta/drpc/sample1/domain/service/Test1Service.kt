package com.docta.drpc.sample1.domain.service

import com.docta.drpc.core.annotation.Rpc
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.sample1.domain.model.result.error.Pizza1Error

@Rpc
interface Test1Service {

    context(context: DrpcContext)
    suspend fun ping()

    context(context: DrpcContext)
    suspend fun hello(senderName: String): String

    context(context: DrpcContext)
    suspend fun orderPizza(pizza: String, amount: Int, address: String): SimpleResult<Pizza1Error>

}