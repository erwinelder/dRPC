package com.docta.drpc.test.domain.service

import com.docta.drpc.core.annotation.Rpc
import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.test.domain.model.result.error.PizzaError

@Rpc(serviceBaseHttpUrl = "http://0.0.0.0:8080")
interface TestService {

    context(context: DrpcContext)
    suspend fun ping()

    context(context: DrpcContext)
    suspend fun hello(senderName: String): String

    context(context: DrpcContext)
    suspend fun orderPizza(pizza: String, amount: Int, address: String): SimpleResult<PizzaError>

}