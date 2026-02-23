package com.docta.drpc.test.domain.model.result.error

import kotlinx.serialization.Serializable

@Serializable
sealed class PizzaError : DataError {

    @Serializable data object InternalError : PizzaError()
    @Serializable data object PizzaDoesNotExist : PizzaError()

}