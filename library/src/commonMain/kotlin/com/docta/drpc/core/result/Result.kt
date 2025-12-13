package com.docta.drpc.core.result

import kotlinx.serialization.Serializable

@Serializable
sealed interface Result<out S, out E> {

    @Serializable
    data class Success<out S, out E>(val success: S): Result<S, E>

    @Serializable
    data class Error<out S, out E>(val error: E): Result<S, E>

}