package com.docta.drpc.sample1

import com.docta.drpc.client.rpcClient
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.sample1.domain.model.result.error.Pizza1Error
import com.docta.drpc.sample1.domain.service.Test1Service
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MainIntegrationTest1 {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    @Test
    fun `client can call service function successfully`() = runTest {
        val testClient = rpcClient<Test1Service>(
            baseHttpUrl = "http://0.0.0.0:8080",
            httpClient = httpClient
        )

        callCatching { testClient.orderPizza(pizza = "Margherita", amount = 1, address = "123 Main St") }
            .getOrElse { throw it }
            .let {
                assertTrue { it.isSuccess() }
            }

        callCatching { testClient.orderPizza(pizza = "Unknown", amount = 1, address = "123 Main St") }
            .getOrElse { throw it }
            .let {
                val error = it.getErrorOrNull()
                assertNotNull(error)
                assertEquals(expected = Pizza1Error.PizzaDoesNotExist, actual = error)
            }
    }

}