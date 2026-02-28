package com.docta.drpc.sample2.domain.service

import com.docta.drpc.core.annotation.Rpc
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.sample2.domain.model.result.error.Pizza2Error

@Rpc
interface Test2Service {

    context(context: DrpcContext)
    suspend fun ping()

    context(context: DrpcContext)
    suspend fun hello(senderName: String): String

    context(context: DrpcContext)
    suspend fun orderPizza(pizza: String, amount: Int, address: String): SimpleResult<Pizza2Error>

}