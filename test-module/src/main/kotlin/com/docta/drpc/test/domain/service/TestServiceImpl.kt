package com.docta.drpc.test.domain.service

import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.test.domain.model.result.error.PizzaError

class TestServiceImpl : TestService {

    context(context: DrpcContext)
    override suspend fun ping() {
        println("ping-pong")
    }

    context(context: DrpcContext)
    override suspend fun hello(senderName: String): String {
        return "Hello, $senderName!"
    }

    context(context: DrpcContext)
    override suspend fun orderPizza(
        pizza: String,
        amount: Int,
        address: String
    ): SimpleResult<PizzaError> {
        println("Order received: $amount x $pizza to be delivered at $address")

        val validPizzas = listOf("Margherita", "Pepperoni", "Hawaiian")
        return if (pizza in validPizzas) {
            println("Preparing $amount x $pizza...")
            SimpleResult.Success()
        } else {
            println("Sorry, we don't have $pizza on the menu.")
            SimpleResult.Error(PizzaError.PizzaDoesNotExist)
        }
    }

}