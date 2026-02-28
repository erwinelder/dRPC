package com.docta.drpc.sample2.domain.model.result.error

import kotlinx.serialization.Serializable

@Serializable
sealed class Pizza2Error : Data2Error {

    @Serializable data object InternalError : Pizza2Error()
    @Serializable data object PizzaDoesNotExist : Pizza2Error()

}