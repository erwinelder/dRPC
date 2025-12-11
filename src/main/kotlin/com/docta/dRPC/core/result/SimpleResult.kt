package com.docta.dRPC.core.result

import com.docta.dRPC.core.result.error.DrpcError
import kotlinx.serialization.Serializable

@Serializable
sealed interface SimpleResult<out E : DrpcError> {

    @Serializable
    class Success<out E : DrpcError>: SimpleResult<E>

    @Serializable
    data class Error<out E : DrpcError>(val error: E): SimpleResult<E>


    fun getErrorOrNull(): E? = (this as? Error)?.error

}

inline fun <E : DrpcError> SimpleResult<E>.onSuccess(action: () -> Nothing) {
    if (this is SimpleResult.Success) action()
}

inline fun <E : DrpcError> SimpleResult<E>.runOnSuccess(action: () -> Unit) {
    if (this is SimpleResult.Success) action()
}

inline fun <E : DrpcError> SimpleResult<E>.onError(action: (E) -> Nothing) {
    if (this is SimpleResult.Error) action(this.error)
}
