package com.docta.drpc.sample1.domain.service

import com.docta.drpc.core.network.context.DrpcContext
import com.docta.drpc.core.result.SimpleResult
import com.docta.drpc.sample1.domain.model.result.error.Pizza1Error

class Test1ServiceImpl : Test1Service {

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
    ): SimpleResult<Pizza1Error> {
        println("Order received: $amount x $pizza to be delivered at $address")

        val validPizzas = listOf("Margherita", "Pepperoni", "Hawaiian")
        return if (pizza in validPizzas) {
            println("Preparing $amount x $pizza...")
            SimpleResult.Success()
        } else {
            println("Sorry, we don't have $pizza on the menu.")
            SimpleResult.Error(Pizza1Error.PizzaDoesNotExist)
        }
    }

}