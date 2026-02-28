package com.docta.drpc.sample.domain.service

import com.docta.drpc.core.annotation.Rpc
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.sample.domain.model.result.error.PizzaError

@Rpc
interface TestService {

    context(context: DrpcContext)
    suspend fun ping()

    context(context: DrpcContext)
    suspend fun hello(senderName: String): String

    context(context: DrpcContext)
    suspend fun orderPizza(pizza: String, amount: Int, address: String): SimpleResult<PizzaError>

}