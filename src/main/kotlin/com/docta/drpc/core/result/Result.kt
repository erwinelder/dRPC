package com.docta.drpc.core.result

import com.docta.drpc.core.result.error.DrpcError
import com.docta.drpc.core.result.success.DrpcSuccess
import kotlinx.serialization.Serializable

@Serializable
sealed interface Result<out S: DrpcSuccess?, out E: DrpcError> {

    @Serializable
    data class Success<out S: DrpcSuccess?, out E: DrpcError>(val success: S): Result<S, E>

    @Serializable
    data class Error<out S: DrpcSuccess?, out E: DrpcError>(val error: E): Result<S, E>

}