package com.docta.drpc.sample2

import com.docta.drpc.client.rpcClient
import com.docta.drpc.core.network.context.callCatching
import com.docta.drpc.sample2.domain.model.result.error.Pizza2Error
import com.docta.drpc.sample2.domain.service.Test2Service
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MainIntegrationTest2 {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    @Test
    fun `client can call service function successfully`() = runTest {
        val testClient = rpcClient<Test2Service>(
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
                assertEquals(expected = Pizza2Error.PizzaDoesNotExist, actual = error)
            }
    }

}