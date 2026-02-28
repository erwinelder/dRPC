package com.docta.drpc.sample1.domain.model.result.error

import kotlinx.serialization.Serializable

@Serializable
sealed class Pizza1Error : Data1Error {

    @Serializable data object InternalError : Pizza1Error()
    @Serializable data object PizzaDoesNotExist : Pizza1Error()

}